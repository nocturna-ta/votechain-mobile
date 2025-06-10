package com.nocturna.votechain.viewmodel.candidate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.ElectionPair
import com.nocturna.votechain.data.model.Party
import com.nocturna.votechain.data.model.SupportingParty
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

    private val _allParties = MutableStateFlow<List<Party>>(emptyList())
    val allParties: StateFlow<List<Party>> = _allParties.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Selected pair for detail view
    private val _selectedPair = MutableStateFlow<ElectionPair?>(null)
    val selectedPair: StateFlow<ElectionPair?> = _selectedPair.asStateFlow()

    // Supporting parties for selected pair
    private val _supportingPartiesLoading = MutableStateFlow(false)
    val supportingPartiesLoading: StateFlow<Boolean> = _supportingPartiesLoading.asStateFlow()

    init {
        // Load initial data
        fetchAllParties()
        fetchElectionPairsWithSupportingParties()
    }

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
     * Fetch all election candidate pairs with their supporting parties
     */
    fun fetchElectionPairsWithSupportingParties() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getElectionPairsWithSupportingParties().collect { result ->
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
     * Fetch supporting parties for a specific election pair
     */
    fun fetchSupportingParties(pairId: String) {
        viewModelScope.launch {
            _supportingPartiesLoading.value = true

            repository.getSupportingParties(pairId).collect { result ->
                _supportingPartiesLoading.value = false
                result.fold(
                    onSuccess = { supportingParties ->
                        // Update the specific pair with supporting parties
                        val updatedPairs = _electionPairs.value.map { pair ->
                            if (pair.id == pairId) {
                                pair.copy(supporting_parties = supportingParties)
                            } else {
                                pair
                            }
                        }
                        _electionPairs.value = updatedPairs
                    },
                    onFailure = { e ->
                        _error.value = e.message ?: "Failed to fetch supporting parties"
                    }
                )
            }
        }
    }

    /**
     * Fetch all political parties
     */
    fun fetchAllParties() {
        viewModelScope.launch {
            repository.getAllParties().collect { result ->
                result.fold(
                    onSuccess = { parties ->
                        _allParties.value = parties
                    },
                    onFailure = { e ->
                        // Log error but don't show it to user as this is not critical
                        android.util.Log.e(
                            "ElectionViewModel",
                            "Failed to fetch all parties: ${e.message}"
                        )
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
     * Get party information by ID from cached parties
     */
    fun getPartyById(partyId: String): Party? {
        return _allParties.value.find { it.id == partyId }
    }

    /**
     * Get supporting parties for a specific election pair
     */
    fun getSupportingPartiesForPair(pairId: String): List<SupportingParty> {
        return _electionPairs.value.find { it.id == pairId }?.supporting_parties ?: emptyList()
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
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