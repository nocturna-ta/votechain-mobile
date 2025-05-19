package com.nocturna.votechain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.VotingResult
import com.nocturna.votechain.data.repository.DetailedResultRepository
import com.nocturna.votechain.data.repository.Region
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Detailed Result Screen
 */
class DetailedResultViewModel(
    private val repository: DetailedResultRepository = DetailedResultRepository()
) : ViewModel() {

    // UI state for detailed results
    private val _detailedResult = MutableStateFlow<VotingResult?>(null)
    val detailedResult: StateFlow<VotingResult?> = _detailedResult.asStateFlow()

    // Available regions for filtering
    private val _availableRegions = MutableStateFlow<List<Region>>(emptyList())
    val availableRegions: StateFlow<List<Region>> = _availableRegions.asStateFlow()

    // Currently selected region
    private val _selectedRegion = MutableStateFlow<Region?>(null)
    val selectedRegion: StateFlow<Region?> = _selectedRegion.asStateFlow()

    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Selected slice in the pie chart
    private val _selectedSlice = MutableStateFlow<Int?>(null)
    val selectedSlice: StateFlow<Int?> = _selectedSlice.asStateFlow()

    /**
     * Initialize the ViewModel by loading regions and results
     */
    fun initialize(categoryId: String) {
        loadAvailableRegions(categoryId)
        loadDetailedResults(categoryId) // Load with default All Regions filter
    }

    /**
     * Set the selected region and reload data with this filter
     */
    fun selectRegion(region: Region, categoryId: String) {
        viewModelScope.launch {
            _selectedRegion.value = region
            loadDetailedResults(categoryId, if (region.code == "ALL") null else region.code)
        }
    }

    /**
     * Set the selected slice in the pie chart
     */
    fun selectSlice(index: Int?) {
        _selectedSlice.value = index
    }

    /**
     * Load available regions for filtering
     */
    private fun loadAvailableRegions(categoryId: String) {
        viewModelScope.launch {
            repository.getAvailableRegions(categoryId).collect { result ->
                result.fold(
                    onSuccess = { regions ->
                        _availableRegions.value = regions
                        // Set "All Regions" as default selected region
                        _selectedRegion.value = regions.find { it.code == "ALL" }
                    },
                    onFailure = { e ->
                        _error.value = e.message ?: "Error loading regions"
                    }
                )
            }
        }
    }

    /**
     * Load detailed results with optional region filter
     */
    private fun loadDetailedResults(categoryId: String, regionCode: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getDetailedResults(categoryId, regionCode).collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = { votingResult ->
                        _detailedResult.value = votingResult
                        // Reset selected slice when data changes
                        _selectedSlice.value = null
                    },
                    onFailure = { e ->
                        _error.value = e.message ?: "Error loading results"
                    }
                )
            }
        }
    }

    /**
     * Factory for creating DetailedResultViewModel
     */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailedResultViewModel::class.java)) {
                return DetailedResultViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}