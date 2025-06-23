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
    val telephone: String,
    val voter_address: String,
    val region: String,
    val is_registered: Boolean,
    val has_voted: Boolean
)

/**
 * Enhanced data class for wallet information (for UI display)
 */
data class WalletInfo(
    val balance: String = "0.00000000",
    val privateKey: String = "",
    val publicKey: String = "",
    val voterAddress: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = ""
)

/**
 * Complete user data combining all user information
 */
data class CompleteUserData(
    val userProfile: UserProfile? = null,
    val voterData: VoterData? = null,
    val walletInfo: WalletInfo = WalletInfo()
)

/**
 * Data class for displaying account information in UI
 */
data class AccountDisplayData(
    val fullName: String = "",
    val nik: String = "",
    val email: String = "",
    val ethBalance: String = "0.00000000",
    val publicKey: String = "",
    val privateKey: String = "",
    val voterAddress: String = "",
    val hasVoted: Boolean = false,
    val isDataLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * User profile data class (if not already defined elsewhere)
 */
data class UserProfile(
    val id: String = "",
    val email: String = "",
    val created_at: String = "",
    val updated_at: String = ""
)