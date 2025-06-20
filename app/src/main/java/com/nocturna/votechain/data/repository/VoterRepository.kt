package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.model.VoterData
import com.nocturna.votechain.data.model.WalletInfo
import com.nocturna.votechain.data.network.NetworkClient
import com.nocturna.votechain.security.CryptoKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Updated VoterRepository with integrated cryptographic key management
 */
class VoterRepository(private val context: Context) {
    private val TAG = "VoterRepository"
    private val PREFS_NAME = "VoteChainPrefs"
    private val KEY_VOTER_FULL_NAME = "voter_full_name"
    private val KEY_VOTER_NIK = "voter_nik"
    private val KEY_VOTER_PUBLIC_KEY = "voter_public_key"
    private val KEY_VOTER_ADDRESS = "voter_address"
    private val KEY_VOTER_HAS_VOTED = "voter_has_voted"
    private val KEY_USER_ID = "user_id"

    // API service and crypto manager
    private val apiService = NetworkClient.apiService
    private val cryptoKeyManager = CryptoKeyManager(context)

    /**
     * Fetch voter data from API and merge with locally stored cryptographic keys
     */
    suspend fun fetchVoterData(userToken: String): Result<VoterData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching voter data with token")

            val formattedToken = if (userToken.startsWith("Bearer ")) {
                userToken
            } else {
                "Bearer $userToken"
            }

            val response = apiService.getVoterDataWithToken(formattedToken)

            if (response.isSuccessful) {
                response.body()?.let { voterResponse ->
                    if (voterResponse.data.isNotEmpty()) {
                        val userId = getUserIdFromResponse(response.headers().get("x-user-id"))
                        Log.d(TAG, "Looking for voter data with user_id: $userId")

                        val voterData = if (!userId.isNullOrEmpty()) {
                            voterResponse.data.find { it.user_id == userId }
                                ?: voterResponse.data.first()
                        } else {
                            voterResponse.data.first()
                        }

                        // Save user ID for future reference
                        saveUserId(userId ?: voterData.user_id)

                        // Merge API data with locally stored cryptographic keys
                        val enhancedVoterData = mergeWithStoredKeys(voterData)

                        // Save merged data locally
                        saveVoterDataLocally(enhancedVoterData)

                        Log.d(TAG, "Voter data fetched and merged successfully")
                        Result.success(enhancedVoterData)
                    } else {
                        Log.e(TAG, "No voter data found in response")
                        Result.failure(Exception("No voter data found"))
                    }
                } ?: run {
                    Log.e(TAG, "Response body is null")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = "API call failed with code: ${response.code()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching voter data: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Merge API voter data with locally stored cryptographic keys
     */
    private fun mergeWithStoredKeys(apiVoterData: VoterData): VoterData {
        return try {
            // Get stored voter address from crypto manager
            val storedVoterAddress = cryptoKeyManager.getVoterAddress()

            // Use stored voter address if available, otherwise use API data
            val voterAddress = if (!storedVoterAddress.isNullOrEmpty()) {
                Log.d(TAG, "Using stored voter address: $storedVoterAddress")
                storedVoterAddress
            } else {
                Log.d(TAG, "Using API voter address: ${apiVoterData.voter_address}")
                apiVoterData.voter_address
            }

            apiVoterData.copy(voter_address = voterAddress)
        } catch (e: Exception) {
            Log.e(TAG, "Error merging stored keys, using API data as-is", e)
            apiVoterData
        }
    }

    /**
     * Get user ID from response header
     */
    private fun getUserIdFromResponse(headerValue: String?): String? {
        return headerValue?.takeIf { it.isNotEmpty() }?.also {
            Log.d(TAG, "Found user_id in header: $it")
        }
    }

    /**
     * Save user_id to SharedPreferences
     */
    private fun saveUserId(userId: String?) {
        if (userId != null) {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString(KEY_USER_ID, userId)
                apply()
            }
            Log.d(TAG, "User ID saved: $userId")
        }
    }

    /**
     * Save voter data to SharedPreferences (without private key for security)
     */
    private fun saveVoterDataLocally(voterData: VoterData) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_VOTER_FULL_NAME, voterData.full_name)
            putString(KEY_VOTER_NIK, voterData.nik)
            putString(KEY_VOTER_PUBLIC_KEY, cryptoKeyManager.getPublicKey() ?: "")
            putString(KEY_VOTER_ADDRESS, voterData.voter_address)
            putBoolean(KEY_VOTER_HAS_VOTED, voterData.has_voted)
            apply()
        }
        Log.d(TAG, "Voter data saved to SharedPreferences")
    }

    /**
     * Get voter data from local storage with cryptographic key integration
     */
    fun getVoterDataLocally(): VoterData? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val fullName = sharedPreferences.getString(KEY_VOTER_FULL_NAME, "") ?: ""
        val nik = sharedPreferences.getString(KEY_VOTER_NIK, "") ?: ""
        val storedVoterAddress = sharedPreferences.getString(KEY_VOTER_ADDRESS, "") ?: ""
        val hasVoted = sharedPreferences.getBoolean(KEY_VOTER_HAS_VOTED, false)

        // Try to get voter address from crypto manager if not in regular storage
        val voterAddress = if (storedVoterAddress.isNotEmpty()) {
            storedVoterAddress
        } else {
            cryptoKeyManager.getVoterAddress() ?: ""
        }

        return if (fullName.isNotEmpty()) {
            VoterData(
                id = "",
                user_id = "",
                nik = nik,
                full_name = fullName,
                gender = "",
                birth_place = "",
                birth_date = "",
                residential_address = "",
                voter_address = voterAddress,
                region = "",
                is_registered = true,
                has_voted = hasVoted
            )
        } else {
            null
        }
    }

    /**
     * Get wallet information with secure private key access
     */
    fun getWalletInfo(): WalletInfo {
        return WalletInfo(
            balance = "0.0000", // TODO: Implement balance checking from blockchain
            privateKey = "", // Private key not exposed here for security
            publicKey = cryptoKeyManager.getPublicKey() ?: ""
        )
    }

    /**
     * Get wallet information including private key (use only when absolutely necessary)
     * This method should only be called when private key is explicitly needed for transactions
     */
    fun getWalletInfoWithPrivateKey(): WalletInfo {
        return WalletInfo(
            balance = "0.0000",
            privateKey = cryptoKeyManager.getPrivateKey() ?: "",
            publicKey = cryptoKeyManager.getPublicKey() ?: ""
        )
    }

    /**
     * Get only the private key (securely decrypted from Android Keystore)
     */
    fun getPrivateKey(): String? {
        return try {
            val privateKey = cryptoKeyManager.getPrivateKey()
            if (privateKey != null) {
                Log.d(TAG, "Private key retrieved successfully")
            } else {
                Log.w(TAG, "Private key not found")
            }
            privateKey
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving private key", e)
            null
        }
    }

    /**
     * Get only the public key
     */
    fun getPublicKey(): String? {
        return cryptoKeyManager.getPublicKey()
    }

    /**
     * Get only the voter address
     */
    fun getVoterAddress(): String? {
        return cryptoKeyManager.getVoterAddress()
    }

    /**
     * Check if cryptographic keys are stored and accessible
     */
    fun hasStoredKeys(): Boolean {
        return cryptoKeyManager.hasStoredKeyPair()
    }

    /**
     * Clear voter data from local storage and secure keystore
     */
    fun clearVoterData() {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(KEY_VOTER_FULL_NAME)
            remove(KEY_VOTER_NIK)
            remove(KEY_VOTER_PUBLIC_KEY)
            remove(KEY_VOTER_ADDRESS)
            remove(KEY_VOTER_HAS_VOTED)
            remove(KEY_USER_ID)
            apply()
        }

        // Also clear cryptographic keys from secure storage
        cryptoKeyManager.clearStoredKeys()

        Log.d(TAG, "Voter data and cryptographic keys cleared from all storage")
    }

    /**
     * Check if voter data is available locally
     */
    fun hasStoredVoterData(): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString(KEY_VOTER_FULL_NAME, null)
        val nik = sharedPreferences.getString(KEY_VOTER_NIK, null)
        val hasKeys = cryptoKeyManager.hasStoredKeyPair()

        return fullName != null && nik != null && hasKeys
    }

    /**
     * Update voting status locally
     */
    fun updateVotingStatus(hasVoted: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(KEY_VOTER_HAS_VOTED, hasVoted)
            apply()
        }
        Log.d(TAG, "Voting status updated: $hasVoted")
    }

    /**
     * Get voting status from local storage
     */
    fun getVotingStatus(): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_VOTER_HAS_VOTED, false)
    }

    /**
     * Validate that all stored voter data and keys are consistent and accessible
     */
    fun validateStoredData(): Boolean {
        return try {
            val hasVoterData = hasStoredVoterData()
            val hasValidKeys = cryptoKeyManager.hasStoredKeyPair()
            val canAccessPrivateKey = cryptoKeyManager.getPrivateKey() != null
            val canAccessPublicKey = cryptoKeyManager.getPublicKey() != null
            val canAccessVoterAddress = cryptoKeyManager.getVoterAddress() != null

            val isValid = hasVoterData && hasValidKeys && canAccessPrivateKey &&
                    canAccessPublicKey && canAccessVoterAddress

            Log.d(TAG, "Data validation result: $isValid")
            if (!isValid) {
                Log.w(TAG, "Validation details - VoterData: $hasVoterData, Keys: $hasValidKeys, " +
                        "PrivateKey: $canAccessPrivateKey, PublicKey: $canAccessPublicKey, " +
                        "VoterAddress: $canAccessVoterAddress")
            }

            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Error validating stored data", e)
            false
        }
    }

    /**
     * Backup essential voter information (excluding private key for security)
     * @return Map containing backed up voter data
     */
    fun backupVoterData(): Map<String, String> {
        val backupData = mutableMapOf<String, String>()

        try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // Backup basic voter information
            sharedPreferences.getString(KEY_VOTER_FULL_NAME, "")?.let {
                if (it.isNotEmpty()) backupData["full_name"] = it
            }
            sharedPreferences.getString(KEY_VOTER_NIK, "")?.let {
                if (it.isNotEmpty()) backupData["nik"] = it
            }

            // Backup public key and voter address (safe to backup)
            cryptoKeyManager.getPublicKey()?.let {
                if (it.isNotEmpty()) backupData["public_key"] = it
            }
            cryptoKeyManager.getVoterAddress()?.let {
                if (it.isNotEmpty()) backupData["voter_address"] = it
            }

            // Backup voting status
            backupData["has_voted"] = sharedPreferences.getBoolean(KEY_VOTER_HAS_VOTED, false).toString()

            Log.d(TAG, "Voter data backup created (${backupData.size} items)")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating backup", e)
        }

        return backupData
    }

    /**
     * Restore voter data from backup (excluding private key)
     * Note: This only restores non-sensitive data. Private key must be regenerated or imported separately
     */
    fun restoreVoterDataFromBackup(backupData: Map<String, String>): Boolean {
        return try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            with(sharedPreferences.edit()) {
                backupData["full_name"]?.let { putString(KEY_VOTER_FULL_NAME, it) }
                backupData["nik"]?.let { putString(KEY_VOTER_NIK, it) }
                backupData["public_key"]?.let { putString(KEY_VOTER_PUBLIC_KEY, it) }
                backupData["voter_address"]?.let { putString(KEY_VOTER_ADDRESS, it) }
                backupData["has_voted"]?.let {
                    putBoolean(KEY_VOTER_HAS_VOTED, it.toBoolean())
                }
                apply()
            }

            Log.d(TAG, "Voter data restored from backup")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring from backup", e)
            false
        }
    }
}