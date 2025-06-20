package com.nocturna.votechain.viewmodel.vote

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.VoteCastResponse
import com.nocturna.votechain.data.model.VotingCategory
import com.nocturna.votechain.data.model.VotingResult
import com.nocturna.votechain.data.network.NetworkClient
import com.nocturna.votechain.data.repository.VotingRepository
import com.nocturna.votechain.security.CryptoKeyManager
import com.nocturna.votechain.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VotingViewModel(private val repository: VotingRepository) : ViewModel() {

    private val _activeVotings = MutableStateFlow<List<VotingCategory>>(emptyList())
    val activeVotings: StateFlow<List<VotingCategory>> = _activeVotings.asStateFlow()

    private val _votingResults = MutableStateFlow<List<VotingResult>>(emptyList())
    val votingResults: StateFlow<List<VotingResult>> = _votingResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _voteResult = MutableStateFlow<VoteCastResponse?>(null)
    val voteResult: StateFlow<VoteCastResponse?> = _voteResult.asStateFlow()

    private val _hasVoted = MutableStateFlow(false)
    val hasVoted: StateFlow<Boolean> = _hasVoted.asStateFlow()

    init {
        checkVotingStatus()
    }

    fun fetchActiveVotings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getActiveVotings().collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = { votings ->
                        _activeVotings.value = votings
                    },
                    onFailure = { e ->
                        _error.value = e.message ?: "Unknown error occurred"
                    }
                )
            }
        }
    }

    fun fetchVotingResults() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getVotingResults().collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = { resultsList ->
                        _votingResults.value = resultsList
                    },
                    onFailure = { e ->
                        _error.value = e.message ?: "Unknown error occurred"
                    }
                )
            }
        }
    }

    /**
     * Cast a vote for a specific election pair
     * @param electionPairId The ID of the selected candidate pair
     * @param region The voter's region
     */
    fun castVote(electionPairId: String, region: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _voteResult.value = null

            repository.castVote(electionPairId, region).collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = { voteResponse ->
                        _voteResult.value = voteResponse
                        _hasVoted.value = true

                        // Refresh data after successful vote
                        fetchActiveVotings()
                        fetchVotingResults()
                    },
                    onFailure = { e ->
                        _error.value = e.message ?: "Failed to cast vote"
                    }
                )
            }
        }
    }

    /**
     * Legacy method for backward compatibility
     * @param categoryId The voting category ID
     * @param optionId The selected option/candidate ID
     */
    fun submitVote(categoryId: String, optionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.submitVote(categoryId, optionId).collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = {
                        _hasVoted.value = true
                        // Refresh data after successful vote
                        fetchActiveVotings()
                        fetchVotingResults()
                    },
                    onFailure = { e ->
                        _error.value = e.message ?: "Failed to submit vote"
                    }
                )
            }
        }
    }

    /**
     * Check current voting status
     */
    private fun checkVotingStatus() {
        _hasVoted.value = repository.hasVoted()
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear vote result
     */
    fun clearVoteResult() {
        _voteResult.value = null
    }

    /**
     * Reset voting status (for testing purposes)
     */
    fun resetVotingStatus() {
        repository.resetVotingStatus()
        _hasVoted.value = false
    }

    // Factory for creating VotingViewModel with dependencies
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VotingViewModel::class.java)) {
                val cryptoKeyManager = CryptoKeyManager(context)
                val tokenManager = TokenManager(context)
                val repository = VotingRepository(
                    context = context,
                    voteApiService = NetworkClient.voteApiService,
                    cryptoKeyManager = cryptoKeyManager,
                    tokenManager = tokenManager
                )
                return VotingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}