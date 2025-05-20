package com.nocturna.votechain.viewmodel.candidate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.ElectionPair
import com.nocturna.votechain.data.repository.ElectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing election-related data
 */
class ElectionViewModel(
    private val repository: ElectionRepository = ElectionRepository()
) : ViewModel() {

    // UI states
    private val _electionPairs = MutableStateFlow<List<ElectionPair>>(emptyList())
    val electionPairs: StateFlow<List<ElectionPair>> = _electionPairs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Selected pair for detail view
    private val _selectedPair = MutableStateFlow<ElectionPair?>(null)
    val selectedPair: StateFlow<ElectionPair?> = _selectedPair.asStateFlow()

    /**
     * Fetch all election candidate pairs
     */
    fun fetchElectionPairs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getElectionPairs().collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = { pairs ->
                        _electionPairs.value = pairs
                    },
                    onFailure = { e ->
                        _error.value = e.message ?: "Unknown error occurred"
                    }
                )
            }
        }
    }

    /**
     * Set the selected election pair by ID
     */
    fun selectPair(pairId: String) {
        _selectedPair.value = _electionPairs.value.find { it.id == pairId }
    }

    /**
     * Factory for creating ElectionViewModel
     */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ElectionViewModel::class.java)) {
                return ElectionViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}