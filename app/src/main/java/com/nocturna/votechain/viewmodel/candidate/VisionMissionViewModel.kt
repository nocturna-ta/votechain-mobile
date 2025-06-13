package com.nocturna.votechain.viewmodel.candidate

import android.util.Log
import com.nocturna.votechain.data.repository.VisionMissionRepository
import com.nocturna.votechain.data.model.WorkProgram
import com.nocturna.votechain.data.repository.VisionMissionRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for Vision Mission Screen
 */
data class VisionMissionUiState(
    val candidateNumber: Int = 1,
    val vision: String = "",
    val mission: String = "", // Single mission from API
    val missions: List<String> = emptyList(), // Keep for backward compatibility
    val workPrograms: List<WorkProgram> = emptyList(), // Work programs from API
    val programDocs: String? = null, // Program docs link from API
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel Interface for Vision Mission
 */
interface VisionMissionViewModel {
    val uiState: StateFlow<VisionMissionUiState>
    fun loadData(candidateNumber: Int = 1) // Keep for backward compatibility
    fun loadDataFromAPI(pairId: String) // New method for API integration
    fun clearError() // Helper method to clear errors
}

/**
 * ViewModel Implementation for Vision Mission
 */
class VisionMissionViewModelImpl(
    private val repository: VisionMissionRepository = VisionMissionRepositoryImpl()
) : VisionMissionViewModel {

    private val TAG = "VisionMissionViewModel"

    // Create coroutine scope with SupervisorJob for better error handling
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _uiState = MutableStateFlow(VisionMissionUiState())
    override val uiState: StateFlow<VisionMissionUiState> = _uiState.asStateFlow()

    /**
     * Load data using the old method (backward compatibility)
     * @param candidateNumber The candidate number (1, 2, 3)
     */
    override fun loadData(candidateNumber: Int) {
        Log.d(TAG, "Loading data for candidate number: $candidateNumber")

        // Update state to loading
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        // Launch coroutine to call suspend function
        coroutineScope.launch {
            try {
                // Call suspend function from repository
                val data = repository.getVisionMission(candidateNumber)

                // Update UI state with results
                _uiState.value = _uiState.value.copy(
                    candidateNumber = data.candidateNumber,
                    vision = data.vision,
                    missions = data.missions, // Use missions list for backward compatibility
                    mission = data.missions.firstOrNull() ?: "", // Use first mission as single mission
                    workPrograms = emptyList(), // No work programs in old method
                    programDocs = null, // No program docs in old method
                    isLoading = false,
                    error = null
                )

                Log.d(TAG, "Successfully loaded data for candidate $candidateNumber")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data for candidate $candidateNumber: ${e.message}", e)

                // Handle any errors
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred while loading candidate data"
                )
            }
        }
    }

    /**
     * Load data from API using pair ID
     * @param pairId The election pair ID from the election pairs API
     */
    override fun loadDataFromAPI(pairId: String) {
        Log.d(TAG, "Loading data from API for pair ID: $pairId")

        // Validate pairId
        if (pairId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Invalid pair ID provided"
            )
            return
        }

        // Update state to loading
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        // Launch coroutine to call suspend function
        coroutineScope.launch {
            try {
                // Call new API method from repository
                val data = repository.getVisionMissionFromAPI(pairId)

                // Update UI state with API results
                _uiState.value = _uiState.value.copy(
                    vision = data.vision,
                    mission = data.mission,
                    workPrograms = data.workPrograms,
                    programDocs = data.programDocs,
                    missions = if (data.mission.isNotBlank()) listOf(data.mission) else emptyList(), // Convert single mission to list for compatibility
                    isLoading = false,
                    error = null
                )

                Log.d(TAG, "Successfully loaded data from API for pair $pairId")
                Log.d(TAG, "Vision: ${data.vision}")
                Log.d(TAG, "Mission: ${data.mission}")
                Log.d(TAG, "Work Programs: ${data.workPrograms.size}")
                Log.d(TAG, "Program Docs: ${data.programDocs}")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading data from API for pair $pairId: ${e.message}", e)

                // Handle any errors
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred while loading data from API"
                )
            }
        }
    }

    /**
     * Clear any error state
     */
    override fun clearError() {
        if (_uiState.value.error != null) {
            _uiState.value = _uiState.value.copy(error = null)
        }
    }
}