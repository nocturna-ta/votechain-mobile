package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.nocturna.votechain.blockchain.BlockchainManager
import com.nocturna.votechain.data.model.VoterData
import com.nocturna.votechain.data.model.WalletInfo
import com.nocturna.votechain.data.model.AccountDisplayData
import com.nocturna.votechain.data.network.NetworkClient
import com.nocturna.votechain.security.CryptoKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enhanced VoterRepository with integrated cryptographic key management and real-time balance fetching
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
    private val KEY_REGISTRATION_EMAIL = "registration_email"
    private val KEY_LAST_BALANCE_UPDATE = "last_balance_update"
    private val KEY_CACHED_BALANCE = "cached_balance"

    // API service and crypto manager
    private val apiService = NetworkClient.apiService
    private val cryptoKeyManager = CryptoKeyManager(context)
    private val userLoginRepository = UserLoginRepository(context)


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

                        // Merge with locally stored cryptographic data
                        val enhancedVoterData = voterData.copy(
                            voter_address = cryptoKeyManager.getVoterAddress() ?: voterData.voter_address
                        )

                        // Save voter data locally for offline access
                        saveVoterDataLocally(enhancedVoterData)

                        Log.d(TAG, "Voter data fetched and enhanced successfully")
                        return@withContext Result.success(enhancedVoterData)
                    } else {
                        Log.w(TAG, "Empty voter data received")
                        return@withContext Result.failure(Exception("No voter data found"))
                    }
                } ?: run {
                    Log.e(TAG, "Null response body")
                    return@withContext Result.failure(Exception("Invalid response"))
                }
            } else {
                Log.e(TAG, "API call failed: ${response.code()} - ${response.message()}")
                return@withContext Result.failure(Exception("API call failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching voter data", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Verify login email matches registration email
     */
    fun verifyLoginEmail(loginEmail: String): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val registrationEmail = sharedPreferences.getString(KEY_REGISTRATION_EMAIL, null)

        if (registrationEmail == null) {
            Log.w(TAG, "No registration email found")
            return true // Allow login if no registration email stored (backward compatibility)
        }

        val matches = registrationEmail.equals(loginEmail, ignoreCase = true)
        if (!matches) {
            Log.w(TAG, "Login email mismatch: expected $registrationEmail, got $loginEmail")
        }

        return matches
    }

    /**
     * Extract user_id from response header
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
     * Get complete wallet information with real-time balance fetching
     */
    suspend fun getCompleteWalletInfo(): WalletInfo = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching complete wallet information")

            val voterAddress = cryptoKeyManager.getVoterAddress()
            val publicKey = cryptoKeyManager.getPublicKey()
            val privateKey = cryptoKeyManager.getPrivateKey()

            if (voterAddress.isNullOrEmpty()) {
                Log.w(TAG, "No voter address found")
                return@withContext WalletInfo(
                    hasError = true,
                    errorMessage = "No voter address found"
                )
            }

            // Try to get fresh balance from blockchain
            val currentBalance = try {
                Log.d(TAG, "Fetching balance for address: $voterAddress")
                val balance = BlockchainManager.getAccountBalance(voterAddress)

                // Cache the balance with timestamp
                cacheBalance(balance)

                Log.d(TAG, "Balance fetched successfully: $balance ETH")
                balance
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch live balance, using cached: ${e.message}")
                getCachedBalance()
            }

            return@withContext WalletInfo(
                balance = currentBalance,
                privateKey = privateKey ?: "",
                publicKey = publicKey ?: "",
                voterAddress = voterAddress,
                lastUpdated = System.currentTimeMillis(),
                isLoading = false,
                hasError = false
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error getting complete wallet info", e)
            return@withContext WalletInfo(
                balance = getCachedBalance(),
                hasError = true,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Refresh balance from blockchain
     */
    suspend fun refreshBalance(): String = withContext(Dispatchers.IO) {
        try {
            val voterAddress = cryptoKeyManager.getVoterAddress()
            if (voterAddress.isNullOrEmpty()) {
                return@withContext "0.00000000"
            }

            val balance = BlockchainManager.getAccountBalance(voterAddress)
            cacheBalance(balance)
            return@withContext balance
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing balance", e)
            return@withContext getCachedBalance()
        }
    }

    /**
     * Get account display data for UI
     */
    suspend fun getAccountDisplayData(): AccountDisplayData = withContext(Dispatchers.IO) {
        try {
            val voterData = getVoterDataLocally()
            val walletInfo = getCompleteWalletInfo()

            return@withContext AccountDisplayData(
                fullName = voterData?.full_name ?: "N/A",
                nik = voterData?.nik ?: "N/A",
                email = "", // This should come from user profile
                ethBalance = walletInfo.balance,
                publicKey = walletInfo.publicKey,
                privateKey = walletInfo.privateKey,
                voterAddress = walletInfo.voterAddress,
                hasVoted = voterData?.has_voted ?: false,
                isDataLoading = false,
                errorMessage = if (walletInfo.hasError) walletInfo.errorMessage else null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting account display data", e)
            return@withContext AccountDisplayData(
                isDataLoading = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Cache balance with timestamp
     */
    private fun cacheBalance(balance: String) {
        try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString(KEY_CACHED_BALANCE, balance)
                putLong(KEY_LAST_BALANCE_UPDATE, System.currentTimeMillis())
                apply()
            }
            Log.d(TAG, "Balance cached: $balance")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching balance", e)
        }
    }

    /**
     * Get cached balance
     */
    private fun getCachedBalance(): String {
        return try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val cachedBalance = sharedPreferences.getString(KEY_CACHED_BALANCE, "0.00000000") ?: "0.00000000"
            val lastUpdate = sharedPreferences.getLong(KEY_LAST_BALANCE_UPDATE, 0)

            // Check if cache is not too old (5 minutes)
            val cacheAge = System.currentTimeMillis() - lastUpdate
            if (cacheAge < 5 * 60 * 1000) {
                Log.d(TAG, "Using cached balance: $cachedBalance")
                cachedBalance
            } else {
                Log.d(TAG, "Cache expired, returning default balance")
                "0.00000000"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached balance", e)
            "0.00000000"
        }
    }

    /**
     * Get wallet information (legacy method, now with real balance)
     */
    suspend fun getWalletInfo(): WalletInfo {
        return getCompleteWalletInfo()
    }

    /**
     * Get wallet information including private key (use only when absolutely necessary)
     * This method should only be called when private key is explicitly needed for transactions
     */
    suspend fun getWalletInfoWithPrivateKey(): WalletInfo {
        return getCompleteWalletInfo()
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
            remove(KEY_CACHED_BALANCE)
            remove(KEY_LAST_BALANCE_UPDATE)
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
     * Validate stored data integrity
     */
    fun validateStoredData(): Boolean {
        return try {
            val hasVoterData = hasStoredVoterData()
            val hasValidKeys = cryptoKeyManager.hasStoredKeyPair()
            val voterAddress = cryptoKeyManager.getVoterAddress()

            hasVoterData && hasValidKeys && !voterAddress.isNullOrEmpty()
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