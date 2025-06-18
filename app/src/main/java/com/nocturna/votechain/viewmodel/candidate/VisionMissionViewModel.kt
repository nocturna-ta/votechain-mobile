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
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody

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
    val error: String? = null,
    val isDownloadingPdf: Boolean = false // Add download state
)

/**
 * ViewModel Interface for Vision Mission
 */
/**
 * Interface for Vision Mission ViewModel
 */
interface VisionMissionViewModel {
    val uiState: StateFlow<VisionMissionUiState>

    fun loadData(candidateNumber: Int)
    fun loadDataFromAPI(pairId: String)
    fun downloadProgramDocs(pairId: String): Result<ResponseBody>
}

/**
 * Implementation of Vision Mission ViewModel
 */
class VisionMissionViewModelImpl : VisionMissionViewModel {

    private val TAG = "VisionMissionViewModel"
    private val repository: VisionMissionRepository = VisionMissionRepositoryImpl()

    // Create coroutine scope for the ViewModel
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _uiState = MutableStateFlow(VisionMissionUiState())
    override val uiState: StateFlow<VisionMissionUiState> = _uiState.asStateFlow()

    /**
     * Load data using old method (for backward compatibility)
     * @param candidateNumber The candidate number (1, 2, 3)
     */
    override fun loadData(candidateNumber: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d(TAG, "Loading vision mission data for candidate: $candidateNumber")

                val visionMissionData = repository.getVisionMission(candidateNumber)

                _uiState.value = _uiState.value.copy(
                    candidateNumber = candidateNumber,
                    vision = visionMissionData.vision,
                    missions = visionMissionData.missions,
                    mission = visionMissionData.missions.joinToString(". "), // Convert list to single string
                    workPrograms = emptyList(), // No work programs in old method
                    programDocs = null, // No program docs in old method
                    isLoading = false,
                    error = null
                )

                Log.d(TAG, "Successfully loaded vision mission data")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading vision mission data: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    /**
     * Load data from API using pair ID
     * @param pairId The election pair ID
     */
    override fun loadDataFromAPI(pairId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d(TAG, "Loading vision mission data from API for pair: $pairId")

                val visionMissionDetail = repository.getVisionMissionFromAPI(pairId)

                _uiState.value = _uiState.value.copy(
                    candidateNumber = pairId.toIntOrNull() ?: 1,
                    vision = visionMissionDetail.vision,
                    mission = visionMissionDetail.mission,
                    missions = listOf(visionMissionDetail.mission), // Convert single string to list for compatibility
                    workPrograms = visionMissionDetail.workPrograms,
                    programDocs = visionMissionDetail.programDocs,
                    isLoading = false,
                    error = null
                )

                Log.d(TAG, "Successfully loaded vision mission data from API")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading vision mission data from API: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load data from API: ${e.message}"
                )
            }
        }
    }

    /**
     * Download program docs PDF
     * @param pairId The election pair ID
     * @return Result containing ResponseBody for the PDF file
     */
    override fun downloadProgramDocs(pairId: String): Result<ResponseBody> {
        return try {
            _uiState.value = _uiState.value.copy(isDownloadingPdf = true)

            Log.d(TAG, "Downloading program docs for pair: $pairId")

            // This should be called from a coroutine scope since it's a suspend function
            val result = runBlocking {
                repository.getProgramDocsFromAPI(pairId)
            }

            _uiState.value = _uiState.value.copy(isDownloadingPdf = false)

            if (result.isSuccess) {
                Log.d(TAG, "Successfully downloaded program docs")
                result
            } else {
                Log.e(TAG, "Failed to download program docs: ${result.exceptionOrNull()?.message}")
                result
            }

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isDownloadingPdf = false)
            Log.e(TAG, "Exception while downloading program docs: ${e.message}", e)
            Result.failure(e)
        }
    }
}