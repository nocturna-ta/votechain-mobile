package com.nocturna.votechain.viewmodel.vote

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.repository.VotingRepository
import com.nocturna.votechain.security.CryptoKeyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Vote Confirmation Screen
 */
class VoteConfirmationViewModel(
    private val context: Context,
    private val categoryId: String,
    private val electionPairId: String
) : ViewModel() {

    private val TAG = "VoteConfirmationViewModel"
    private val votingRepository = VotingRepository(context, CryptoKeyManager(context))

    private val _uiState = MutableStateFlow(VoteConfirmationUiState())
    val uiState: StateFlow<VoteConfirmationUiState> = _uiState.asStateFlow()

    /**
     * Cast the vote using the verified OTP token
     */
    fun castVote() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Get stored region or use default
            val region = getStoredRegion() ?: "default"

            votingRepository.castVoteWithOTPVerification(electionPairId, region)
                .collect { result ->
                    result.fold(
                        onSuccess = { voteResponse ->
                            Log.d(TAG, "Vote cast successfully: ${voteResponse.message}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isVoteSuccess = true,
                                error = null
                            )
                        },
                        onFailure = { e ->
                            Log.e(TAG, "Vote casting failed: ${e.message}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to cast vote"
                            )
                        }
                    )
                }
        }
    }

    private fun getStoredRegion(): String? {
        val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_region", null)
    }

    /**
     * Factory for creating VoteConfirmationViewModel
     */
    class Factory(
        private val context: Context,
        private val categoryId: String,
        private val electionPairId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VoteConfirmationViewModel::class.java)) {
                return VoteConfirmationViewModel(context, categoryId, electionPairId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * UI State for Vote Confirmation Screen
 */
data class VoteConfirmationUiState(
    val isLoading: Boolean = false,
    val isVoteSuccess: Boolean = false,
    val error: String? = null
)