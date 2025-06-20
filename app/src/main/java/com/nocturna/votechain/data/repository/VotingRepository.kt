package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.model.DummyData
import com.nocturna.votechain.data.model.VoteCastRequest
import com.nocturna.votechain.data.model.VoteCastResponse
import com.nocturna.votechain.data.model.VotingCategory
import com.nocturna.votechain.data.model.VotingResult
import com.nocturna.votechain.data.network.VoteApiService
import com.nocturna.votechain.security.CryptoKeyManager
import com.nocturna.votechain.utils.TokenManager
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
     * Cast a vote for a specific election pair
     * @param electionPairId The ID of the selected candidate pair
     * @param region The voter's region
     * @return Flow with the result of the vote casting operation
     */
    fun castVote(electionPairId: String, region: String): Flow<Result<VoteCastResponse>> = flow {
        try {
            Log.d(TAG, "Starting vote casting process for pair: $electionPairId")

            // Get authentication token
            val token = tokenManager.getAccessToken()
            if (token.isNullOrEmpty()) {
                Log.e(TAG, "No authentication token available")
                emit(Result.failure(Exception("Authentication required")))
                return@flow
            }

            // Get voter ID from stored data
            val voterId = getStoredVoterId()
            if (voterId.isNullOrEmpty()) {
                Log.e(TAG, "No voter ID found")
                emit(Result.failure(Exception("Voter ID not found")))
                return@flow
            }

            // Create blockchain transaction signature
            val signedTransaction = createSignedTransaction(electionPairId, voterId)
            if (signedTransaction.isNullOrEmpty()) {
                Log.e(TAG, "Failed to create signed transaction")
                emit(Result.failure(Exception("Failed to create blockchain signature")))
                return@flow
            }

            // Create vote request
            val voteRequest = VoteCastRequest(
                election_pair_id = electionPairId,
                region = region,
                signed_transaction = signedTransaction,
                voter_id = voterId
            )

            Log.d(TAG, "Sending vote request: $voteRequest")

            // Make API call
            val response = voteApiService.castVote("Bearer $token", voteRequest)

            if (response.isSuccessful) {
                response.body()?.let { voteResponse ->
                    Log.d(TAG, "Vote cast successfully: ${voteResponse.message}")

                    // Update local voting status
                    updateVotingStatus(true)

                    emit(Result.success(voteResponse))
                } ?: run {
                    Log.e(TAG, "Empty response body")
                    emit(Result.failure(Exception("Empty response body")))
                }
            } else {
                val errorMessage = "Vote casting failed with code: ${response.code()}"
                Log.e(TAG, errorMessage)
                emit(Result.failure(Exception(errorMessage)))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error casting vote: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Legacy method for backward compatibility
     */
    fun submitVote(categoryId: String, optionId: String): Flow<Result<Unit>> = flow {
        try {
            // Map legacy parameters to new castVote method
            val region = getStoredRegion() ?: "default"

            castVote(optionId, region).collect { result ->
                result.fold(
                    onSuccess = {
                        emit(Result.success(Unit))
                    },
                    onFailure = { e ->
                        emit(Result.failure(e))
                    }
                )
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Create a signed blockchain transaction for the vote
     */
    private fun createSignedTransaction(electionPairId: String, voterId: String): String? {
        return try {
            // Get private key for signing
            val privateKey = cryptoKeyManager.getPrivateKey()
            if (privateKey == null) {
                Log.e(TAG, "Private key not available")
                return null
            }

            // Create transaction data
            val transactionData = "${voterId}_${electionPairId}_${System.currentTimeMillis()}"

            // In a real implementation, you would use the private key to sign the transaction
            // For now, we'll create a mock signed transaction
            val signature = cryptoKeyManager.signData(transactionData)

            if (signature != null) {
                // Return the signed transaction in the expected format
                "${transactionData}_${signature}"
            } else {
                Log.e(TAG, "Failed to sign transaction data")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating signed transaction", e)
            null
        }
    }

    /**
     * Get stored voter ID
     */
    private fun getStoredVoterId(): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("voter_id", null)
            ?: sharedPreferences.getString("voter_nik", null) // fallback to NIK
    }

    /**
     * Get stored region
     */
    private fun getStoredRegion(): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("voter_region", null)
    }

    /**
     * Update voting status locally
     */
    private fun updateVotingStatus(hasVoted: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(KEY_HAS_VOTED, hasVoted)
            putLong("voted_at", System.currentTimeMillis())
            apply()
        }
        Log.d(TAG, "Voting status updated: $hasVoted")
    }

    /**
     * Check if user has already voted
     */
    fun hasVoted(): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_HAS_VOTED, false)
    }
}