package com.nocturna.votechain.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class TokenManager(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "VoteChainTokenPrefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val TOKEN_EXPIRY_KEY = "token_expiry"
        private const val TAG = "TokenManager"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get access token
     */
    fun getAccessToken(): String? {
        val token = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)

        // Check if token is expired
        if (token != null && isTokenExpired()) {
            Log.w(TAG, "Access token is expired")
            clearTokens()
            return null
        }

        return token
    }

    /**
     * Save access token
     */
    fun saveAccessToken(token: String, expiryTimeInMillis: Long = 0) {
        with(sharedPreferences.edit()) {
            putString(ACCESS_TOKEN_KEY, token)
            if (expiryTimeInMillis > 0) {
                putLong(TOKEN_EXPIRY_KEY, expiryTimeInMillis)
            }
            apply()
        }
        Log.d(TAG, "Access token saved")
    }

    /**
     * Get refresh token
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }

    /**
     * Save refresh token
     */
    fun saveRefreshToken(token: String) {
        with(sharedPreferences.edit()) {
            putString(REFRESH_TOKEN_KEY, token)
            apply()
        }
        Log.d(TAG, "Refresh token saved")
    }

    /**
     * Check if token is expired
     */
    private fun isTokenExpired(): Boolean {
        val expiryTime = sharedPreferences.getLong(TOKEN_EXPIRY_KEY, 0)
        if (expiryTime == 0L) {
            // No expiry set, assume token is valid
            return false
        }
        return System.currentTimeMillis() > expiryTime
    }

    /**
     * Clear all tokens
     */
    fun clearTokens() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        Log.d(TAG, "All tokens cleared")
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        val token = getAccessToken()
        return !token.isNullOrEmpty()
    }
}
