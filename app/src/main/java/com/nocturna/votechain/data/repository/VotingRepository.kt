package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.model.VoteCastRequest
import com.nocturna.votechain.data.model.VoteCastResponse
import com.nocturna.votechain.data.model.VotingCategory
import com.nocturna.votechain.data.network.NetworkClient
import com.nocturna.votechain.security.CryptoKeyManager
import com.nocturna.votechain.utils.SignedTransactionGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

/**
 * Enhanced Voting Repository with integrated OTP verification flow
 * Now with improved error handling and transaction verification
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
     * Enhanced cast vote with OTP verification and signed transaction
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
            Log.d(TAG, "🗳️ Starting enhanced vote casting with OTP verification")
            Log.d(TAG, "  - Election Pair ID: $electionPairId")
            Log.d(TAG, "  - Region: $region")

            // Step 1: Pre-flight validation
            val preflightResult = performPreflightValidation(electionPairId, region)
            if (preflightResult.isFailure) {
                emit(preflightResult)
                return@flow
            }

            // Step 2: Get verified OTP token
            val otpToken = otpRepository.getStoredOTPToken()
            if (otpToken.isNullOrEmpty()) {
                Log.e(TAG, "❌ No valid OTP token found")
                emit(Result.failure(Exception("OTP verification required. Please verify OTP first.")))
                return@flow
            }

            Log.d(TAG, "✅ Valid OTP token found")

            // Step 3: Get authentication token
            val token = getAuthToken()
            if (token.isNullOrEmpty()) {
                Log.e(TAG, "❌ No authentication token available")
                emit(Result.failure(Exception("Authentication required. Please login again.")))
                return@flow
            }

            Log.d(TAG, "✅ Authentication token available")

            // Step 4: Get voter ID from stored data
            val voterId = getVoterId()
            if (voterId.isNullOrEmpty()) {
                Log.e(TAG, "❌ Voter ID not found in stored data")
                emit(Result.failure(Exception("Voter ID not found. Please complete registration.")))
                return@flow
            }

            Log.d(TAG, "✅ Voter ID retrieved: $voterId")

            // Step 5: Get region (use provided region or get from storage)
            val finalRegion = region.ifEmpty { getStoredRegion() ?: "default" }
            Log.d(TAG, "✅ Using region: $finalRegion")

            // Step 6: Enhanced cryptographic prerequisites validation
            val validationResult = validateVotingPrerequisites()
            if (!validationResult.isValid) {
                Log.e(TAG, "❌ Voting prerequisites validation failed")
                emit(Result.failure(Exception("Voting prerequisites not met: ${validationResult.getErrorMessage()}")))
                return@flow
            }

            Log.d(TAG, "✅ Enhanced cryptographic prerequisites validated")

            // Step 7: Generate signed transaction with enhanced security
            Log.d(TAG, "🔐 Generating enhanced signed transaction...")
            val signedTransaction = signedTransactionGenerator.generateVoteSignedTransaction(
                electionPairId = electionPairId,
                voterId = voterId,
                region = finalRegion
            )

            // Step 8: Validate signed transaction
            if (signedTransaction.isNullOrEmpty()) {
                Log.e(TAG, "❌ Failed to generate signed transaction")
                emit(Result.failure(Exception("Failed to sign vote data. Cryptographic signing failed.")))
                return@flow
            }

            // Step 9: Verify transaction integrity before submission
            if (!signedTransactionGenerator.verifyTransactionIntegrity(signedTransaction)) {
                Log.e(TAG, "❌ Signed transaction integrity verification failed")
                emit(Result.failure(Exception("Invalid signed transaction generated. Please try again.")))
                return@flow
            }

            Log.d(TAG, "✅ Signed transaction generated and verified successfully")
            Log.d(TAG, "  - Transaction length: ${signedTransaction.length} characters")
            Log.d(TAG, "  - Transaction preview: ${signedTransaction.take(50)}...")

            // Step 10: Create enhanced vote request
            val voteRequest = VoteCastRequest(
                election_pair_id = electionPairId,
                otp_token = otpToken,
                region = finalRegion,
                signed_transaction = signedTransaction,
                voter_id = voterId
            )

            // Step 11: Log request details for debugging
            logEnhancedVoteRequestDetails(voteRequest)

            Log.d(TAG, "🌐 Making API call to cast vote")

            // Step 12: Make API call with enhanced error handling
            val response = voteApiService.castVoteWithOTP(
                token = "Bearer $token",
                request = voteRequest
            )

            Log.d(TAG, "📡 Vote API response received - Code: ${response.code()}")

            // Step 13: Process response with enhanced error handling
            emit(processVoteResponse(response, electionPairId, voteRequest))

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during enhanced vote casting", e)
            emit(Result.failure(handleVotingException(e)))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Perform pre-flight validation
     */
    private fun performPreflightValidation(electionPairId: String, region: String): Result<VoteCastResponse> {
        return try {
            // Validate inputs
            if (electionPairId.isEmpty()) {
                Log.e(TAG, "❌ Election pair ID is empty")
                return Result.failure(Exception("Election pair ID is required"))
            }

            // Check if user has already voted
            if (hasUserVoted()) {
                Log.e(TAG, "❌ User has already voted")
                return Result.failure(Exception("You have already voted in this election"))
            }

            // Check network connectivity
            if (!isNetworkAvailable()) {
                Log.e(TAG, "❌ Network not available")
                return Result.failure(Exception("Network connection required to cast vote"))
            }

            Log.d(TAG, "✅ Pre-flight validation passed")
            Result.success(VoteCastResponse(0, null, null, "Pre-flight validation passed"))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Pre-flight validation failed: ${e.message}", e)
            Result.failure(Exception("Pre-flight validation failed: ${e.message}"))
        }
    }

    /**
     * Validate voting prerequisites
     */
    private fun validateVotingPrerequisites(): ValidationResult {
        return try {
            val issues = mutableListOf<String>()

            // Check if crypto key manager has required keys
            if (!cryptoKeyManager.hasStoredKeyPair()) {
                Log.e(TAG, "❌ No key pair stored")
                issues.add("No key pair stored")
            }

            // Test signing capability
            val testData = "test_${System.currentTimeMillis()}"
            val testSignature = cryptoKeyManager.signData(testData)

            if (testSignature.isNullOrEmpty()) {
                Log.e(TAG, "❌ Signing test failed - no signature generated")
                issues.add("Signing test failed - no signature generated")
            }

            // Check OTP token availability
            val otpToken = otpRepository.getStoredOTPToken()
            if (otpToken.isNullOrEmpty()) {
                issues.add("OTP token not available")
            }

            // Check authentication token
            val authToken = getAuthToken()
            if (authToken.isNullOrEmpty()) {
                issues.add("Authentication token not available")
            }

            // Check voter ID
            val voterId = getVoterId()
            if (voterId.isNullOrEmpty()) {
                issues.add("Voter ID not available")
            }

            Log.d(TAG, if (issues.isEmpty()) "✅ Prerequisites validation passed" else "❌ Prerequisites validation failed")

            ValidationResult(
                isValid = issues.isEmpty(),
                issues = issues
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Prerequisites validation exception: ${e.message}", e)
            ValidationResult(
                isValid = false,
                issues = listOf("Prerequisites validation exception: ${e.message}")
            )
        }
    }

    /**
     * Process vote response with enhanced error handling
     */
    private fun processVoteResponse(
        response: Response<VoteCastResponse>,
        electionPairId: String,
        voteRequest: VoteCastRequest
    ): Result<VoteCastResponse> {
        return try {
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

                    // Log successful vote for analytics
                    logSuccessfulVote(electionPairId, voteResponse.data?.tx_hash)

                    Result.success(voteResponse)
                } else {
                    Log.e(TAG, "❌ Empty response body from vote API")
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                // Enhanced error handling based on response codes
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Vote API failed with code: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")

                val errorException = handleVotingError(response)

                // Clear invalid OTP token for specific error codes
                if (response.code() in listOf(401, 422)) {
                    otpRepository.clearOTPToken()
                    Log.d(TAG, "🧹 Cleared invalid OTP token")
                }

                Result.failure(errorException)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error processing vote response: ${e.message}", e)
            Result.failure(Exception("Failed to process vote response: ${e.message}"))
        }
    }

    /**
     * Enhanced error handling for voting API responses
     */
    private fun handleVotingError(response: Response<VoteCastResponse>): Exception {
        val errorCode = response.code()
        val errorMessage = when (errorCode) {
            400 -> "Invalid vote data. Please check your selection and try again."
            401 -> "Authentication failed. Please login again."
            403 -> "You have already voted or are not authorized to vote."
            422 -> "Invalid or expired OTP token. Please verify OTP again."
            423 -> "Invalid signed transaction. Please try again."
            429 -> "Too many requests. Please wait a moment and try again."
            500 -> "Server error. Please try again later."
            502 -> "Service temporarily unavailable. Please try again later."
            503 -> "Service maintenance in progress. Please try again later."
            504 -> "Request timeout. Please check your connection and try again."
            else -> "Failed to cast vote. Please try again. (Error: $errorCode)"
        }

        Log.e(TAG, "Vote API error: $errorCode - $errorMessage")

        // Create specific exception types for different error scenarios
        return when (errorCode) {
            401, 403 -> SecurityException(errorMessage)
            422 -> IllegalArgumentException(errorMessage)
            429 -> Exception("$errorMessage (Rate Limited)")
            in 500..599 -> Exception("$errorMessage (Server Error)")
            else -> Exception(errorMessage)
        }
    }

    /**
     * Handle voting exceptions with enhanced error messages
     */
    private fun handleVotingException(e: Exception): Exception {
        return when (e) {
            is SecurityException -> Exception("Security error: ${e.message}")
            is IllegalArgumentException -> Exception("Invalid data: ${e.message}")
            is java.net.SocketTimeoutException -> Exception("Network timeout. Please check your connection.")
            is java.net.UnknownHostException -> Exception("Cannot connect to voting server. Please check your internet connection.")
            is javax.net.ssl.SSLException -> Exception("Secure connection failed. Please try again.")
            else -> Exception("Voting failed: ${e.message}")
        }
    }

    /**
     * Get voter ID from stored data with multiple fallback sources
     */
    private fun getVoterId(): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_id", "")
            ?: sharedPreferences.getString("voter_id", "")
            ?: sharedPreferences.getString("username", "")
            ?: cryptoKeyManager.getVoterAddress()
    }

    /**
     * Get stored region from preferences
     */
    private fun getStoredRegion(): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_region", null)
            ?: sharedPreferences.getString("region", null)
            ?: sharedPreferences.getString("location", null)
    }

    /**
     * Get authentication token with multiple fallback sources
     */
    private fun getAuthToken(): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
            ?: sharedPreferences.getString("user_token", null)
            ?: sharedPreferences.getString("access_token", null)
            ?: sharedPreferences.getString("jwt_token", null)
    }

    /**
     * Check network availability
     */
    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            networkCapabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            Log.w(TAG, "Cannot check network availability: ${e.message}")
            true // Assume network is available if we can't check
        }
    }

    /**
     * Update local voting status with enhanced metadata
     */
    private fun updateLocalVotingStatus(electionPairId: String, txHash: String?) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val timestamp = System.currentTimeMillis()

        with(sharedPreferences.edit()) {
            putBoolean(KEY_HAS_VOTED, true)
            putString("last_vote_election_pair_id", electionPairId)
            putString("last_vote_tx_hash", txHash)
            putLong("last_vote_timestamp", timestamp)
            putString("last_vote_region", getStoredRegion())
            putString("last_vote_method", "enhanced_signed_transaction")
            putInt("total_votes_cast", sharedPreferences.getInt("total_votes_cast", 0) + 1)
            apply()
        }

        Log.d(TAG, "✅ Local voting status updated")
        Log.d(TAG, "  - Election Pair: $electionPairId")
        Log.d(TAG, "  - TX Hash: $txHash")
        Log.d(TAG, "  - Timestamp: $timestamp")
    }

    /**
     * Log successful vote for analytics
     */
    private fun logSuccessfulVote(electionPairId: String, txHash: String?) {
        try {
            Log.d(TAG, "📊 Logging successful vote for analytics")
            Log.d(TAG, "  - Election Pair ID: $electionPairId")
            Log.d(TAG, "  - Transaction Hash: $txHash")
            Log.d(TAG, "  - Timestamp: ${System.currentTimeMillis()}")
            Log.d(TAG, "  - Voter Address: ${cryptoKeyManager.getVoterAddress()}")

            // Here you could add analytics or reporting to external services
            // Example: FirebaseAnalytics, custom analytics endpoint, etc.

        } catch (e: Exception) {
            Log.w(TAG, "Failed to log successful vote: ${e.message}")
        }
    }

    /**
     * Enhanced vote request details logging
     */
    private fun logEnhancedVoteRequestDetails(voteRequest: VoteCastRequest) {
        Log.d(TAG, "📋 Enhanced Vote Request Details:")
        Log.d(TAG, "  - Election Pair ID: ${voteRequest.election_pair_id}")
        Log.d(TAG, "  - Region: ${voteRequest.region}")
        Log.d(TAG, "  - Voter ID: ${voteRequest.voter_id}")
        Log.d(TAG, "  - OTP Token: ${if (voteRequest.otp_token.isNotEmpty()) "✅ Present (${voteRequest.otp_token.length} chars)" else "❌ Missing"}")
        Log.d(TAG, "  - Signed Transaction: ${if (voteRequest.signed_transaction.isNotEmpty()) "✅ Present (${voteRequest.signed_transaction.length} chars)" else "❌ Missing"}")

        // Log transaction info
        if (voteRequest.signed_transaction.isNotEmpty()) {
            val txInfo = signedTransactionGenerator.getTransactionInfo(voteRequest.signed_transaction)
            Log.d(TAG, "  - Transaction Info:")
            txInfo.forEach { (key, value) ->
                Log.d(TAG, "    * $key: $value")
            }
        }
    }

    /**
     * Legacy vote submission method for backward compatibility
     */
    fun submitVote(categoryId: String, optionId: String): Flow<Result<VoteCastResponse>> = flow {
        try {
            Log.d(TAG, "📝 Submitting legacy vote - Category: $categoryId, Option: $optionId")

            // Get stored region or use default
            val region = getStoredRegion() ?: "default"

            // For legacy compatibility, use optionId as electionPairId
            // Use the enhanced voting method
            castVoteWithOTPVerification(optionId, region).collect { result ->
                emit(result)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during legacy vote submission", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Check if user has already voted
     */
    fun hasUserVoted(): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_HAS_VOTED, false)
    }

    /**
     * Get voting statistics
     */
    fun getVotingStats(): Map<String, Any> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return mapOf(
            "has_voted" to sharedPreferences.getBoolean(KEY_HAS_VOTED, false),
            "total_votes_cast" to sharedPreferences.getInt("total_votes_cast", 0),
            "last_vote_timestamp" to sharedPreferences.getLong("last_vote_timestamp", 0),
            "last_vote_election_pair_id" to (sharedPreferences.getString("last_vote_election_pair_id", "") ?: ""),
            "last_vote_tx_hash" to (sharedPreferences.getString("last_vote_tx_hash", "") ?: ""),
            "last_vote_method" to (sharedPreferences.getString("last_vote_method", "unknown") ?: "unknown")
        )
    }

    /**
     * Get active voting categories
     */
    fun getActiveVotings(): Flow<Result<List<VotingCategory>>> = flow {
        try {
            // Simulate network delay
            delay(1000)

            // Return default Presidential election card
            val defaultPresidentialElection = VotingCategory(
                id = "presidential_2024",
                title = "Presidential Election 2024 - Indonesia",
                description = "Choose the leaders you trust to guide Indonesia forward",
                isActive = true
            )

            emit(Result.success(listOf(defaultPresidentialElection)))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting active votings: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get voting results
     */
    fun getVotingResults(): Flow<Result<List<VotingCategory>>> = flow {
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
            Log.e(TAG, "❌ Error getting voting results: ${e.message}", e)
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
            remove("last_vote_election_pair_id")
            remove("last_vote_tx_hash")
            remove("last_vote_timestamp")
            remove("last_vote_region")
            remove("last_vote_method")
            remove("total_votes_cast")
            apply()
        }
        otpRepository.clearOTPToken()
        Log.d(TAG, "🧹 Voting status and OTP token reset")
    }

    /**
     * Retry vote casting with exponential backoff
     */
    fun retryVoteCasting(
        electionPairId: String,
        region: String,
        maxRetries: Int = 3
    ): Flow<Result<VoteCastResponse>> = flow {
        var retryCount = 0
        var lastException: Exception? = null

        while (retryCount < maxRetries) {
            try {
                Log.d(TAG, "🔄 Retry attempt ${retryCount + 1} of $maxRetries")

                castVoteWithOTPVerification(electionPairId, region).collect { result ->
                    if (result.isSuccess) {
                        Log.d(TAG, "✅ Vote casting successful on retry ${retryCount + 1}")
                        emit(result)
                        return@collect
                    } else {
                        lastException = result.exceptionOrNull() as? Exception
                        Log.w(TAG, "❌ Retry ${retryCount + 1} failed: ${lastException?.message}")
                    }
                }

                retryCount++

                if (retryCount < maxRetries) {
                    val delayMs = (1000 * Math.pow(2.0, retryCount.toDouble())).toLong()
                    Log.d(TAG, "⏳ Waiting ${delayMs}ms before next retry...")
                    delay(delayMs)
                }

            } catch (e: Exception) {
                lastException = e
                Log.e(TAG, "❌ Exception during retry ${retryCount + 1}: ${e.message}", e)
                retryCount++

                if (retryCount < maxRetries) {
                    val delayMs = (1000 * Math.pow(2.0, retryCount.toDouble())).toLong()
                    delay(delayMs)
                }
            }
        }

        // All retries failed
        Log.e(TAG, "❌ All retry attempts failed")
        emit(Result.failure(lastException ?: Exception("Vote casting failed after $maxRetries retries")))
    }.flowOn(Dispatchers.IO)

    /**
     * Get detailed voting status
     */
    fun getDetailedVotingStatus(): Map<String, Any> {
        val stats = getVotingStats()

        return stats.toMutableMap().apply {
            put("crypto_ready", cryptoKeyManager.hasStoredKeyPair())
            put("otp_token_available", !otpRepository.getStoredOTPToken().isNullOrEmpty())
            put("auth_token_available", !getAuthToken().isNullOrEmpty())
            put("voter_id_available", !getVoterId().isNullOrEmpty())
            put("region_available", !getStoredRegion().isNullOrEmpty())
            put("network_available", isNetworkAvailable())
            put("can_vote", canVote())
        }
    }

    /**
     * Check if user can vote
     */
    fun canVote(): Boolean {
        return try {
            !hasUserVoted() &&
                    cryptoKeyManager.hasStoredKeyPair() &&
                    !getAuthToken().isNullOrEmpty() &&
                    !getVoterId().isNullOrEmpty() &&
                    isNetworkAvailable()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user can vote: ${e.message}", e)
            false
        }
    }

    /**
     * Validation result data class
     */
    data class ValidationResult(
        val isValid: Boolean,
        val issues: List<String>
    ) {
        fun getErrorMessage(): String {
            return if (issues.isEmpty()) {
                "All validations passed"
            } else {
                "Validation failed:\n${issues.joinToString("\n• ", "• ")}"
            }
        }
    }
}