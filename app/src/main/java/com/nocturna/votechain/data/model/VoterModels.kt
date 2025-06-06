package com.nocturna.votechain.data.model

/**
 * Data class for voter information response
 */
data class VoterResponse(
    val data: List<VoterData>,
    val code: Int
)

/**
 * Data class for individual voter data
 */
data class VoterData(
    val id: String,
    val user_id: String,
    val nik: String,
    val full_name: String,
    val gender: String,
    val birth_place: String,
    val birth_date: String,
    val residential_address: String,
    val voter_address: String,
    val region: String,
    val is_registered: Boolean,
    val has_voted: Boolean
)

/**
 * Data class for wallet information (for UI display)
 */
data class WalletInfo(
    val balance: String = "0.0000",
    val privateKey: String = "",
    val publicKey: String = ""
)