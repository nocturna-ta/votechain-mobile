package com.nocturna.votechain.data.model

data class VotingCategory(
    val id: String,
    val title: String,
    val description: String,
    val isActive: Boolean = true
)