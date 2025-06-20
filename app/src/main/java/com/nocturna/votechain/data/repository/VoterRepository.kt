package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.model.VoterData
import com.nocturna.votechain.data.model.VoterWalletInfo
import com.nocturna.votechain.data.model.WalletInfo
import com.nocturna.votechain.data.network.NetworkClient
import com.nocturna.votechain.data.network.NetworkClient.apiService
import com.nocturna.votechain.data.network.VoterApiService
import com.nocturna.votechain.data.storage.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.utils.Convert

/**
 * Enhanced VoterRepository with integrated wallet functionality
 */
class VoterRepository(private val context: Context) {
    private val TAG = "VoterRepository"
    private val PREFS_NAME = "VoteChainPrefs"
    private val KEY_VOTER_DATA = "voter_data"
    private val KEY_VOTER_FULL_NAME = "voter_full_name"
    private val KEY_VOTER_NIK = "voter_nik"
    private val KEY_VOTER_PUBLIC_KEY = "voter_public_key"
    private val KEY_VOTER_PRIVATE_KEY = "voter_private_key"
    private val KEY_VOTER_HAS_VOTED = "voter_has_voted"
    private val KEY_USER_ID = "user_id"

    private val walletManager = WalletManager.getInstance(context)
    private val enhancedUserRepository by lazy { EnhancedUserRepository(context) }

    /**
     * Fetch voter data from API and save to local storage
     */
    suspend fun fetchVoterData(userToken: String): Result<VoterData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching voter data with token")

            // Format token properly if it doesn't already have Bearer prefix
            val formattedToken = if (userToken.startsWith("Bearer ")) {
                userToken
            } else {
                "Bearer $userToken"
            }

            val response = apiService.getVoterDataWithToken(formattedToken)

            if (response.isSuccessful) {
                response.body()?.let { voterResponse ->
                    if (voterResponse.data.isNotEmpty()) {
                        // Get the user_id from the header to find the right voter data
                        val userId = getUserIdFromResponse(response.headers().get("x-user-id"))
                        Log.d(TAG, "Looking for voter data with user_id: $userId")

                        // Find the voter data that matches the user_id
                        val voterData = if (!userId.isNullOrEmpty()) {
                            voterResponse.data.find { it.user_id == userId }
                                ?: voterResponse.data.firstOrNull()
                        } else {
                            voterResponse.data.firstOrNull()
                        }

                        if (voterData != null) {
                            Log.d(TAG, "Voter data found: ${voterData.full_name}")

                            // Save user_id for future reference
                            saveUserId(userId)

                            // Save voter data to local storage
                            saveVoterDataLocally(voterData)

                            // Integrate with wallet if voter has an address
                            if (voterData.voter_address.isNotEmpty()) {
                                integrateWalletAddress(voterData.voter_address)
                            }

                            Result.success(voterData)
                        } else {
                            Log.e(TAG, "No voter data found for user")
                            Result.failure(Exception("No voter data found"))
                        }
                    } else {
                        Log.e(TAG, "Empty voter data response")
                        Result.failure(Exception("No voter data available"))
                    }
                } ?: run {
                    Log.e(TAG, "Null response body")
                    Result.failure(Exception("Invalid response"))
                }
            } else {
                Log.e(TAG, "API call failed: ${response.code()}")
                Result.failure(Exception("Failed to fetch voter data: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during voter data fetch", e)
            Result.failure(e)
        }
    }

    /**
     * Integrate voter address with wallet system
     */
    private suspend fun integrateWalletAddress(voterAddress: String) {
        try {
            // Check if the voter address matches any existing wallet
            val existingWallets = walletManager.getAllWallets()
            val matchingWallet = existingWallets.find {
                it.address.equals(voterAddress, ignoreCase = true)
            }

            if (matchingWallet != null) {
                Log.d(TAG, "Found matching wallet for voter address")
                // Set this wallet as selected if no wallet is currently selected
                if (walletManager.getSelectedWalletAddress() == null) {
                    walletManager.setSelectedWallet(voterAddress)
                }
            } else {
                Log.d(TAG, "No matching wallet found for voter address: $voterAddress")
                // Note: We don't create a wallet here as we need the private key
                // This would typically be handled during wallet import/creation
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error integrating wallet address", e)
        }
    }

    /**
     * Create or import wallet for voter
     */
    suspend fun createVoterWallet(pin: String, privateKey: String? = null): Result<String> {
        return try {
            val walletData = if (privateKey != null) {
                // Import existing wallet
                walletManager.importWallet(privateKey, pin, "Voter Wallet")
            } else {
                // Create new wallet
                walletManager.createNewWallet(pin, "Voter Wallet")
            }

            // Update voter data with new wallet address
            updateVoterWalletAddress(walletData.address)

            Result.success(walletData.address)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating voter wallet", e)
            Result.failure(e)
        }
    }

    /**
     * Get current wallet information
     */
    suspend fun getCurrentWalletInfo(): WalletInfo? {
        return try {
            val selectedAddress = walletManager.getSelectedWalletAddress()
                ?: walletManager.getDefaultWalletAddress()

            if (selectedAddress != null) {
                val balance = walletManager.getWalletBalance(selectedAddress)
                val balanceEth = Convert.fromWei(balance.toString(), Convert.Unit.ETHER)

                WalletInfo(
                    balance = "${balanceEth.setScale(4)} ETH",
                    privateKey = "", // Don't expose private key
                    publicKey = selectedAddress
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting wallet info", e)
            null
        }
    }

    /**
     * Check if user has a wallet setup
     */
    fun hasWalletSetup(): Boolean {
        return walletManager.hasWallets()
    }

    /**
     * Update voter wallet address in local storage
     */
    private fun updateVoterWalletAddress(address: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_VOTER_PUBLIC_KEY, address)
            apply()
        }
        Log.d(TAG, "Updated voter wallet address: $address")
    }

    /**
     * Extract the user_id from the response header
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
     * Save voter data to SharedPreferences
     */
    private fun saveVoterDataLocally(voterData: VoterData) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_VOTER_FULL_NAME, voterData.full_name)
            putString(KEY_VOTER_NIK, voterData.nik)
            putString(KEY_VOTER_PUBLIC_KEY, voterData.voter_address)
            putBoolean(KEY_VOTER_HAS_VOTED, voterData.has_voted)
            // Don't store private key directly - use wallet manager instead
            putString(KEY_VOTER_PRIVATE_KEY, "")
            apply()
        }
        Log.d(TAG, "Voter data saved to SharedPreferences")
    }

    /**
     * Get voter data from local storage with wallet integration
     */
    fun getVoterDataLocally(): VoterData? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val fullName = sharedPreferences.getString(KEY_VOTER_FULL_NAME, "") ?: ""
        val nik = sharedPreferences.getString(KEY_VOTER_NIK, "") ?: ""
        val publicKey = sharedPreferences.getString(KEY_VOTER_PUBLIC_KEY, "") ?: ""
        val hasVoted = sharedPreferences.getBoolean(KEY_VOTER_HAS_VOTED, false)

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
                voter_address = publicKey,
                region = "",
                is_registered = true,
                has_voted = hasVoted
            )
        } else {
            null
        }
    }

    /**
     * Get wallet information for display with enhanced features
     */
    suspend fun getWalletInfo(): VoterWalletInfo {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedPublicKey = sharedPreferences.getString(KEY_VOTER_PUBLIC_KEY, "") ?: ""

        // Try to get wallet info from wallet manager
        val walletInfo = getCurrentWalletInfo()

        return walletInfo?.let {
            VoterWalletInfo(
                balance = it.balance,
                privateKey = it.privateKey,
                publicKey = it.publicKey
            )
        } ?: VoterWalletInfo(
            balance = "0.0000 ETH",
            privateKey = "",
            publicKey = storedPublicKey
        )
    }

    /**
     * Clear voter data from local storage
     */
    fun clearVoterData() {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(KEY_VOTER_FULL_NAME)
            remove(KEY_VOTER_NIK)
            remove(KEY_VOTER_PUBLIC_KEY)
            remove(KEY_VOTER_PRIVATE_KEY)
            remove(KEY_VOTER_HAS_VOTED)
            apply()
        }
        Log.d(TAG, "Voter data cleared from SharedPreferences")
    }

    /**
     * Check if voter data is available locally
     */
    fun hasStoredVoterData(): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString(KEY_VOTER_FULL_NAME, null)
        val nik = sharedPreferences.getString(KEY_VOTER_NIK, null)
        return fullName != null && nik != null
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
}
