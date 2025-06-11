package com.nocturna.votechain.data.network

import com.nocturna.votechain.data.model.AllPartiesResponse
import com.nocturna.votechain.data.model.ElectionPairsResponse
import com.nocturna.votechain.data.model.PartyResponse
import com.nocturna.votechain.data.model.SupportingPartiesResponse
import com.nocturna.votechain.data.model.VisionMissionDetailResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

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

    /**
     * Get detail for a specific election pair
     * Endpoint: /v1/election/pairs/{pairId}/detail
     */
    @GET("v1/election/pairs/{pairId}/detail")
    suspend fun getVisionMissionDetail(@Path("pairId") pairId: String): Response<VisionMissionDetailResponse>

    /**
     * Get supporting parties for a specific election pair
     * Endpoint: /v1/election/pairs/{pairID}/supporting-parties
     */
    @GET("v1/election/pairs/{pairID}/supporting-parties")
    suspend fun getSupportingParties(@Path("pairID") pairId: String): Response<SupportingPartiesResponse>

    /**
     * Get all political parties
     * Endpoint: /v1/party
     */
    @GET("v1/party")
    suspend fun getParties(): Response<PartyResponse>

    /**
     * Get party photo by party ID
     * Endpoint: /v1/party/{id}/photo
     */
    @GET("v1/party/{id}/photo")
    suspend fun getPartyPhoto(
        @Path("id") partyId: String
    ): Response<ResponseBody>
}