package com.nocturna.votechain.data.network

import com.nocturna.votechain.data.model.VoteCastRequest
import com.nocturna.votechain.data.model.VoteCastResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API Service interface for voting endpoints
 */
interface VoteApiService {
    /**
     * Cast a vote
     * Endpoint: /v1/vote/cast
     */
    @POST("v1/vote/cast")
    suspend fun castVote(
        @Header("Authorization") token: String,
        @Body request: VoteCastRequest
    ): Response<VoteCastResponse>
}