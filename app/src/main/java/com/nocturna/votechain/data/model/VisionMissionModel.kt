package com.nocturna.votechain.data.model

/**
 * Data models for Vision Mission Detail API response
 */

// Main API Response for Vision Mission Detail
data class VisionMissionDetailResponse(
    val code: Int,
    val data: VisionMissionDetailData?,
    val error: ApiError?
)

// Data container for vision mission detail
data class VisionMissionDetailData(
    val id: String,
    val election_pair_id: String,
    val vision: String,
    val mission: String,
    val work_program: List<WorkProgramResponse>,
    val program_docs: String?
)

// Work program response from API
data class WorkProgramResponse(
    val program_name: String,
    val program_photo: String?,
    val program_desc: List<String>
)

// Work program model for UI (matches existing model in ViewModel)
data class WorkProgram(
    val programName: String,
    val programPhoto: String?,
    val programDesc: List<String>
)

// Model for repository return (combines API data in UI-friendly format)
data class VisionMissionDetailModel(
    val id: String,
    val electionPairId: String,
    val vision: String,
    val mission: String,
    val workPrograms: List<WorkProgram>,
    val programDocs: String?
)

// Keep the old model for backward compatibility
data class VisionMissionModel(
    val candidateNumber: Int,
    val vision: String,
    val missions: List<String>
)
//
//// API Error model for consistent error handling
//data class ApiError(
//    val error_message: String,
//    val error_code: String? = null
//)