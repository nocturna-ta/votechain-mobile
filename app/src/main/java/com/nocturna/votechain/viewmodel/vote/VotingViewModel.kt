package com.nocturna.votechain.viewmodel.vote

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.VotingCategory
import com.nocturna.votechain.data.model.VotingResult
import com.nocturna.votechain.data.repository.VotingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VotingViewModel : ViewModel() {
    private val repository = VotingRepository()

    private val _activeVotings = MutableStateFlow<List<VotingCategory>>(emptyList())
    val activeVotings: StateFlow<List<VotingCategory>> = _activeVotings.asStateFlow()

    private val _votingResults = MutableStateFlow<List<VotingResult>>(emptyList())
    val votingResults: StateFlow<List<VotingResult>> = _votingResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val hasActiveVotings = mutableStateOf(false)

    init {
        fetchActiveVotings()
    }

    fun fetchActiveVotings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getActiveVotings().collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = { votingList ->
                        _activeVotings.value = votingList
                        hasActiveVotings.value = votingList.isNotEmpty()
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

    fun submitVote(categoryId: String, optionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.submitVote(categoryId, optionId).collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = {
                        // Refresh data after successful vote
                        fetchActiveVotings()
                        fetchVotingResults()
                    },
                    onFailure = { e ->
                        _error.value = e.message ?: "Unknown error occurred"
                    }
                )
            }
        }
    }
}