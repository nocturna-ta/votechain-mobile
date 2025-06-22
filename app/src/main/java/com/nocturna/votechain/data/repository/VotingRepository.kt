package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.model.DummyData
import com.nocturna.votechain.data.model.VoteCastRequest
import com.nocturna.votechain.data.model.VoteCastResponse
import com.nocturna.votechain.data.model.VotingCategory
import com.nocturna.votechain.data.model.VotingResult
import com.nocturna.votechain.data.network.VoteApiService
import com.nocturna.votechain.data.network.ElectionNetworkClient
import com.nocturna.votechain.security.CryptoKeyManager
import com.nocturna.votechain.utils.TokenManager
import com.nocturna.votechain.utils.VoteValidationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class VotingRepository(
    private val context: Context,
    private val voteApiService: VoteApiService,
    private val cryptoKeyManager: CryptoKeyManager,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "VotingRepository"
        private const val PREFS_NAME = "VoteChainPrefs"
        private const val KEY_HAS_VOTED = "voter_has_voted"
    }

    // Use dummy data flag - set to true for empty voting state
    private val showEmptyState = false

    fun getActiveVotings(): Flow<Result<List<VotingCategory>>> = flow {
        try {
            // Simulate network delay
            delay(1000)

            // Return dummy data
            if (showEmptyState) {
                emit(Result.success(DummyData.emptyVotingCategories))
            } else {
                emit(Result.success(DummyData.activeVotingCategories))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getVotingResults(): Flow<Result<List<VotingResult>>> = flow {
        try {
            // Simulate network delay
            delay(1000)

            // Return dummy data
            emit(Result.success(DummyData.votingResults))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get authentication token with fallback to ElectionNetworkClient
     */
    private fun getAuthToken(): String? {
        // First try TokenManager
        var token = tokenManager.getAccessToken()
        Log.d(TAG, "TokenManager token: ${if (token.isNullOrEmpty()) "Empty" else "Available (${token.length} chars)"}")

        // If TokenManager doesn't have token, try ElectionNetworkClient
        if (token.isNullOrEmpty()) {
            token = ElectionNetworkClient.getUserToken()
            Log.d(TAG, "ElectionNetworkClient token: ${if (token.isEmpty()) "Empty" else "Available (${token.length} chars)"}")

            // If found in ElectionNetworkClient, sync to TokenManager
            if (token.isNotEmpty()) {
                Log.d(TAG, "Syncing token from ElectionNetworkClient to TokenManager")
                tokenManager.saveAccessToken(token)
            }
        }

        return if (token.isNullOrEmpty()) null else token
    }

    /**
     * Cast a vote for a specific election pair
     * @param electionPairId The ID of the selected candidate pair
     * @param region The voter's region
     * @return Flow with the result of the vote casting operation
     */
    fun castVote(electionPairId: String, region: String): Flow<Result<VoteCastResponse>> = flow {
        try {
            Log.d(TAG, "🗳️ Starting vote casting process for pair: $electionPairId")

            // Step 1: Comprehensive validation
            val validationResult = VoteValidationHelper.validateVotePrerequisites(
                context, cryptoKeyManager, tokenManager
            )

            if (!validationResult.isValid) {
                Log.e(TAG, "❌ Vote validation failed")
                Log.e(TAG, validationResult.getErrorMessage())
                emit(Result.failure(Exception("Vote validation failed: ${validationResult.getErrorMessage()}")))
                return@flow
            }

            Log.d(TAG, "✅ All vote prerequisites validated successfully")

            // Step 2: Get authentication token with fallback
            val token = getAuthToken()
            if (token.isNullOrEmpty()) {
                Log.e(TAG, "❌ No authentication token available from any source")
                emit(Result.failure(Exception("Authentication required")))
                return@flow
            }

            // Step 3: Get voter ID from stored data
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val voterId = sharedPreferences.getString("user_id", "") ?: ""

            // Step 4: Create signed transaction
            val dataToSign = "$electionPairId:$voterId:$region"
            Log.d(TAG, "📝 Preparing to sign data: $dataToSign")

            val signedTransaction = cryptoKeyManager.signData(dataToSign)


// Step 5: Validate signed transaction
            if (signedTransaction.isNullOrEmpty()) {
                Log.e(TAG, "❌ Failed to generate signed transaction")
                Log.e(TAG, "This indicates a problem with the cryptographic signing process")

                // Additional debugging
                Log.d(TAG, "🔍 Debugging signing failure:")
                Log.d(TAG, "  - Has key pair: ${cryptoKeyManager.hasStoredKeyPair()}")
                Log.d(TAG, "  - Can sign data: ${cryptoKeyManager.canSignData()}")

                emit(Result.failure(Exception("Failed to sign vote data. Cryptographic signing failed.")))
                return@flow
            }

            // Step 6: Log detailed request information
            VoteValidationHelper.logVoteRequestDetails(electionPairId, region, voterId, signedTransaction)

            // Step 7: Create vote request
            val voteRequest = VoteCastRequest(
                election_pair_id = electionPairId,
                voter_id = voterId,
                region = region,
                signed_transaction = signedTransaction
            )

            Log.d(TAG, "🌐 Making API call to cast vote")

            // Step 8: Make API call
            val response = voteApiService.castVote(
                token = "Bearer $token",
                request = voteRequest
            )

            Log.d(TAG, "📡 Vote API response received - Code: ${response.code()}")

            if (response.isSuccessful) {
                val voteResponse = response.body()
                if (voteResponse != null) {
                    Log.d(TAG, "✅ Vote cast successfully!")
                    Log.d(TAG, "📋 Response details:")
                    Log.d(TAG, "  - Code: ${voteResponse.code}")
                    Log.d(TAG, "  - Message: ${voteResponse.message}")
                    voteResponse.data?.let { data ->
                        Log.d(TAG, "  - Vote ID: ${data.id}")
                        Log.d(TAG, "  - Status: ${data.status}")
                        Log.d(TAG, "  - TX Hash: ${data.tx_hash}")
                    }

                    // Mark as voted
                    markAsVoted()
                    emit(Result.success(voteResponse))
                } else {
                    Log.e(TAG, "❌ Vote response body is null")
                    emit(Result.failure(Exception("Invalid response from server")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Vote failed - HTTP ${response.code()}: $errorBody")

                // Enhanced error logging
                when (response.code()) {
                    400 -> Log.e(TAG, "Bad Request - Check if all required fields are present and valid")
                    401 -> Log.e(TAG, "Unauthorized - Authentication token may be invalid or expired")
                    403 -> Log.e(TAG, "Forbidden - User may not have permission to vote")
                    409 -> Log.e(TAG, "Conflict - User may have already voted")
                    422 -> Log.e(TAG, "Unprocessable Entity - Validation error on server side")
                    500 -> Log.e(TAG, "Internal Server Error - Server-side issue")
                }

                emit(Result.failure(Exception("Vote failed: ${response.code()} - $errorBody")))
            }

        } catch (e: Exception) {
            Log.e(TAG, "💥 Exception during vote casting: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Mark voter as having voted
     */
    private fun markAsVoted() {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(KEY_HAS_VOTED, true)
            apply()
        }
        Log.d(TAG, "Voter marked as having voted")
    }

    /**
     * Check if voter has already voted
     */
    fun hasVoted(): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_HAS_VOTED, false)
    }

    /**
     * Reset voting status (for testing purposes)
     */
    fun resetVotingStatus() {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(KEY_HAS_VOTED, false)
            apply()
        }
        Log.d(TAG, "Voting status reset")
    }

    /**
     * Legacy submit vote method for backward compatibility
     * @param categoryId The voting category ID
     * @param optionId The selected option/candidate ID
     * @return Flow with the result of the vote submission
     */
    fun submitVote(categoryId: String, optionId: String): Flow<Result<Unit>> = flow {
        try {
            Log.d(TAG, "Legacy submitVote called with categoryId: $categoryId, optionId: $optionId")

            // Map legacy parameters to new castVote method
            val region = getStoredRegion() ?: "default"

            castVote(optionId, region).collect { result ->
                result.fold(
                    onSuccess = { voteResponse ->
                        Log.d(TAG, "Legacy vote submission successful")
                        emit(Result.success(Unit))
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Legacy vote submission failed: ${e.message}")
                        emit(Result.failure(e))
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in legacy submitVote", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get stored region from preferences
     */
    private fun getStoredRegion(): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_region", null)
    }
}