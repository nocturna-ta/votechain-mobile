package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.LoginRequest
import com.nocturna.votechain.data.model.UserLoginData
import com.nocturna.votechain.data.network.ElectionNetworkClient
import com.nocturna.votechain.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Repository class to handle user login operations with voter data integration
 */
class UserLoginRepository(private val context: Context) {
    private val TAG = "UserLoginRepository"
    private val apiService = NetworkClient.apiService
    private val voterRepository = VoterRepository(context)

    private val PREFS_NAME = "VoteChainPrefs"
    private val SECURE_PREFS_NAME = "VoteChainSecurePrefs"
    private val KEY_USER_TOKEN = "user_token"
    private val KEY_USER_EMAIL = "user_email"
    private val KEY_USER_EXPIRES_AT = "expires_at"
    private val KEY_USER_PASSWORD_HASH = "user_password_hash"

    // Initialize encrypted shared preferences for storing sensitive data
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create encrypted preferences, falling back to regular preferences", e)
        context.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Login user with email and password, then fetch voter data
     * @return Result containing either the successful response or an exception
     */
    suspend fun loginUser(
        email: String,
        password: String
    ): Result<ApiResponse<UserLoginData>> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(
                email = email,
                password = password
            )

            Log.d(TAG, "Attempting to login user with email: $email")
            val response = apiService.loginUser(request)

            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    Log.d(TAG, "Login successful: ${loginResponse.message}")

                    // Store user data if login is successful and there is a token
                    if (loginResponse.code == 200 && loginResponse.data?.token?.isNotEmpty() == true) {
                        saveUserData(loginResponse.data, email)
                        // Save password hash for profile verification
                        savePasswordHash(password)

                        // Fetch voter data after successful login
                        try {
                            val voterResult = voterRepository.fetchVoterData(loginResponse.data.token)
                            if (voterResult.isSuccess) {
                                Log.d(TAG, "Voter data fetched successfully")
                            } else {
                                Log.w(TAG, "Failed to fetch voter data: ${voterResult.exceptionOrNull()?.message}")
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Exception while fetching voter data", e)
                        }
                    }

                    Result.success(loginResponse)
                } ?: run {
                    Log.e(TAG, "Empty response body with success code: ${response.code()}")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Login failed with code: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")

                // Try to parse the error body as JSON for more detailed logging
                try {
                    val gson = Gson()
                    val errorJson = gson.fromJson(errorBody, ApiResponse::class.java)
                    Log.e(TAG, "Parsed error: ${errorJson.message}, code: ${errorJson.code}")
                    if (errorJson.error != null) {
                        Log.e(TAG, "Error details: ${errorJson.error.error_message}")
                    }
                    Result.failure(Exception(errorJson.message))
                } catch (e: Exception) {
                    Log.e(TAG, "Could not parse error JSON: ${e.message}")
                    Result.failure(Exception("Error: ${response.code()} - $errorBody"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during login", e)
            Result.failure(e)
        }
    }

    /**
     * Save user data to SharedPreferences including email
     */
    private fun saveUserData(userData: UserLoginData, email: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_USER_TOKEN, userData.token)
            putString(KEY_USER_EMAIL, email) // Save the email used for login
            putString(KEY_USER_EXPIRES_AT, userData.expires_at)
            apply()
        }
        Log.d(TAG, "User data saved to SharedPreferences with email: $email")
    }

    /**
     * Save password hash to encrypted shared preferences
     */
    private fun savePasswordHash(password: String) {
        try {
            val passwordHash = hashPassword(password)
            with(encryptedSharedPreferences.edit()) {
                putString(KEY_USER_PASSWORD_HASH, passwordHash)
                apply()
            }
            Log.d(TAG, "Password hash saved securely")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save password hash", e)
        }
    }

    /**
     * Verify password against stored hash
     */
    fun verifyPassword(password: String): Boolean {
        return try {
            val storedHash = encryptedSharedPreferences.getString(KEY_USER_PASSWORD_HASH, null)
            if (storedHash != null) {
                val inputHash = hashPassword(password)
                val isValid = storedHash == inputHash
                Log.d(TAG, "Password verification: ${if (isValid) "SUCCESS" else "FAILED"}")
                isValid
            } else {
                Log.w(TAG, "No stored password hash found")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during password verification", e)
            false
        }
    }

    /**
     * Hash password using SHA-256 with salt
     */
    private fun hashPassword(password: String): String {
        val salt = "VoteChain_Salt_2024" // In production, use a unique salt per user
        val input = "$password$salt"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Save user token to SharedPreferences
     */
    fun saveUserToken(token: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_USER_TOKEN, token)
            apply()
        }

        // Also save token to ElectionNetworkClient to ensure it's available for election API calls
        ElectionNetworkClient.saveUserToken(token)

        Log.d(TAG, "User token saved to SharedPreferences and ElectionNetworkClient")
    }

    /**
     * Get saved user token from SharedPreferences
     */
    fun getUserToken(): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USER_TOKEN, "") ?: ""
    }

    /**
     * Get saved user email from SharedPreferences
     */
    fun getUserEmail(): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
    }

    /**
     * Check if user is logged in (has a valid token)
     */
    fun isUserLoggedIn(): Boolean {
        val token = getUserToken()
        val expiresAt = getUserTokenExpiry()

        // Check if token exists and is not expired
        if (token.isNotEmpty() && expiresAt.isNotEmpty()) {
            try {
                // Simple expiry check - in a real app you might want to parse the date
                // and check if it's in the future
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Error checking token expiry: ${e.message}")
            }
        }
        return token.isNotEmpty()
    }

    /**
     * Get token expiry time
     */
    private fun getUserTokenExpiry(): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USER_EXPIRES_AT, "") ?: ""
    }

    /**
     * Log out user by clearing token, user data, and voter data
     */
    fun logoutUser() {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(KEY_USER_TOKEN)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_EXPIRES_AT)
            apply()
        }

        // Clear password hash from encrypted preferences
        try {
            with(encryptedSharedPreferences.edit()) {
                remove(KEY_USER_PASSWORD_HASH)
                apply()
            }
            Log.d(TAG, "Password hash cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear password hash", e)
        }

        // Clear voter data as well
        voterRepository.clearVoterData()

        Log.d(TAG, "User logged out, all data cleared")
    }

    /**
     * Check if token is expired (enhanced version)
     * You can implement more sophisticated date parsing here
     */
    fun isTokenExpired(): Boolean {
        val expiresAt = getUserTokenExpiry()
        if (expiresAt.isEmpty()) return true

        try {
            // TODO: Implement proper date parsing based on your API's date format
            // For example, if expires_at is in ISO 8601 format:
            // val expiryDate = Instant.parse(expiresAt)
            // return expiryDate.isBefore(Instant.now())

            // For now, we assume token is valid if it exists
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing token expiry date: ${e.message}")
            return true // Assume expired if we can't parse
        }
    }

    /**
     * Clear expired session
     */
    fun clearExpiredSession() {
        if (isTokenExpired()) {
            logoutUser()
            Log.d(TAG, "Expired session cleared")
        }
    }

    /**
     * Get user data summary for debugging
     */
    fun getUserDataSummary(): Map<String, String> {
        return mapOf(
            "hasToken" to getUserToken().isNotEmpty().toString(),
            "email" to getUserEmail(),
            "expiresAt" to getUserTokenExpiry(),
            "isLoggedIn" to isUserLoggedIn().toString(),
            "isExpired" to isTokenExpired().toString()
        )
    }
}