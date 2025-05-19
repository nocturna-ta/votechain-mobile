package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.nocturna.votechain.data.network.ApiResponse
import com.nocturna.votechain.data.network.LoginRequest
import com.nocturna.votechain.data.network.NetworkClient
import com.nocturna.votechain.data.network.UserLoginData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class to handle user login operations
 */
class UserLoginRepository(private val context: Context) {
    private val TAG = "UserLoginRepository"
    private val apiService = NetworkClient.apiService

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

                    // Here you can store the token and user data in SharedPreferences
                    // for use throughout the app
                    saveUserToken(it.data?.token ?: "")

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
                } catch (e: Exception) {
                    Log.e(TAG, "Could not parse error JSON: ${e.message}")
                }

                Result.failure(Exception("Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during login", e)
            Result.failure(e)
        }
    }

    /**
     * Save user token to SharedPreferences
     */
    private fun saveUserToken(token: String) {
        val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("user_token", token)
            apply()
        }
        Log.d(TAG, "User token saved to SharedPreferences")
    }

    /**
     * Get saved user token from SharedPreferences
     */
    fun getUserToken(): String {
        val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_token", "") ?: ""
    }

    /**
     * Check if user is logged in (has a token)
     */
    fun isUserLoggedIn(): Boolean {
        return getUserToken().isNotEmpty()
    }

    /**
     * Log out user by clearing token
     */
    fun logoutUser() {
        val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("user_token")
            apply()
        }
        Log.d(TAG, "User logged out, token cleared")
    }
}