package com.nocturna.votechain.data.model

/**
 * Enhanced user profile data class including wallet and voting info with keys
 */
data class EnhancedUserProfile(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val nik: String = "",
    val address: String = "",
    val gender: String = "",
    val birthPlace: String = "",
    val birthDate: String = "",
    val telephone: String = "",
    val residentialAddress: String = "",
    val isRegistered: Boolean = false,
    val hasVoted: Boolean = false,
    val voterAddress: String = "",
    val balance: String = "0.0",
    val publicKey: String = "",  // Added public key field
    val privateKey: String = "",  // Added private key field (should be handled with care)
    val region: String = ""
)
