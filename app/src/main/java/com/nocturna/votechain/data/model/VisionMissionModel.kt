package com.nocturna.votechain.data.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VisionMissionDetailModel(
    val electionPairId: String,
    val id: String,
    val vision: String,
    val mission: String,
    val programDocs: String?,
    val workPrograms: List<WorkProgram>
)

data class WorkProgram(
    val programName: String,
    val programDesc: List<String>,
    val programPhoto: String?
)

// API Response Models
data class VisionMissionDetailResponse(
    val code: Int,
    val data: VisionMissionDetailData?,
    val error: ErrorResponse?,
    val message: String
)

data class VisionMissionDetailData(
    val election_pair_id: String,
    val id: String,
    val mission: String,
    val program_docs: String?,
    val vision: String,
    val work_program: List<WorkProgramResponse>
)

data class WorkProgramResponse(
    val program_desc: List<String>,
    val program_name: String,
    val program_photo: String?
)

data class ErrorResponse(
    val error_code: Int,
    val error_message: String
)

// Keep the old model for backward compatibility if needed
data class VisionMissionModel(
    val candidateNumber: Int,
    val vision: String,
    val missions: List<String>
)
