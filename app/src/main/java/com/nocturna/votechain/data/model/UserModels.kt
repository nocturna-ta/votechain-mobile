package com.nocturna.votechain.data.model

import com.google.gson.annotations.SerializedName

/**
 * Generic API Response structure
 */
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("error")
    val error: ApiError? = null,

    @SerializedName("message")
    val message: String
)

/**
 * API Error structure
 */
data class ApiError(
    @SerializedName("error_code")
    val error_code: Int,

    @SerializedName("error_message")
    val error_message: String
)

/**
 * User Registration Response Data
 */
data class UserRegistrationData(
    @SerializedName("email")
    val email: String,

    @SerializedName("id")
    val id: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("requested_role")
    val requested_role: String,

    @SerializedName("verification_status")
    val verification_status: String
)

/**
 * User Registration Request
 */
data class UserRegistrationRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("nik")
    val nik: String,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("birth_place")
    val birthPlace: String,

    @SerializedName("birth_date")
    val birthDate: String,

    @SerializedName("residential_address")
    val residentialAddress: String,

    @SerializedName("region")
    val region: String,

    @SerializedName("role")
    val role: String = "voter",

    @SerializedName("voter_address")
    val voterAddress: String
)


/**
 * Wallet Data for local storage
 */
data class WalletData(
    val address: String,
    val privateKey: String,
    val publicKey: String,
    val balance: java.math.BigInteger = java.math.BigInteger.ZERO,
    val name: String = "Main Wallet",
    val mnemonic: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Encrypted Wallet Info for secure storage
 */
data class WalletInfo(
    val address: String,
    val encryptedPrivateKey: String,
    val name: String = "Wallet",
    val mnemonic: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)