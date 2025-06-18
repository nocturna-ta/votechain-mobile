package com.nocturna.votechain.viewmodel.candidate

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.repository.VisionMissionRepository
import com.nocturna.votechain.data.model.WorkProgram
import com.nocturna.votechain.data.repository.VisionMissionRepositoryImpl
import com.nocturna.votechain.utils.PdfUtils
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
    val isDownloadingPdf: Boolean = false, // New state for PDF download
    val downloadError: String? = null // New state for download errors
)

/**
 * Vision Mission Presenter Interface
 */
interface VisionMissionPresenter {
    val uiState: StateFlow<VisionMissionUiState>
    fun loadData(candidateNumber: Int)
    fun loadDataFromAPI(pairId: String)
    fun downloadProgramDocs(context: Context, pairId: String, candidateName: String = "Kandidat")
}

/**
 * Implementation of Vision Mission Presenter
 */
class VisionMissionViewModelImpl : VisionMissionPresenter {

    private val TAG = "VisionMissionViewModel"
    private val repository: VisionMissionRepository = VisionMissionRepositoryImpl()

    // Create coroutine scope for the presenter
    private val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(VisionMissionUiState())
    override val uiState: StateFlow<VisionMissionUiState> = _uiState.asStateFlow()

    /**
     * Load data using legacy method (for backward compatibility)
     */
    override fun loadData(candidateNumber: Int) {
        presenterScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val visionMission = repository.getVisionMission(candidateNumber)
                _uiState.value = _uiState.value.copy(
                    candidateNumber = visionMission.candidateNumber,
                    vision = visionMission.vision,
                    missions = visionMission.missions,
                    isLoading = false,
                    error = null
                )
                Log.d(TAG, "Successfully loaded legacy data for candidate $candidateNumber")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading legacy data", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Load data from API using pair ID
     */
    override fun loadDataFromAPI(pairId: String) {
        presenterScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val visionMissionDetail = repository.getVisionMissionFromAPI(pairId)
                _uiState.value = _uiState.value.copy(
                    vision = visionMissionDetail.vision,
                    mission = visionMissionDetail.mission,
                    workPrograms = visionMissionDetail.workPrograms,
                    programDocs = visionMissionDetail.programDocs,
                    isLoading = false,
                    error = null
                )
                Log.d(TAG, "Successfully loaded API data for pair $pairId")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading API data for pair $pairId", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load candidate data"
                )
            }
        }
    }

    /**
     * Download program docs PDF dan buka dengan aplikasi eksternal
     */
    override fun downloadProgramDocs(context: Context, pairId: String, candidateName: String) {
        presenterScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloadingPdf = true,
                downloadError = null
            )

            try {
                Log.d(TAG, "Starting PDF download for pair $pairId")

                // Download PDF dari API
                val result = repository.getProgramDocsFromAPI(pairId)

                result.fold(
                    onSuccess = { responseBody ->
                        Log.d(TAG, "PDF downloaded successfully, opening with external app")

                        // Download dan buka PDF
                        val pdfResult = PdfUtils.downloadAndOpenPdf(
                            context = context,
                            responseBody = responseBody,
                            candidateName = candidateName
                        )

                        pdfResult.fold(
                            onSuccess = {
                                _uiState.value = _uiState.value.copy(
                                    isDownloadingPdf = false,
                                    downloadError = null
                                )
                                Log.d(TAG, "PDF successfully opened with external app")
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    isDownloadingPdf = false,
                                    downloadError = "Gagal membuka dokumen: ${error.message}"
                                )
                                Log.e(TAG, "Error opening PDF", error)
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isDownloadingPdf = false,
                            downloadError = "Gagal mengunduh dokumen: ${error.message}"
                        )
                        Log.e(TAG, "Error downloading PDF", error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during PDF download", e)
                _uiState.value = _uiState.value.copy(
                    isDownloadingPdf = false,
                    downloadError = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear download error
     */
    fun clearDownloadError() {
        _uiState.value = _uiState.value.copy(downloadError = null)
    }
}