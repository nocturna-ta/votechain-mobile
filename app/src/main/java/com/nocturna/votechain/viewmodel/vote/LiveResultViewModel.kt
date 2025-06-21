package com.nocturna.votechain.viewmodel.vote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.VotingResult
import com.nocturna.votechain.data.network.LiveResultsWebSocketManager
import com.nocturna.votechain.data.repository.LiveResultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for live voting results with WebSocket connection
 */
class LiveResultViewModel(
    private val repository: LiveResultRepository = LiveResultRepository()
) : ViewModel() {

    // Live result state
    private val _liveResult = MutableStateFlow<VotingResult?>(null)
    val liveResult: StateFlow<VotingResult?> = _liveResult.asStateFlow()

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
     * Start receiving live results for a specific category
     */
    fun startLiveResults(categoryId: String, regionCode: String? = null) {
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
            repository.getLiveResults(categoryId, regionCode).collect { result ->
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
    fun retryConnection(categoryId: String, regionCode: String? = null) {
        stopLiveResults()
        startLiveResults(categoryId, regionCode)
    }

    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}

/**
 * Factory for creating LiveResultViewModel
 */
class LiveResultViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiveResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LiveResultViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}