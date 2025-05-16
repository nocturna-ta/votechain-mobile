package com.nocturna.votechain.data.model

data class VotingResult(
    val categoryId: String,
    val categoryTitle: String,
    val options: List<VotingOption>,
    val totalVotes: Int
)

data class VotingOption(
    val id: String,
    val name: String,
    val votes: Int,
    val percentage: Float
)