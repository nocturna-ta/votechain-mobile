package com.nocturna.votechain.viewmodel.vote

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.VoteCastResponse
import com.nocturna.votechain.data.repository.VotingRepository
import com.nocturna.votechain.security.CryptoKeyManager
import com.nocturna.votechain.utils.VotingErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Enhanced ViewModel for voting operations with improved error handling
 */
class VotingViewModel(
    private val context: Context,
    private val repository: VotingRepository,
    private val errorHandler: VotingErrorHandler
) : ViewModel() {

    private val TAG = "VotingViewModel"

    // UI State
    private val _uiState = MutableStateFlow(VotingUiState())
    val uiState: StateFlow<VotingUiState> = _uiState.asStateFlow()

    // Legacy state flows for backward compatibility
    private val _voteResult = MutableStateFlow<VoteCastResponse?>(null)
    val voteResult: StateFlow<VoteCastResponse?> = _voteResult.asStateFlow()

    private val _error = MutableStateFlow<VotingErrorHandler.VotingError?>(null)
    val error: StateFlow<VotingErrorHandler.VotingError?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasVoted = MutableStateFlow(false)
    val hasVoted: StateFlow<Boolean> = _hasVoted.asStateFlow()

    init {
        // Initialize voting status
        updateVotingStatus()
    }

    /**
     * Enhanced cast vote with comprehensive error handling
     */
    fun castVote(electionPairId: String, region: String, otpToken: String? = null) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🗳️ Starting enhanced vote casting")

                // Update UI state
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    votingStep = VotingStep.SUBMITTING_VOTE
                )
                _isLoading.value = true
                _error.value = null

                // Validate inputs
                if (electionPairId.isEmpty()) {
                    throw IllegalArgumentException("Election pair ID is required")
                }

                // Pre-flight checks
                performPreflightChecks()

                // Cast vote using repository
                repository.castVoteWithOTPVerification(electionPairId, region)
                    .collect { result ->
                        result.fold(
                            onSuccess = { response ->
                                Log.d(TAG, "✅ Vote cast successfully")
                                handleVoteSuccess(response)
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "❌ Vote casting failed: ${exception.message}")
                                handleVoteError(exception)
                            }
                        )
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception in castVote: ${e.message}", e)
                handleVoteError(e)
            }
        }
    }

    /**
     * Retry vote casting with exponential backoff
     */
    fun retryVote(electionPairId: String, region: String, maxRetries: Int = 3) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Starting retry vote casting")

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    votingStep = VotingStep.RETRYING_VOTE
                )
                _isLoading.value = true
                _error.value = null

                repository.retryVoteCasting(electionPairId, region, maxRetries)
                    .collect { result ->
                        result.fold(
                            onSuccess = { response ->
                                Log.d(TAG, "✅ Retry vote cast successfully")
                                handleVoteSuccess(response)
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "❌ Retry vote casting failed: ${exception.message}")
                                handleVoteError(exception)
                            }
                        )
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception in retryVote: ${e.message}", e)
                handleVoteError(e)
            }
        }
    }

    /**
     * Handle successful vote
     */
    private fun handleVoteSuccess(response: VoteCastResponse) {
        Log.d(TAG, "✅ Processing successful vote response")

        _voteResult.value = response
        _hasVoted.value = true

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = null,
            voteResult = response,
            votingStep = VotingStep.VOTE_CONFIRMED,
            isVoteSuccessful = true
        )
        _isLoading.value = false
        _error.value = null

        Log.d(TAG, "🎉 Vote successful - TX Hash: ${response.data?.tx_hash}")
    }

    /**
     * Handle vote error with enhanced error processing
     */
    private fun handleVoteError(exception: Throwable) {
        Log.e(TAG, "❌ Processing vote error: ${exception.message}")

        val votingError = errorHandler.handleVotingError(exception as Exception)
        errorHandler.logError(votingError, exception)

        _error.value = votingError

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = votingError,
            votingStep = VotingStep.ERROR,
            isVoteSuccessful = false
        )
        _isLoading.value = false

        // Clear vote result on error
        _voteResult.value = null
    }

    /**
     * Perform pre-flight checks before voting
     */
    private fun performPreflightChecks() {
        Log.d(TAG, "🔍 Performing pre-flight checks")

        // Check if user can vote
        if (!repository.canVote()) {
            val status = repository.getDetailedVotingStatus()
            val issues = mutableListOf<String>()

            if (status["has_voted"] as Boolean) {
                issues.add("You have already voted")
            }
            if (!(status["crypto_ready"] as Boolean)) {
                issues.add("Voting keys not set up")
            }
            if (!(status["otp_token_available"] as Boolean)) {
                issues.add("OTP verification required")
            }
            if (!(status["auth_token_available"] as Boolean)) {
                issues.add("Authentication required")
            }
            if (!(status["network_available"] as Boolean)) {
                issues.add("Network connection required")
            }

            throw IllegalStateException("Cannot vote: ${issues.joinToString(", ")}")
        }

        Log.d(TAG, "✅ Pre-flight checks passed")
    }

    /**
     * Update voting status
     */
    private fun updateVotingStatus() {
        try {
            val hasVoted = repository.hasUserVoted()
            val status = repository.getDetailedVotingStatus()

            _hasVoted.value = hasVoted

            _uiState.value = _uiState.value.copy(
                hasVoted = hasVoted,
                canVote = repository.canVote(),
                votingStatus = status
            )

            Log.d(TAG, "📊 Voting status updated - Has voted: $hasVoted, Can vote: ${repository.canVote()}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating voting status: ${e.message}", e)
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Reset voting state
     */
    fun resetVotingState() {
        _voteResult.value = null
        _error.value = null
        _isLoading.value = false

        _uiState.value = VotingUiState()

        // Update voting status
        updateVotingStatus()

        Log.d(TAG, "🔄 Voting state reset")
    }

    /**
     * Get recovery suggestions for current error
     */
    fun getRecoverySuggestions(): List<String> {
        return _error.value?.let { error ->
            errorHandler.getRecoverySuggestions(error)
        } ?: emptyList()
    }

    /**
     * Check if current error is retryable
     */
    fun isCurrentErrorRetryable(): Boolean {
        return _error.value?.isRetryable ?: false
    }

    /**
     * Get estimated recovery time for current error
     */
    fun getEstimatedRecoveryTime(): String {
        return _error.value?.let { error ->
            errorHandler.getEstimatedRecoveryTime(error)
        } ?: "Unknown"
    }

    /**
     * Create error report for support
     */
    fun createErrorReport(): Map<String, Any>? {
        return _error.value?.let { error ->
            // Find the original exception if available
            val originalException = Exception(error.message)
            errorHandler.createErrorReport(error, originalException)
        }
    }

    /**
     * Check if error requires immediate user action
     */
    fun requiresImmediateAction(): Boolean {
        return _error.value?.let { error ->
            errorHandler.requiresImmediateAction(error)
        } ?: false
    }

    /**
     * Get voting statistics
     */
    fun getVotingStats(): Map<String, Any> {
        return repository.getVotingStats()
    }

    /**
     * Factory for creating VotingViewModel
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VotingViewModel::class.java)) {
                val cryptoKeyManager = CryptoKeyManager(context)
                val repository = VotingRepository(context, cryptoKeyManager)
                val errorHandler = VotingErrorHandler(context)
                return VotingViewModel(context, repository, errorHandler) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Comprehensive UI state for voting operations
 */
data class VotingUiState(
    val isLoading: Boolean = false,
    val error: VotingErrorHandler.VotingError? = null,
    val voteResult: VoteCastResponse? = null,
    val votingStep: VotingStep = VotingStep.IDLE,
    val hasVoted: Boolean = false,
    val canVote: Boolean = false,
    val isVoteSuccessful: Boolean = false,
    val votingStatus: Map<String, Any> = emptyMap()
)

/**
 * Voting steps enum for UI state tracking
 */
enum class VotingStep {
    IDLE,
    VALIDATING_PREREQUISITES,
    GENERATING_TRANSACTION,
    SUBMITTING_VOTE,
    RETRYING_VOTE,
    VOTE_CONFIRMED,
    ERROR
}