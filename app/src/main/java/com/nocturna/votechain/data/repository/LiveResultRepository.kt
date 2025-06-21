package com.nocturna.votechain.data.repository

import com.nocturna.votechain.data.model.VotingResult
import com.nocturna.votechain.data.network.LiveResultsWebSocketManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Repository for live voting results using WebSocket connection
 * Replaces the dummy data with real-time results from the API
 */
class LiveResultRepository {
    private val webSocketManager = LiveResultsWebSocketManager()

    /**
     * Get live results with real-time updates
     */
    fun getLiveResults(categoryId: String, regionCode: String? = null): Flow<Result<VotingResult?>> {
        // Connect to WebSocket if not already connected
        webSocketManager.connect()

        // Combine live results with error state to return proper Result type
        return combine(
            webSocketManager.liveResults,
            webSocketManager.error
        ) { liveResult, error ->
            when {
                error != null -> Result.failure(Exception(error))
                liveResult != null -> {
                    // Filter by category and region if needed
                    val filteredResult = filterResult(liveResult, categoryId, regionCode)
                    Result.success(filteredResult)
                }
                else -> Result.success(null)
            }
        }
    }

    /**
     * Get connection state
     */
    fun getConnectionState(): Flow<LiveResultsWebSocketManager.ConnectionState> {
        return webSocketManager.connectionState
    }

    /**
     * Disconnect from live updates
     */
    fun disconnect() {
        webSocketManager.disconnect()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        webSocketManager.cleanup()
    }

    /**
     * Filter results based on category and region
     * Adapt this method based on your actual data structure
     */
    private fun filterResult(result: VotingResult, categoryId: String, regionCode: String?): VotingResult {
        // Apply filtering logic here if needed
        // For now, return the result as is
        return result
    }
}