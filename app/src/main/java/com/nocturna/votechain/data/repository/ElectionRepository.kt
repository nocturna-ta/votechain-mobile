package com.nocturna.votechain.data.repository

import android.util.Log
import com.nocturna.votechain.data.model.ElectionPair
import com.nocturna.votechain.data.model.Party
import com.nocturna.votechain.data.model.SupportingParty
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

    /**
     * Get supporting parties for a specific election pair
     * @param pairId The election pair ID
     * @return Flow of supporting parties result
     */
    fun getSupportingParties(pairId: String): Flow<Result<List<SupportingParty>>> = flow {
        try {
            Log.d(TAG, "Fetching supporting parties for pair ID: $pairId")
            val response = apiService.getSupportingParties(pairId)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null && responseBody.code == 200) {
                    val parties = responseBody.data?.parties ?: emptyList()
                    Log.d(TAG, "Successfully fetched ${parties.size} supporting parties")
                    emit(Result.success(parties))
                } else {
                    val errorMsg = responseBody?.error?.error_message ?: "Unknown error occurred"
                    Log.e(TAG, "API returned error: $errorMsg")
                    emit(Result.failure(Exception(errorMsg)))
                }
            } else {
                Log.e(TAG, "API call failed with code: ${response.code()}")
                emit(Result.failure(Exception("Failed to fetch supporting parties: ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching supporting parties: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get all political parties
     * @return Flow of all parties result
     */
    fun getAllParties(): Flow<Result<List<Party>>> = flow {
        try {
            Log.d(TAG, "Fetching all parties from API")
            val response = apiService.getAllParties()

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null && responseBody.code == 200) {
                    val parties = responseBody.data ?: emptyList()
                    Log.d(TAG, "Successfully fetched ${parties.size} parties")
                    emit(Result.success(parties))
                } else {
                    val errorMsg = responseBody?.error?.error_message ?: "Unknown error occurred"
                    Log.e(TAG, "API returned error: $errorMsg")
                    emit(Result.failure(Exception(errorMsg)))
                }
            } else {
                Log.e(TAG, "API call failed with code: ${response.code()}")
                emit(Result.failure(Exception("Failed to fetch all parties: ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching all parties: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get election pairs with their supporting parties
     * @return Flow of election pairs with supporting parties
     */
    fun getElectionPairsWithSupportingParties(): Flow<Result<List<ElectionPair>>> = flow {
        try {
            Log.d(TAG, "Fetching election pairs with supporting parties")

            // First, get all election pairs
            val pairsResponse = apiService.getElectionPairs()
            if (!pairsResponse.isSuccessful) {
                emit(Result.failure(Exception("Failed to fetch election pairs")))
                return@flow
            }

            val pairs = pairsResponse.body()?.data?.pairs ?: emptyList()
            val pairsWithParties = mutableListOf<ElectionPair>()

            // For each pair, fetch supporting parties
            for (pair in pairs) {
                try {
                    val partiesResponse = apiService.getSupportingParties(pair.id)
                    val supportingParties = if (partiesResponse.isSuccessful) {
                        partiesResponse.body()?.data?.parties ?: emptyList()
                    } else {
                        emptyList()
                    }

                    // Create updated pair with supporting parties
                    val updatedPair = pair.copy(supporting_parties = supportingParties)
                    pairsWithParties.add(updatedPair)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to fetch supporting parties for pair ${pair.id}: ${e.message}")
                    // Add pair without supporting parties
                    pairsWithParties.add(pair.copy(supporting_parties = emptyList()))
                }
            }

            Log.d(TAG, "Successfully fetched ${pairsWithParties.size} election pairs with supporting parties")
            emit(Result.success(pairsWithParties))

        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching election pairs with supporting parties: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}