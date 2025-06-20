package com.nocturna.votechain.utils

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.network.ElectionNetworkClient

/**
 * Utility class to handle token synchronization between different storage systems
 */
object TokenSyncUtil {
    private const val TAG = "TokenSyncUtil"

    /**
     * Sync token from ElectionNetworkClient to TokenManager
     * This ensures both systems have the same token
     */
    fun syncTokenToTokenManager(context: Context, tokenManager: TokenManager): Boolean {
        return try {
            val electionToken = ElectionNetworkClient.getUserToken()

            if (electionToken.isNotEmpty()) {
                Log.d(TAG, "Syncing token from ElectionNetworkClient to TokenManager")
                tokenManager.saveAccessToken(electionToken)
                Log.d(TAG, "✅ Token sync successful")
                true
            } else {
                Log.w(TAG, "No token found in ElectionNetworkClient to sync")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing token", e)
            false
        }
    }

    /**
     * Sync token from TokenManager to ElectionNetworkClient
     */
    fun syncTokenToElectionClient(tokenManager: TokenManager): Boolean {
        return try {
            val token = tokenManager.getAccessToken()

            if (!token.isNullOrEmpty()) {
                Log.d(TAG, "Syncing token from TokenManager to ElectionNetworkClient")
                ElectionNetworkClient.saveUserToken(token)
                Log.d(TAG, "✅ Token sync successful")
                true
            } else {
                Log.w(TAG, "No token found in TokenManager to sync")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing token", e)
            false
        }
    }

    /**
     * Get token from any available source and sync to both systems
     * Returns the token if found, null otherwise
     */
    fun getAndSyncToken(context: Context, tokenManager: TokenManager): String? {
        return try {
            // First try TokenManager
            var token = tokenManager.getAccessToken()

            if (!token.isNullOrEmpty()) {
                Log.d(TAG, "Token found in TokenManager, syncing to ElectionNetworkClient")
                ElectionNetworkClient.saveUserToken(token)
                return token
            }

            // Try ElectionNetworkClient
            token = ElectionNetworkClient.getUserToken()
            if (token.isNotEmpty()) {
                Log.d(TAG, "Token found in ElectionNetworkClient, syncing to TokenManager")
                tokenManager.saveAccessToken(token)
                return token
            }

            Log.w(TAG, "No token found in any storage system")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting and syncing token", e)
            null
        }
    }

    /**
     * Save token to both storage systems simultaneously
     */
    fun saveTokenToBothSystems(token: String, tokenManager: TokenManager, expiryTimeInMillis: Long = 0) {
        try {
            Log.d(TAG, "Saving token to both storage systems")

            // Save to TokenManager
            tokenManager.saveAccessToken(token, expiryTimeInMillis)

            // Save to ElectionNetworkClient
            ElectionNetworkClient.saveUserToken(token)

            Log.d(TAG, "✅ Token saved to both systems successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving token to both systems", e)
        }
    }

    /**
     * Clear tokens from both storage systems
     */
    fun clearTokensFromBothSystems(context: Context, tokenManager: TokenManager) {
        try {
            Log.d(TAG, "Clearing tokens from both storage systems")

            // Clear from TokenManager
            tokenManager.clearTokens()

            // Clear from ElectionNetworkClient
            ElectionNetworkClient.clearUserToken()

            Log.d(TAG, "✅ Tokens cleared from both systems")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing tokens", e)
        }
    }

    /**
     * Check if user is authenticated in any system
     */
    fun isUserAuthenticated(context: Context, tokenManager: TokenManager): Boolean {
        val tokenManagerHasToken = tokenManager.isLoggedIn()
        val electionClientHasToken = ElectionNetworkClient.hasValidToken()

        Log.d(TAG, "Authentication status - TokenManager: $tokenManagerHasToken, ElectionClient: $electionClientHasToken")

        return tokenManagerHasToken || electionClientHasToken
    }

    /**
     * Validate and sync tokens between systems
     * Call this during app initialization or login
     */
    fun validateAndSyncTokens(context: Context, tokenManager: TokenManager): Boolean {
        return try {
            Log.d(TAG, "Validating and syncing tokens")

            val tmToken = tokenManager.getAccessToken()
            val ecToken = ElectionNetworkClient.getUserToken()

            Log.d(TAG, "TokenManager has token: ${!tmToken.isNullOrEmpty()}")
            Log.d(TAG, "ElectionClient has token: ${ecToken.isNotEmpty()}")

            when {
                // Both have tokens - verify they match
                !tmToken.isNullOrEmpty() && ecToken.isNotEmpty() -> {
                    if (tmToken == ecToken) {
                        Log.d(TAG, "✅ Tokens match in both systems")
                        true
                    } else {
                        Log.w(TAG, "⚠️ Token mismatch detected, using TokenManager token")
                        ElectionNetworkClient.saveUserToken(tmToken)
                        true
                    }
                }

                // Only TokenManager has token
                !tmToken.isNullOrEmpty() && ecToken.isEmpty() -> {
                    Log.d(TAG, "Syncing token from TokenManager to ElectionClient")
                    ElectionNetworkClient.saveUserToken(tmToken)
                    true
                }

                // Only ElectionClient has token
                tmToken.isNullOrEmpty() && ecToken.isNotEmpty() -> {
                    Log.d(TAG, "Syncing token from ElectionClient to TokenManager")
                    tokenManager.saveAccessToken(ecToken)
                    true
                }

                // Neither has token
                else -> {
                    Log.d(TAG, "No tokens found in either system")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating and syncing tokens", e)
            false
        }
    }
}