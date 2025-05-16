package com.nocturna.votechain.data.model

data class CandidatePersonalInfo(
    val id: String,
    val fullName: String,
    val position: String,
    val gender: String,
    val birthInfo: String,
    val religion: String,
    val education: String,
    val occupation: String,
    val photoResId: Int
)

data class EducationEntry(
    val institution: String,
    val period: String
)

data class WorkEntry(
    val institution: String,
    val position: String,
    val period: String
)

data class CandidateDetailData(
    val personalInfo: CandidatePersonalInfo,
    val educationHistory: List<EducationEntry>,
    val workHistory: List<WorkEntry>
)