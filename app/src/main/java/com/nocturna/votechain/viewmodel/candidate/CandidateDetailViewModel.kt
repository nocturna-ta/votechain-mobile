package com.nocturna.votechain.viewmodel.candidate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.CandidateDetailData
import com.nocturna.votechain.domain.GetCandidateDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Candidate Detail Screen
 */
class CandidateDetailViewModel(
    private val getCandidateDetailUseCase: GetCandidateDetail
) : ViewModel() {

    private val _uiState = MutableStateFlow<CandidateDetailUiState>(CandidateDetailUiState.Loading)
    val uiState: StateFlow<CandidateDetailUiState> = _uiState.asStateFlow()

    /**
     * Fetch candidate details by ID
     */
    fun fetchCandidateDetail(candidateId: String) {
        viewModelScope.launch {
            _uiState.value = CandidateDetailUiState.Loading
            try {
                val candidateData = getCandidateDetailUseCase(candidateId)
                _uiState.value = CandidateDetailUiState.Success(candidateData)
            } catch (e: Exception) {
                _uiState.value = CandidateDetailUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * UI State for Candidate Detail Screen
     */
    sealed class CandidateDetailUiState {
        data object Loading : CandidateDetailUiState()
        data class Success(val data: CandidateDetailData) : CandidateDetailUiState()
        data class Error(val message: String) : CandidateDetailUiState()
    }

    /**
     * Factory for creating CandidateDetailViewModel
     */
    class Factory(
        private val getCandidateDetailUseCase: GetCandidateDetail
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CandidateDetailViewModel::class.java)) {
                return CandidateDetailViewModel(getCandidateDetailUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}