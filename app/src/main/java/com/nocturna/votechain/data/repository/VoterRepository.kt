package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.model.VoterData
import com.nocturna.votechain.data.model.WalletInfo
import com.nocturna.votechain.data.network.NetworkClient
import com.nocturna.votechain.data.network.VoterApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class to handle voter-related operations
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

    // Use existing API service from NetworkClient
    private val apiService = NetworkClient.apiService

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
                        val voterData = voterResponse.data[0] // Get first voter data
                        saveVoterDataLocally(voterData)
                        Log.d(TAG, "Voter data fetched and saved successfully")
                        Result.success(voterData)
                    } else {
                        Log.e(TAG, "No voter data found in response")
                        Result.failure(Exception("No voter data found"))
                    }
                } ?: run {
                    Log.e(TAG, "Empty response body")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to fetch voter data: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")

                when (response.code()) {
                    401 -> Result.failure(Exception("Unauthorized: Invalid or expired token"))
                    403 -> Result.failure(Exception("Forbidden: Access denied"))
                    404 -> Result.failure(Exception("Voter data not found"))
                    else -> Result.failure(Exception("Failed to fetch voter data: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during voter data fetch", e)
            Result.failure(e)
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
            // Private key will be empty for now as mentioned in requirements
            putString(KEY_VOTER_PRIVATE_KEY, "")
            apply()
        }
        Log.d(TAG, "Voter data saved to SharedPreferences")
    }

    /**
     * Get voter data from local storage
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
     * Get wallet information for display
     */
    fun getWalletInfo(): WalletInfo {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        return WalletInfo(
            balance = "0.0000", // Empty for now as mentioned
            privateKey = sharedPreferences.getString(KEY_VOTER_PRIVATE_KEY, "") ?: "",
            publicKey = sharedPreferences.getString(KEY_VOTER_PUBLIC_KEY, "") ?: ""
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

//    /**
//     * Get stored voter data from local SharedPreferences
//     */
//    fun getStoredVoterData(): VoterData? {
//        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//
//        val fullName = sharedPreferences.getString(KEY_VOTER_FULL_NAME, null)
//        val nik = sharedPreferences.getString(KEY_VOTER_NIK, null)
//
//        return if (fullName != null && nik != null) {
//            val publicKey = sharedPreferences.getString(KEY_VOTER_PUBLIC_KEY, null)
//            val privateKey = sharedPreferences.getString(KEY_VOTER_PRIVATE_KEY, null)
//            val hasVoted = sharedPreferences.getBoolean(KEY_VOTER_HAS_VOTED, false)
//
//            val walletInfo = if (publicKey != null && privateKey != null) {
//                WalletInfo(public_key = publicKey, private_key = privateKey)
//            } else null
//
//            VoterData(
//                full_name = fullName,
//                nik = nik,
//                wallet_info = walletInfo,
//                has_voted = hasVoted
//            )
//        } else {
//            null
//        }
//    }

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

