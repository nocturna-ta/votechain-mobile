package com.nocturna.votechain.data.repository

import com.nocturna.votechain.data.model.PartyElectionPair
import com.nocturna.votechain.data.model.PartyResponse
import com.nocturna.votechain.data.network.ElectionNetworkClient
import com.nocturna.votechain.data.network.PartyPhotoHelper

class PartyRepository {
    private val electionApiService = ElectionNetworkClient.electionApiService

    /**
     * Get parties data from API
     */
    suspend fun getParties(): Result<PartyResponse> {
        return try {
            val response = electionApiService.getParties()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get party photo URLs from party data
     */
    fun getPartyPhotoUrls(parties: List<PartyElectionPair>): Map<String, String> {
        return parties.associate { party ->
            party.party.name to PartyPhotoHelper.getPartyPhotoUrl(party.party.id)
        }
    }
}