package com.nocturna.votechain.viewmodel.candidate

import com.nocturna.votechain.data.repository.VisionMissionRepository
import com.nocturna.votechain.data.repository.VisionMissionRepositoryImpl
import com.nocturna.votechain.data.model.WorkProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VisionMissionUiState(
    val candidateNumber: Int = 1,
    val vision: String = "",
    val mission: String = "", // Changed from missions list to single mission
    val missions: List<String> = emptyList(), // Keep for backward compatibility
    val workPrograms: List<WorkProgram> = emptyList(), // New field for work programs
    val programDocs: String? = null, // New field for program docs link
    val isLoading: Boolean = false,
    val error: String? = null
)

// Presenter Interface - Updated
interface VisionMissionViewModel {
    val uiState: StateFlow<VisionMissionUiState>
    fun loadData(candidateNumber: Int = 1) // Keep for backward compatibility
    fun loadDataFromAPI(pairId: String) // New method for API integration
}

// Presenter Implementation - Updated
class VisionMissionViewModelImpl(
    private val repository: VisionMissionRepository = VisionMissionRepositoryImpl()
) : VisionMissionViewModel {

    // Create coroutine scope for the presenter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(VisionMissionUiState())
    override val uiState: StateFlow<VisionMissionUiState> = _uiState.asStateFlow()

    // Keep old method for backward compatibility
    override fun loadData(candidateNumber: Int) {
        // Update state to loading
        _uiState.value = _uiState.value.copy(isLoading = true)

        // Launch coroutine to call suspend function
        coroutineScope.launch {
            try {
                // Call suspend function from within coroutine
                val data = repository.getVisionMission(candidateNumber)

                // Update UI state with results
                _uiState.value = _uiState.value.copy(
                    candidateNumber = data.candidateNumber,
                    vision = data.vision,
                    missions = data.missions,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                // Handle any errors
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    // New method for API integration
    override fun loadDataFromAPI(pairId: String) {
        // Update state to loading
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        // Launch coroutine to call suspend function
        coroutineScope.launch {
            try {
                // Call new API method
                val data = repository.getVisionMissionFromAPI(pairId)

                // Update UI state with results
                _uiState.value = _uiState.value.copy(
                    vision = data.vision,
                    mission = data.mission,
                    workPrograms = data.workPrograms,
                    programDocs = data.programDocs,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                // Handle any errors
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
}