package com.nocturna.votechain.data.repository

import com.nocturna.votechain.data.model.DummyData
import com.nocturna.votechain.data.model.VotingCategory
import com.nocturna.votechain.data.model.VotingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class VotingRepository {
    // Use dummy data flag - set to true for empty voting state
    private val showEmptyState = false

    fun getActiveVotings(): Flow<Result<List<VotingCategory>>> = flow {
        try {
            // Simulate network delay
            delay(1000)

            // Return dummy data
            if (showEmptyState) {
                emit(Result.success(DummyData.emptyVotingCategories))
            } else {
                emit(Result.success(DummyData.activeVotingCategories))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getVotingResults(): Flow<Result<List<VotingResult>>> = flow {
        try {
            // Simulate network delay
            delay(1000)

            // Return dummy data
            emit(Result.success(DummyData.votingResults))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun submitVote(categoryId: String, optionId: String): Flow<Result<Unit>> = flow {
        try {
            // Simulate network delay
            delay(1000)

            // Simulate successful vote
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    // To be implemented later when connecting to real API
    /*
    private val apiService = RetrofitClient.apiService

    fun getActiveVotingsFromApi(): Flow<Result<List<VotingCategory>>> = flow {
        try {
            val response = apiService.getActiveVotings()
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Failed to fetch active votings: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    */
}