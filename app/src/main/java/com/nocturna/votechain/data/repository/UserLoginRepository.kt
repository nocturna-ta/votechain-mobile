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
    private val KEY_USER_EXPIRES_AT = "expires_at"
    private val KEY_USER_PASSWORD_HASH = "user_password_hash"
    private val KEY_SESSION_EXPIRY = "session_expiry"
    private val KEY_FAILED_LOGIN_ATTEMPTS = "failed_login_attempts"
    private val KEY_LAST_FAILED_ATTEMPT = "last_failed_attempt"

    // Security constants
    private val MAX_LOGIN_ATTEMPTS = 5
    private val LOCKOUT_DURATION = 30 * 60 * 1000L // 30 minutes
    private val SESSION_DURATION = 24 * 60 * 60 * 1000L // 24 hours

    // Initialize encrypted shared preferences
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
        Log.e(TAG, "Failed to create encrypted preferences", e)
        context.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Enhanced login with email verification and security checks
     */
    suspend fun loginUserSecurely(
        email: String,
        password: String
    ): Result<LoginResult> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Check for account lockout
            if (isAccountLocked()) {
                return@withContext Result.failure(
                    SecurityException("Account temporarily locked due to multiple failed attempts")
                )
            }

            // Step 2: Verify email matches registration
            if (!voterRepository.verifyLoginEmail(email)) {
                incrementFailedAttempts()
                return@withContext Result.failure(
                    SecurityException("Email does not match registration email")
                )
            }

            // Step 3: Attempt login
            val request = LoginRequest(email = email, password = password)
            Log.d(TAG, "Attempting secure login for: $email")

            val response = apiService.loginUser(request)

            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // Reset failed attempts on successful login
                    resetFailedAttempts()

                    // Store session data securely
                    storeSecureSession(
                        token = loginResponse.data?.token ?: "",
                        email = email,
                        userId = null,
                        refreshToken = null
                    )

                    // Store password hash for future verification
                    storePasswordHash(password)

                    // Fetch voter data
                    val voterResult = voterRepository.fetchVoterData(loginResponse.data?.token ?: "")

                    return@withContext Result.success(
                        LoginResult(
                            apiResponse = loginResponse,
                            voterDataFetched = voterResult.isSuccess,
                            sessionExpiry = System.currentTimeMillis() + SESSION_DURATION
                        )
                    )
                } ?: run {
                    incrementFailedAttempts()
                    return@withContext Result.failure(Exception("Empty response from server"))
                }
            } else {
                incrementFailedAttempts()
                val errorMessage = when (response.code()) {
                    401 -> "Invalid email or password"
                    403 -> "Account access denied"
                    429 -> "Too many requests. Please try again later"
                    else -> "Login failed: ${response.message()}"
                }
                return@withContext Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            incrementFailedAttempts()
            return@withContext Result.failure(e)
        }
    }

    /**
     * Verify user password for sensitive operations
     */
    suspend fun verifyUserPassword(password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val storedHash = encryptedSharedPreferences.getString(KEY_USER_PASSWORD_HASH, null)
                ?: return@withContext false

            val inputHash = hashPassword(password)
            return@withContext storedHash == inputHash
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying password", e)
            return@withContext false
        }
    }

    /**
     * Store secure session data
     */
    private fun storeSecureSession(
        token: String,
        email: String,
        userId: String?,
        refreshToken: String?
    ) {
        val sessionExpiry = System.currentTimeMillis() + SESSION_DURATION

        with(encryptedSharedPreferences.edit()) {
            putString(KEY_USER_TOKEN, token)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ID, userId)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            putLong(KEY_SESSION_EXPIRY, sessionExpiry)
            apply()
        }

        Log.d(TAG, "Secure session stored for: $email")
    }

    /**
     * Store password hash for verification
     */
    private fun storePasswordHash(password: String) {
        val hash = hashPassword(password)
        encryptedSharedPreferences.edit()
            .putString(KEY_USER_PASSWORD_HASH, hash)
            .apply()
    }

    /**
     * Hash password using SHA-256
     */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Check if session is valid
     */
    fun isSessionValid(): Boolean {
        val expiry = encryptedSharedPreferences.getLong(KEY_SESSION_EXPIRY, 0)
        return System.currentTimeMillis() < expiry
    }

    /**
     * Get complete user session with validation
     */
    fun getCompleteUserSession(): UserSession? {
        if (!isSessionValid()) {
            Log.w(TAG, "Session expired")
            clearStoredData()
            return null
        }

        return try {
            val token = encryptedSharedPreferences.getString(KEY_USER_TOKEN, null)
            val email = encryptedSharedPreferences.getString(KEY_USER_EMAIL, null)
            val userId = encryptedSharedPreferences.getString(KEY_USER_ID, null)
            val refreshToken = encryptedSharedPreferences.getString(KEY_REFRESH_TOKEN, null)
            val loginTimestamp = encryptedSharedPreferences.getLong(KEY_LOGIN_TIMESTAMP, 0)
            val sessionExpiry = encryptedSharedPreferences.getLong(KEY_SESSION_EXPIRY, 0)

            if (token != null && email != null) {
                UserSession(
                    token = token,
                    email = email,
                    userId = userId,
                    refreshToken = refreshToken,
                    loginTimestamp = loginTimestamp,
                    sessionExpiry = sessionExpiry
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user session", e)
            null
        }
    }

    /**
     * Increment failed login attempts
     */
    private fun incrementFailedAttempts() {
        val currentAttempts = encryptedSharedPreferences.getInt(KEY_FAILED_LOGIN_ATTEMPTS, 0)
        with(encryptedSharedPreferences.edit()) {
            putInt(KEY_FAILED_LOGIN_ATTEMPTS, currentAttempts + 1)
            putLong(KEY_LAST_FAILED_ATTEMPT, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Reset failed login attempts
     */
    private fun resetFailedAttempts() {
        with(encryptedSharedPreferences.edit()) {
            putInt(KEY_FAILED_LOGIN_ATTEMPTS, 0)
            putLong(KEY_LAST_FAILED_ATTEMPT, 0)
            apply()
        }
    }

    /**
     * Check if account is locked due to failed attempts
     */
    private fun isAccountLocked(): Boolean {
        val attempts = encryptedSharedPreferences.getInt(KEY_FAILED_LOGIN_ATTEMPTS, 0)
        if (attempts < MAX_LOGIN_ATTEMPTS) {
            return false
        }

        val lastAttempt = encryptedSharedPreferences.getLong(KEY_LAST_FAILED_ATTEMPT, 0)
        val timeSinceLastAttempt = System.currentTimeMillis() - lastAttempt

        return if (timeSinceLastAttempt < LOCKOUT_DURATION) {
            true
        } else {
            // Reset attempts after lockout period
            resetFailedAttempts()
            false
        }
    }

    /**
     * Clear all stored data (for logout)
     */
    fun clearStoredData() {
        encryptedSharedPreferences.edit().clear().apply()
        Log.d(TAG, "All stored data cleared")
    }

    /**
     * Get user email
     */
    fun getUserEmail(): String? {
        return if (isSessionValid()) {
            encryptedSharedPreferences.getString(KEY_USER_EMAIL, null)
        } else {
            null
        }
    }

    /**
     * Get user token
     */
    fun getUserToken(): String? {
        return if (isSessionValid()) {
            encryptedSharedPreferences.getString(KEY_USER_TOKEN, null)
        } else {
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
        if (token?.isNotEmpty() ?: false && expiresAt.isNotEmpty()) {
            try {
                // Simple expiry check - in a real app you might want to parse the date
                // and check if it's in the future
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Error checking token expiry: ${e.message}")
            }
        }
        return token?.isNotEmpty() ?: false
    }

    /**
     * Get token expiry time
     */
    private fun getUserTokenExpiry(): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USER_EXPIRES_AT, "") ?: ""
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
     * Get user data summary for debugging
     */
    fun getUserDataSummary(): Map<String, String> {
        return mapOf(
            "hasToken" to (getUserToken()?.isNotEmpty() ?: false).toString(),
            "email" to (getUserEmail() ?: ""),
            "expiresAt" to getUserTokenExpiry(),
            "isLoggedIn" to isUserLoggedIn().toString(),
            "isExpired" to isTokenExpired().toString()
        )
    }

    // Data classes
    data class LoginResult(
        val apiResponse: ApiResponse<UserLoginData>,
        val voterDataFetched: Boolean,
        val sessionExpiry: Long
    )

    data class UserSession(
        val token: String,
        val email: String,
        val userId: String? = null,
        val refreshToken: String? = null,
        val loginTimestamp: Long = 0,
        val sessionExpiry: Long = 0
    )
}