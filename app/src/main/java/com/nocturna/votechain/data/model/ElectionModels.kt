package com.nocturna.votechain.data.model

/**
 * Data models for election candidate pairs API response
 */

// Main response wrapper
data class ElectionPairsResponse(
    val code: Int,
    val message: String,
    val data: ElectionPairsData?,
    val error: ApiError?
)

// Data containing pairs and total count
data class ElectionPairsData(
    val pairs: List<ElectionPair>,
    val total: Int
)

// Error information
data class ElectionApiError(
    val error_code: Int,
    val error_message: String
)

// Single election pair (president and vice president)
data class ElectionPair(
    val id: String,
    val election_no: String,
    val is_active: Boolean,
    val pair_photo_path: String,
    val president: Candidate,
    val vice_president: Candidate,
    val vote_count: Int,
    val supporting_parties: List<SupportingParty>? = null
)

// Candidate information (used for both president and vice president)
data class Candidate(
    val full_name: String,
    val gender: String,
    val birth_place: String,
    val birth_date: String,
    val religion: String,
    val last_education: String,
    val job: String,
    val photo_path: String,
    val education_history: List<Education>,
    val work_experience: List<WorkExperience>
)

// Education history entry
data class Education(
    val institute_name: String,
    val year: String
)

// Work experience entry
data class WorkExperience(
    val institute_name: String,
    val position: String,
    val year: String
)

// Supporting party data model
data class SupportingParty(
    val id: String,
    val election_pair_id: String,
    val party_id: String,
    val party: Party
)

// Party information
data class Party(
    val id: String,
    val name: String,
    val logo_path: String
)

// Supporting parties response wrapper
data class SupportingPartiesResponse(
    val code: Int,
    val message: String,
    val data: SupportingPartiesData?,
    val error: ApiError?
)

// Supporting parties data container
data class SupportingPartiesData(
    val parties: List<SupportingParty>
)

// All parties response wrapper
data class AllPartiesResponse(
    val code: Int,
    val message: String,
    val data: List<Party>?,
    val error: ApiError?
)

// Generic API error
//data class ApiError(
//    val error_code: Int,
//    val error_message: String
//)