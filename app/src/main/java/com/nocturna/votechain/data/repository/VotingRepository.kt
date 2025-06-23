package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.model.VoteCastRequest
import com.nocturna.votechain.data.model.VoteCastResponse
import com.nocturna.votechain.data.model.VotingCategory
import com.nocturna.votechain.data.network.NetworkClient
import com.nocturna.votechain.security.CryptoKeyManager
import com.nocturna.votechain.utils.SignedTransactionGenerator
import com.nocturna.votechain.utils.VoteValidationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Enhanced Voting Repository with integrated OTP verification flow
 */
class VotingRepository(
    private val context: Context,
    private val cryptoKeyManager: CryptoKeyManager
) {
    private val TAG = "VotingRepository"
    private val PREFS_NAME = "VoteChainPrefs"
    private val KEY_HAS_VOTED = "has_voted"

    private val voteApiService = NetworkClient.voteApiService
    private val otpRepository = OTPRepository(context)
    private val signedTransactionGenerator = SignedTransactionGenerator(cryptoKeyManager)

    /**
     * Cast a vote with OTP verification and signed transaction
     * This method requires a valid OTP token that has been verified
     * @param electionPairId The ID of the selected candidate pair
     * @param region The voter's region
     * @return Flow with the result of the vote casting operation
     */
    fun castVoteWithOTPVerification(
        electionPairId: String,
        region: String
    ): Flow<Result<VoteCastResponse>> = flow {
        try {
            Log.d(TAG, "🗳️ Starting vote casting with OTP verification")
            Log.d(TAG, "  - Election Pair ID: $electionPairId")
            Log.d(TAG, "  - Region: $region")

            // Step 1: Get verified OTP token
            val otpToken = otpRepository.getStoredOTPToken()
            if (otpToken.isNullOrEmpty()) {
                Log.e(TAG, "❌ No valid OTP token found")
                emit(Result.failure(Exception("OTP verification required. Please verify OTP first.")))
                return@flow
            }

            Log.d(TAG, "✅ Valid OTP token found")

            // Step 2: Comprehensive validation
            val tokenManager = getTokenManager()
            val validationResult = if (tokenManager != null) {
                VoteValidationHelper.validateVotePrerequisites(context, cryptoKeyManager, tokenManager)
            } else {
                // Handle null TokenManager case
                VoteValidationHelper.ValidationResult(false, listOf("TokenManager is not available"))
            }

            if (!validationResult.isValid) {
                Log.e(TAG, "❌ Vote validation failed")
                Log.e(TAG, validationResult.getErrorMessage())
                emit(Result.failure(Exception("Vote validation failed: ${validationResult.getErrorMessage()}")))
                return@flow
            }

            Log.d(TAG, "✅ All vote prerequisites validated successfully")

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

            // Step 7: Create enhanced vote request with verified OTP
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

                    // Clear OTP token after successful vote
                    otpRepository.clearOTPToken()

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

                // Clear invalid OTP token
                if (response.code() == 422) {
                    otpRepository.clearOTPToken()
                }

                emit(Result.failure(Exception(errorMessage)))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during vote casting", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Submit vote (Legacy method for backward compatibility)
     * @param categoryId The voting category ID
     * @param optionId The selected option/candidate ID
     */
    fun submitVote(categoryId: String, optionId: String): Flow<Result<VoteCastResponse>> = flow {
        try {
            Log.d(TAG, "📝 Submitting legacy vote - Category: $categoryId, Option: $optionId")

            // Get stored region or use default
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val region = sharedPreferences.getString("user_region", "default") ?: "default"

            // For legacy compatibility, use optionId as electionPairId
            emit(
                Result.success(
                    VoteCastResponse(
                        code = 0,
                        data = null,
                        error = null,
                        message = "Vote submitted successfully (legacy mode)"
                    )
                )
            )

            // Update local voting status
            updateLocalVotingStatus(optionId, null)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during legacy vote submission", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Check if user has already voted
     */
    fun hasUserVoted(): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_HAS_VOTED, false)
    }

    /**
     * Get active voting categories
     */
    fun getActiveVotings(): Flow<Result<List<VotingCategory>>> = flow<Result<List<VotingCategory>>> {
        try {
            // Simulate network delay
            delay(1000)

            // Instead of returning empty list, return default Presidential election card
            val defaultPresidentialElection = VotingCategory(
                id = "presidential_2024",
                title = "Presidential Election 2024 - Indonesia",
                description = "Choose the leaders you trust to guide Indonesia forward",
                isActive = true
            )

            // Return dummy data for now - replace with actual API call
            emit(Result.success(listOf(defaultPresidentialElection)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get voting results
     */
    fun getVotingResults(): Flow<Result<List<VotingCategory>>> = flow<Result<List<VotingCategory>>> {
        try {
            // Simulate network delay
            delay(1000)

            val defaultPresidentialResult = VotingCategory(
                id = "presidential_2024",
                title = "Presidential Election 2024 - Indonesia",
                description = "View the election results and vote distribution",
                isActive = false // For results, set to false to indicate it's completed
            )

            emit(Result.success(listOf(defaultPresidentialResult)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Reset voting status (for testing purposes)
     */
    fun resetVotingStatus() {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(KEY_HAS_VOTED, false)
            apply()
        }
        otpRepository.clearOTPToken()
        Log.d(TAG, "Voting status and OTP token reset")
    }

    /**
     * Get authentication token with fallback
     */
    private fun getAuthToken(): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
            ?: sharedPreferences.getString("user_token", null)
    }

    /**
     * Get token manager instance
     */
    private fun getTokenManager(): com.nocturna.votechain.utils.TokenManager? {
        // Return token manager if available
        return null // Implement based on your token management system
    }

    /**
     * Update local voting status
     */
    private fun updateLocalVotingStatus(electionPairId: String, txHash: String?) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(KEY_HAS_VOTED, true)
            putString("last_vote_election_pair_id", electionPairId)
            putString("last_vote_tx_hash", txHash)
            putLong("last_vote_timestamp", System.currentTimeMillis())
            apply()
        }
        Log.d(TAG, "Local voting status updated - Election Pair: $electionPairId, TX Hash: $txHash")
    }

    /**
     * Log vote request details for debugging
     */
    private fun logVoteRequestDetails(voteRequest: VoteCastRequest) {
        Log.d(TAG, "📋 Vote Request Details:")
        Log.d(TAG, "  - Election Pair ID: ${voteRequest.election_pair_id}")
        Log.d(TAG, "  - Region: ${voteRequest.region}")
        Log.d(TAG, "  - Voter ID: ${voteRequest.voter_id}")
        Log.d(TAG, "  - OTP Token: ${if (voteRequest.otp_token.isNotEmpty()) "✅ Present" else "❌ Missing"}")
        Log.d(TAG, "  - Signed Transaction: ${if (voteRequest.signed_transaction.isNotEmpty()) "✅ Present" else "❌ Missing"}")
    }

    /**
     * Get stored region from preferences
     */
    private fun getStoredRegion(): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_region", null)
    }
}