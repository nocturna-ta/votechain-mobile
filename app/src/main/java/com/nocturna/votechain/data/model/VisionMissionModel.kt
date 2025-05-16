package com.nocturna.votechain.data.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Model
data class VisionMissionModel(
    val candidateNumber: Int,
    val vision: String,
    val missions: List<String>
)