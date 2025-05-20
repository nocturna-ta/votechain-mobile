package com.nocturna.votechain.data.repository

import android.util.Log
import com.nocturna.votechain.data.model.ElectionPair
import com.nocturna.votechain.data.network.ElectionNetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Repository for election-related data
 */
class ElectionRepository {
    private val TAG = "ElectionRepository"
    private val apiService = ElectionNetworkClient.electionApiService

    /**
     * Get all election candidate pairs
     * @return Flow of election pairs result
     */
    fun getElectionPairs(): Flow<Result<List<ElectionPair>>> = flow {
        try {
            Log.d(TAG, "Fetching election pairs from API")
            val response = apiService.getElectionPairs()

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null && responseBody.code == 200) {
                    val pairs = responseBody.data?.pairs ?: emptyList()
                    Log.d(TAG, "Successfully fetched ${pairs.size} election pairs")
                    emit(Result.success(pairs))
                } else {
                    val errorMsg = responseBody?.error?.error_message ?: "Unknown error occurred"
                    Log.e(TAG, "API returned error: $errorMsg")
                    emit(Result.failure(Exception(errorMsg)))
                }
            } else {
                Log.e(TAG, "API call failed with code: ${response.code()}")
                emit(Result.failure(Exception("Failed to fetch election pairs: ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching election pairs: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}