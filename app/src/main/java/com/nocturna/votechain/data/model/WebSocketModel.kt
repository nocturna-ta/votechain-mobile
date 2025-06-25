package com.nocturna.votechain.data.model

import com.google.gson.annotations.SerializedName

/**
 * Live results update message from WebSocket
 * Fixed percentage fields to be Double instead of Int
 */
data class LiveResultsUpdateMessage(
    @SerializedName("type")
    val type: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("data")
    val data: LiveElectionData,
    @SerializedName("filter")
    val filter: LiveResultFilter
)

data class LiveElectionData(
    @SerializedName("election_id")
    val electionId: String,
    @SerializedName("total_votes")
    val totalVotes: Int,
    @SerializedName("total_voters")
    val totalVoters: Int,
    @SerializedName("last_updated")
    val lastUpdated: String,
    @SerializedName("overall_percentage")
    val overallPercentage: Double,
    @SerializedName("regions")
    val regions: List<LiveRegionResult>,
    @SerializedName("top_cities")
    val topCities: List<LiveCityResult>,
    @SerializedName("stats")
    val stats: LiveElectionStats
)

data class LiveRegionResult(
    @SerializedName("region")
    val region: String,
    @SerializedName("votes")
    val votes: Int,
    @SerializedName("percentage")
    val percentage: Double // Changed from Int to Double
)

data class LiveCityResult(
    @SerializedName("city")
    val city: String,
    @SerializedName("votes")
    val votes: Int,
    @SerializedName("voters")
    val voters: Int,
    @SerializedName("percentage")
    val percentage: Double, // Changed from Int to Double
    @SerializedName("rank")
    val rank: Int
)

data class LiveElectionStats(
    @SerializedName("total_voters")
    val totalVoters: Int,
    @SerializedName("total_regions")
    val totalRegions: Int,
    @SerializedName("success_rate")
    val successRate: Double,
    @SerializedName("votes_per_second")
    val votesPerSecond: Double,
    @SerializedName("active_regions")
    val activeRegions: Int,
    @SerializedName("completion_rate")
    val completionRate: Double
)

data class LiveResultFilter(
    @SerializedName("election_pair_id")
    val electionPairId: String
)