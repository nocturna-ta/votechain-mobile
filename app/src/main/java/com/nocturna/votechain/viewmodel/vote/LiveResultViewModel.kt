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

    // Multiple elections data - Map dari election_id ke LiveElectionData
    private val _allElectionsData = MutableStateFlow<Map<String, LiveElectionData>>(emptyMap())
    val allElectionsData: StateFlow<Map<String, LiveElectionData>> = _allElectionsData.asStateFlow()

    // Aggregated data dari semua elections
    private val _aggregatedElectionData = MutableStateFlow<AggregatedElectionStats?>(null)
    val aggregatedElectionData: StateFlow<AggregatedElectionStats?> = _aggregatedElectionData.asStateFlow()

    // Connection state
    private val _connectionState = MutableStateFlow(LiveResultsWebSocketManager.ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<LiveResultsWebSocketManager.ConnectionState> = _connectionState.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Set untuk tracking active election subscriptions
    private val activeElectionSubscriptions = mutableSetOf<String>()

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

                // Update specific election data in map
                data?.let { liveData ->
                    val currentMap = _allElectionsData.value.toMutableMap()
                    currentMap[liveData.electionId] = liveData
                    _allElectionsData.value = currentMap

                    // Update aggregated data
                    updateAggregatedData()
                }
            }
        }
    }

    /**
     * Start monitoring multiple elections
     */
    fun startMultipleElections(electionIds: List<String>) {
        electionIds.forEach { electionId ->
            if (!activeElectionSubscriptions.contains(electionId)) {
                activeElectionSubscriptions.add(electionId)
                subscribeToElection(electionId)
            }
        }
    }

    /**
     * Subscribe to specific election untuk multiple election monitoring
     */
    private fun subscribeToElection(electionId: String) {
        viewModelScope.launch {
            try {
                // Dalam implementasi nyata, ini akan subscribe ke WebSocket
                // untuk election_id spesifik
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to subscribe to election $electionId: ${e.message}"
            }
        }
    }

    /**
     * Update aggregated data dari semua elections
     */
    private fun updateAggregatedData() {
        val allData = _allElectionsData.value
        if (allData.isNotEmpty()) {
            val aggregated = AggregatedElectionStats(
                totalElections = allData.size,
                totalVotes = allData.values.sumOf { it.totalVotes },
                totalVoters = allData.values.firstOrNull()?.totalVoters ?: 0,
                averageParticipation = allData.values.map { it.overallPercentage }.average(),
                electionsData = allData
            )
            _aggregatedElectionData.value = aggregated
        }
    }

    /**
     * Simulate live data untuk multiple elections
     * Dalam implementasi nyata, ini akan datang dari WebSocket
     */
    fun simulateMultipleElectionData(baseElectionData: LiveElectionData, electionIds: List<String>) {
        viewModelScope.launch {
            val simulatedData = mutableMapOf<String, LiveElectionData>()

            electionIds.forEach { electionId ->
                // Simulasi variasi data untuk setiap election
                val multiplier = when (electionId) {
                    electionIds.getOrNull(0) -> 1.2 // Election pertama lebih tinggi
                    electionIds.getOrNull(1) -> 0.8 // Election kedua lebih rendah
                    electionIds.getOrNull(2) -> 1.1 // Election ketiga sedang
                    else -> kotlin.random.Random.nextDouble(0.5, 1.5)
                }

                val simulatedElectionData = baseElectionData.copy(
                    electionId = electionId,
                    totalVotes = (baseElectionData.totalVotes * multiplier).toInt(),
                    overallPercentage = baseElectionData.overallPercentage * multiplier,
                    regions = baseElectionData.regions.map { region ->
                        region.copy(
                            votes = (region.votes * multiplier).toInt(),
                            percentage = region.percentage * multiplier
                        )
                    },
                    topCities = baseElectionData.topCities.map { city ->
                        city.copy(
                            votes = (city.votes * multiplier).toInt(),
                            percentage = city.percentage * multiplier
                        )
                    }
                )

                simulatedData[electionId] = simulatedElectionData
            }

            _allElectionsData.value = simulatedData
            updateAggregatedData()
        }
    }

    /**
     * Get election data untuk specific election ID
     */
    fun getElectionData(electionId: String): LiveElectionData? {
        return _allElectionsData.value[electionId]
    }

    /**
     * Get top performing elections berdasarkan total votes
     */
    fun getTopPerformingElections(limit: Int = 5): List<Pair<String, LiveElectionData>> {
        return _allElectionsData.value.toList()
            .sortedByDescending { it.second.totalVotes }
            .take(limit)
    }

    /**
     * Get election performance comparison
     */
    fun getElectionComparison(): List<ElectionComparison> {
        val allData = _allElectionsData.value
        return allData.map { (electionId, data) ->
            ElectionComparison(
                electionId = electionId,
                totalVotes = data.totalVotes,
                participationRate = data.overallPercentage,
                topRegion = data.regions.maxByOrNull { it.votes }?.region ?: "Unknown",
                averageVotesPerRegion = data.regions.map { it.votes }.average()
            )
        }.sortedByDescending { it.totalVotes }
    }

    /**
     * Stop receiving live results
     */
    fun stopLiveResults() {
        repository.disconnect()
        _connectionState.value = LiveResultsWebSocketManager.ConnectionState.DISCONNECTED
        activeElectionSubscriptions.clear()
    }

    /**
     * Retry connection
     */
    fun retryConnection(electionId: String, regionCode: String? = null) {
        stopLiveResults()
        startLiveResults(electionId, regionCode)
    }

    /**
     * Refresh all election data
     */
    fun refreshAllElections() {
        val currentElections = activeElectionSubscriptions.toList()
        stopLiveResults()
        startMultipleElections(currentElections)
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

/**
 * Data class untuk aggregated election statistics
 */
data class AggregatedElectionStats(
    val totalElections: Int,
    val totalVotes: Int,
    val totalVoters: Int,
    val averageParticipation: Double,
    val electionsData: Map<String, LiveElectionData>
)

/**
 * Data class untuk election comparison
 */
data class ElectionComparison(
    val electionId: String,
    val totalVotes: Int,
    val participationRate: Double,
    val topRegion: String,
    val averageVotesPerRegion: Double
)