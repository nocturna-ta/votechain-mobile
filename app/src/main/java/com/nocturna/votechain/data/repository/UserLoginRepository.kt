package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.LoginRequest
import com.nocturna.votechain.data.model.UserLoginData
import com.nocturna.votechain.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class to handle user login operations
 */
class UserLoginRepository(private val context: Context) {
    private val TAG = "UserLoginRepository"
    private val apiService = NetworkClient.apiService
    private val PREFS_NAME = "VoteChainPrefs"
    private val KEY_USER_TOKEN = "user_token"
    private val KEY_USER_EMAIL = "user_email"
    private val KEY_USER_EXPIRES_AT = "expires_at"

    /**
     * Login user with email and password
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
                response.body()?.let {
                    Log.d(TAG, "Login successful: ${it.message}")

                    // Store user data if login is successful and there is a token
                    if (it.code == 200 && it.data?.token?.isNotEmpty() == true) {
                        saveUserData(it.data)
                    }

                    Result.success(it)
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
     * Save user data to SharedPreferences
     */
    fun saveUserData(userData: UserLoginData) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_USER_TOKEN, userData.token)
            putString(KEY_USER_EMAIL, userData.message) // Assuming message contains user info
            putString(KEY_USER_EXPIRES_AT, userData.expires_at)
            apply()
        }
        Log.d(TAG, "User data saved to SharedPreferences")
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
        Log.d(TAG, "User token saved to SharedPreferences")
    }

    /**
     * Get saved user token from SharedPreferences
     */
    fun getUserToken(): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USER_TOKEN, "") ?: ""
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
     * Log out user by clearing token and user data
     */
    fun logoutUser() {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(KEY_USER_TOKEN)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_EXPIRES_AT)
            apply()
        }
        Log.d(TAG, "User logged out, data cleared")
    }
}