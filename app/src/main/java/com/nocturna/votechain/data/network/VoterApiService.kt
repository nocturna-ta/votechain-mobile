package com.nocturna.votechain.data.network

import com.nocturna.votechain.data.model.VoterResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * API Service interface for voter-related endpoints
 */
interface VoterApiService {
    /**
     * Get voter information by user email
     * Endpoint: /v1/voter
     */
    @GET("v1/voter")
    suspend fun getVoterData(
        @Header("Authorization") token: String
    ): Response<VoterResponse>
}