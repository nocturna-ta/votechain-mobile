package com.nocturna.votechain.viewmodel.candidate

import com.nocturna.votechain.data.repository.VisionMissionRepository
import com.nocturna.votechain.data.repository.VisionMissionRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI State for the Vision & Mission screen
data class VisionMissionUiState(
    val candidateNumber: Int = 1,
    val vision: String = "",
    val missions: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Presenter Interface
interface VisionMissionViewModel {
    val uiState: StateFlow<VisionMissionUiState>
    fun loadData(candidateNumber: Int = 1)
}

// Presenter Implementation
class VisionMissionViewModelImpl(
    private val repository: VisionMissionRepository = VisionMissionRepositoryImpl()
) : VisionMissionViewModel {

    // Create coroutine scope for the presenter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(VisionMissionUiState())
    override val uiState: StateFlow<VisionMissionUiState> = _uiState.asStateFlow()

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
}
