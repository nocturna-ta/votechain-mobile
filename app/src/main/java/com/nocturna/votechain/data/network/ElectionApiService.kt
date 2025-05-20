package com.nocturna.votechain.data.network

import com.nocturna.votechain.data.model.ElectionPairsResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * API Service interface for election-related endpoints
 */
interface ElectionApiService {
    /**
     * Get all election candidate pairs
     * Endpoint: /v1/election/pairs
     */
    @GET("v1/election/pairs")
    suspend fun getElectionPairs(): Response<ElectionPairsResponse>
}