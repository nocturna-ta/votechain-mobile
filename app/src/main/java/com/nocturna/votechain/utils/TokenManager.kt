package com.nocturna.votechain.utils

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.repository.UserLoginRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class untuk mengelola JWT token dan validasi expiry
 */
object TokenManager {
    private const val TAG = "TokenManager"

    /**
     * Check if token is expired
     */
    fun isTokenExpired(context: Context): Boolean {
        val userLoginRepository = UserLoginRepository(context)
        val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
        val expiresAt = sharedPreferences.getString("expires_at", "") ?: ""

        if (expiresAt.isEmpty()) {
            Log.w(TAG, "No token expiry found")
            return true
        }

        return try {
            // Parse the expiry time (format might be Unix timestamp or ISO format)
            val expiryTime = if (expiresAt.toLongOrNull() != null) {
                // Unix timestamp
                Date(expiresAt.toLong() * 1000)
            } else {
                // Try to parse as ISO format
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(expiresAt)
            }

            val currentTime = Date()
            val isExpired = expiryTime?.before(currentTime) ?: true

            Log.d(TAG, "Token expiry check - Current: $currentTime, Expiry: $expiryTime, Expired: $isExpired")
            isExpired
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing token expiry: ${e.message}", e)
            true
        }
    }

    /**
     * Check if user needs to re-login
     */
    fun shouldForceLogin(context: Context): Boolean {
        val userLoginRepository = UserLoginRepository(context)
        val token = userLoginRepository.getUserToken()

        return when {
            token.isEmpty() -> {
                Log.d(TAG, "No token found, force login required")
                true
            }
            isTokenExpired(context) -> {
                Log.d(TAG, "Token expired, force login required")
                true
            }
            else -> {
                Log.d(TAG, "Token valid, no force login required")
                false
            }
        }
    }

    /**
     * Clear expired token and related data
     */
    fun clearExpiredSession(context: Context) {
        if (isTokenExpired(context)) {
            Log.d(TAG, "Clearing expired session")
            val userLoginRepository = UserLoginRepository(context)
            userLoginRepository.logoutUser()
        }
    }

    /**
     * Get time until token expires in minutes
     */
    fun getTimeUntilExpiry(context: Context): Long {
        val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
        val expiresAt = sharedPreferences.getString("expires_at", "") ?: ""

        if (expiresAt.isEmpty()) return 0

        return try {
            val expiryTime = if (expiresAt.toLongOrNull() != null) {
                Date(expiresAt.toLong() * 1000)
            } else {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(expiresAt)
            }

            val currentTime = Date()
            val diffInMs = (expiryTime?.time ?: 0) - currentTime.time
            diffInMs / (1000 * 60) // Convert to minutes
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating time until expiry: ${e.message}", e)
            0
        }
    }
}