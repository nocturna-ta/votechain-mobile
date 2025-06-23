// Update untuk LiveResultViewModel.kt

package com.nocturna.votechain.viewmodel.vote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.LiveElectionData
import com.nocturna.votechain.data.model.VotingResult
import com.nocturna.votechain.data.network.LiveResultsWebSocketManager
import com.nocturna.votechain.data.repository.LiveResultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for live voting results with WebSocket connection
 * Enhanced to handle raw live data
 */
class LiveResultViewModel(
    private val repository: LiveResultRepository = LiveResultRepository()
) : ViewModel() {

    // Live result state
    private val _liveResult = MutableStateFlow<VotingResult?>(null)
    val liveResult: StateFlow<VotingResult?> = _liveResult.asStateFlow()

    // Raw live data from WebSocket
    private val _rawLiveData = MutableStateFlow<LiveElectionData?>(null)
    val rawLiveData: StateFlow<LiveElectionData?> = _rawLiveData.asStateFlow()

    // Connection state
    private val _connectionState = MutableStateFlow(LiveResultsWebSocketManager.ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<LiveResultsWebSocketManager.ConnectionState> = _connectionState.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Start receiving live results for a specific election
     * @param electionId The election ID to monitor
     * @param regionCode Optional region filter
     */
    fun startLiveResults(electionId: String, regionCode: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Observe connection state
            repository.getConnectionState().collect { state ->
                _connectionState.value = state
            }
        }

        viewModelScope.launch {
            // Observe live results
            repository.getLiveResults(electionId, regionCode).collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = { votingResult ->
                        _liveResult.value = votingResult
                        _error.value = null
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                    }
                )
            }
        }

        // Also observe raw live data if available
        viewModelScope.launch {
            repository.getRawLiveData().collect { data ->
                _rawLiveData.value = data
            }
        }
    }

    /**
     * Stop receiving live results
     */
    fun stopLiveResults() {
        repository.disconnect()
        _connectionState.value = LiveResultsWebSocketManager.ConnectionState.DISCONNECTED
    }

    /**
     * Retry connection
     */
    fun retryConnection(electionId: String, regionCode: String? = null) {
        stopLiveResults()
        startLiveResults(electionId, regionCode)
    }

    override fun onCleared() {
        super.onCleared()
        stopLiveResults()
        repository.cleanup()
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LiveResultViewModel::class.java)) {
                        return LiveResultViewModel() as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}