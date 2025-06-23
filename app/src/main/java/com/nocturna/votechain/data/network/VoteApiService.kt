package com.nocturna.votechain.data.network

import com.nocturna.votechain.data.model.VoteCastData
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
    suspend fun castVoteWithOTP(
        @Header("Authorization") token: String,
        @Body request: VoteCastRequest
    ): Response<VoteCastResponse>
}

/**
 * Extension function untuk VoteApiService agar mendukung Enhanced request
 */
suspend fun VoteApiService.castVote(
    token: String,
    request: VoteCastRequest
): Response<VoteCastResponse> {
    // Convert EnhancedVoteCastRequest ke VoteCastRequest yang sudah ada
    val legacyRequest = com.nocturna.votechain.data.model.VoteCastRequest(
        election_pair_id = request.election_pair_id,
        region = request.region,
        signed_transaction = request.signed_transaction,
        voter_id = request.voter_id,
        otp_token = request.otp_token
    )

    // Call existing API and convert response
    val legacyResponse = this.castVoteWithOTP(token, legacyRequest)

    // Convert response (you'll need to map the response accordingly)
    return Response.success(
        legacyResponse.code(),
        VoteCastResponse(
            code = legacyResponse.body()?.code ?: -1,
            data = legacyResponse.body()?.data?.let {
                VoteCastData(
                    id = it.id,
                    status = it.status,
                    tx_hash = it.tx_hash,
                    voted_at = it.voted_at
                )
            },
            error = legacyResponse.body()?.error,
            message = legacyResponse.body()?.message ?: ""
        )
    )
}