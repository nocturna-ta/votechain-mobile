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
import com.nocturna.votechain.utils.SignedTransactionGenerator
import kotlin.apply

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

    private val signedTransactionGenerator = SignedTransactionGenerator(cryptoKeyManager)

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
     * Cast a vote with OTP verification and signed transaction
     * @param electionPairId The ID of the selected candidate pair
     * @param region The voter's region
     * @param otpToken The OTP token for verification
     * @return Flow with the result of the vote casting operation
     */
    fun castVoteWithOTP(
        electionPairId: String,
        region: String,
        otpToken: String
    ): Flow<Result<VoteCastResponse>> = flow {
        try {
            Log.d(TAG, "🗳️ Starting enhanced vote casting process")
            Log.d(TAG, "  - Election Pair ID: $electionPairId")
            Log.d(TAG, "  - Region: $region")
            Log.d(TAG, "  - OTP Token: ${if (otpToken.isNotEmpty()) "Provided" else "Missing"}")

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

            // Step 2: Validate OTP token
            if (otpToken.isEmpty()) {
                Log.e(TAG, "❌ OTP token is required for voting")
                emit(Result.failure(Exception("OTP token is required for voting")))
                return@flow
            }

            // Step 3: Get authentication token
            val token = getAuthToken()
            if (token.isNullOrEmpty()) {
                Log.e(TAG, "❌ No authentication token available")
                emit(Result.failure(Exception("Authentication required")))
                return@flow
            }

            // Step 4: Get voter ID from stored data
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val voterId = sharedPreferences.getString("user_id", "")
                ?: sharedPreferences.getString("voter_id", "")

            if (voterId.isNullOrEmpty()) {
                Log.e(TAG, "❌ Voter ID not found in stored data")
                emit(Result.failure(Exception("Voter ID not found. Please complete registration.")))
                return@flow
            }

            Log.d(TAG, "✅ Voter ID retrieved: $voterId")

            // Step 5: Generate signed transaction
            Log.d(TAG, "🔐 Generating signed transaction...")
            val signedTransaction = signedTransactionGenerator.generateVoteSignedTransaction(
                electionPairId = electionPairId,
                voterId = voterId,
                region = region
            )

            // Step 6: Validate signed transaction
            if (!signedTransactionGenerator.validateSignedTransaction(signedTransaction)) {
                Log.e(TAG, "❌ Failed to generate valid signed transaction")
                emit(Result.failure(Exception("Failed to sign vote data. Cryptographic signing failed.")))
                return@flow
            }

            Log.d(TAG, "✅ Signed transaction generated and validated successfully")

            // Step 7: Create enhanced vote request with OTP
            val voteRequest = VoteCastRequest(
                election_pair_id = electionPairId,
                otp_token = otpToken,
                region = region,
                signed_transaction = signedTransaction!!,
                voter_id = voterId
            )

            // Step 8: Log detailed request information
            logVoteRequestDetails(voteRequest)

            Log.d(TAG, "🌐 Making API call to cast vote with OTP verification")

            // Step 9: Make API call with enhanced request
            val response = voteApiService.castVoteWithOTP(
                token = "Bearer $token",
                request = voteRequest
            )

            Log.d(TAG, "📡 Vote API response received - Code: ${response.code()}")

            if (response.isSuccessful) {
                val voteResponse = response.body()
                if (voteResponse != null) {
                    Log.d(TAG, "✅ Vote cast successfully!")
                    Log.d(TAG, "  - Vote ID: ${voteResponse.data?.id}")
                    Log.d(TAG, "  - Status: ${voteResponse.data?.status}")
                    Log.d(TAG, "  - TX Hash: ${voteResponse.data?.tx_hash}")
                    Log.d(TAG, "  - Voted At: ${voteResponse.data?.voted_at}")

                    // Update local voting status
                    updateLocalVotingStatus(electionPairId, voteResponse.data?.tx_hash)

                    emit(Result.success(voteResponse))
                } else {
                    Log.e(TAG, "❌ Empty response body from vote API")
                    emit(Result.failure(Exception("Empty response from server")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Vote API failed with code: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")

                val errorMessage = when (response.code()) {
                    400 -> "Invalid vote data or OTP token"
                    401 -> "Authentication failed"
                    403 -> "Already voted or not authorized"
                    422 -> "Invalid OTP token or expired"
                    else -> "Server error: ${response.code()}"
                }

                emit(Result.failure(Exception(errorMessage)))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during vote casting", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Get authentication token with fallback
     */
    private fun getAuthToken(): String? {
        // First try TokenManager
        var token = tokenManager.getAccessToken()
        Log.d(TAG, "TokenManager token: ${if (token.isNullOrEmpty()) "Empty" else "Available"}")

        // If TokenManager doesn't have token, try other sources
        if (token.isNullOrEmpty()) {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            token = sharedPreferences.getString("access_token", null)

            // If found, sync to TokenManager
            if (!token.isNullOrEmpty()) {
                Log.d(TAG, "Syncing token from SharedPreferences to TokenManager")
                tokenManager.saveAccessToken(token)
            }
        }

        return if (token.isNullOrEmpty()) null else token
    }

    /**
     * Update local voting status after successful vote
     */
    private fun updateLocalVotingStatus(electionPairId: String, txHash: String?) {
        try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("voter_has_voted", true)
                putString("last_vote_election_pair_id", electionPairId)
                putLong("last_vote_timestamp", System.currentTimeMillis())
                if (!txHash.isNullOrEmpty()) {
                    putString("last_vote_tx_hash", txHash)
                }
                apply()
            }
            Log.d(TAG, "✅ Local voting status updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Failed to update local voting status: ${e.message}")
        }
    }

    /**
     * Log detailed vote request information for debugging
     */
    private fun logVoteRequestDetails(request: VoteCastRequest) {
        Log.d(TAG, "📋 Vote Request Details:")
        Log.d(TAG, "  - Election Pair ID: ${request.election_pair_id}")
        Log.d(TAG, "  - Voter ID: ${request.voter_id}")
        Log.d(TAG, "  - Region: ${request.region}")
        Log.d(TAG, "  - OTP Token: ${if (request.otp_token.isNotEmpty()) "✅ PROVIDED" else "❌ MISSING"}")
        Log.d(TAG, "  - Signed Transaction: ${
            when {
                request.signed_transaction.isEmpty() -> "❌ EMPTY"
                request.signed_transaction.length < 32 -> "❌ TOO_SHORT (${request.signed_transaction.length} chars)"
                else -> "✅ VALID (${request.signed_transaction.length} chars): ${request.signed_transaction.take(16)}..."
            }
        }")
    }

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

            castVoteWithOTP(optionId, region, "").collect { result ->
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