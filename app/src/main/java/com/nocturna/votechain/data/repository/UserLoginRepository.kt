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
import com.nocturna.votechain.security.CryptoKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Repository class to handle user login operations with voter data integration
 */
class UserLoginRepository(private val context: Context) {
    private val TAG = "EnhancedUserLoginRepository"
    private val apiService = NetworkClient.apiService
    private val voterRepository = VoterRepository(context)

    private val PREFS_NAME = "VoteChainPrefs"
    private val SECURE_PREFS_NAME = "VoteChainSecurePrefs"

    // Storage keys
    private val KEY_USER_TOKEN = "user_token"
    private val KEY_USER_EMAIL = "user_email"
    private val KEY_USER_ID = "user_id"
    private val KEY_REFRESH_TOKEN = "refresh_token"
    private val KEY_LOGIN_TIMESTAMP = "login_timestamp"
    private val KEY_USER_PASSWORD_HASH = "user_password_hash"
    private val KEY_USER_EXPIRES_AT = "expires_at"
    private val KEY_SESSION_EXPIRY = "session_expiry"
    private val KEY_FAILED_LOGIN_ATTEMPTS = "failed_login_attempts"
    private val KEY_LAST_FAILED_ATTEMPT = "last_failed_attempt"

    // Security constants
    private val MAX_LOGIN_ATTEMPTS = 5
    private val LOCKOUT_DURATION = 30 * 60 * 1000L // 30 minutes
    private val SESSION_DURATION = 24 * 60 * 60 * 1000L // 24 hours

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
    suspend fun verifyPassword(password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userEmail = getUserEmail()
            if (userEmail.isNullOrEmpty()) {
                Log.w(TAG, "No user email found for password verification")
                return@withContext false
            }

            // Try to login with current email and provided password
            val loginResult = loginUser(userEmail, password)

            return@withContext loginResult.fold(
                onSuccess = {
                    Log.d(TAG, "Password verification successful")
                    true
                },
                onFailure = {
                    Log.d(TAG, "Password verification failed")
                    false
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying password", e)
            return@withContext false
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
    fun getUserEmail(): String? {
        return try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val email = sharedPreferences.getString(KEY_USER_EMAIL, null)
            Log.d(TAG, "Retrieved user email for verification: ${email?.take(3)}***")
            email
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user email", e)
            null
        }
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
    suspend fun logoutUser() : Result<Unit> = withContext(Dispatchers.IO) {
        try {
//            // Clear login data
//            clearProfileData()

            // Clear voter data
            val voterRepository = VoterRepository(context)
            voterRepository.clearVoterData()

            // Clear user profile data
            val userProfileRepository = UserProfileRepository(context)
            userProfileRepository.clearProfileData()

            // Clear crypto keys
            val cryptoKeyManager = CryptoKeyManager(context)
            cryptoKeyManager.clearStoredKeys()

            Log.d(TAG, "Enhanced logout completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during enhanced logout", e)
            Result.failure(e)
        }
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
     * Check if user session is still valid
     */
    suspend fun isSessionValid(): Boolean = withContext(Dispatchers.IO) {
        try {

            val token = getUserToken()
            if (token.isNullOrEmpty()) {
                return@withContext false
            }

            // Try to validate token with server
            val response = apiService.getVoterDataWithToken("Bearer $token")
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error checking session validity", e)
            return@withContext false
        }
    }

    /**
     * Store complete user session data
     */
    fun storeCompleteUserSession(
        userToken: String,
        userEmail: String,
        userId: String? = null,
        refreshToken: String? = null
    ) {
        try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString(KEY_USER_TOKEN, userToken)
                putString(KEY_USER_EMAIL, userEmail)
                userId?.let { putString(KEY_USER_ID, it) }
                refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
                putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
                apply()
            }
            Log.d(TAG, "Complete user session stored")
        } catch (e: Exception) {
            Log.e(TAG, "Error storing complete user session", e)
        }
    }

    /**
     * Data class to hold user session information
     */
    data class UserSession(
        val token: String,
        val email: String,
        val userId: String? = null,
        val refreshToken: String? = null,
        val loginTimestamp: Long = 0
    )

    /**
     * Get complete user session data
     */
    fun getCompleteUserSession(): UserSession? {
        return try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            val token = sharedPreferences.getString(KEY_USER_TOKEN, null)
            val email = sharedPreferences.getString(KEY_USER_EMAIL, null)
            val userId = sharedPreferences.getString(KEY_USER_ID, null)
            val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
            val loginTimestamp = sharedPreferences.getLong(KEY_LOGIN_TIMESTAMP, 0)

            if (token != null && email != null) {
                UserSession(
                    token = token,
                    email = email,
                    userId = userId,
                    refreshToken = refreshToken,
                    loginTimestamp = loginTimestamp
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting complete user session", e)
            null
        }
    }

    /**
     * Get private key for specific email user
     */
    fun getPrivateKey(email: String): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("${email}_private_key", null)
    }

    /**
     * Get public key for specific email user
     */
    fun getPublicKey(email: String): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("${email}_public_key", null)
    }

    /**
     * Save keys for specific email user
     */
    fun saveKeysForUser(email: String, privateKey: String, publicKey: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("${email}_private_key", privateKey)
        editor.putString("${email}_public_key", publicKey)
        editor.apply()
        Log.d(TAG, "Keys saved for user: $email")
    }
}