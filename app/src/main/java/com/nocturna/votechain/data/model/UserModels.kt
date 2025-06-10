package com.nocturna.votechain.data.model

/**
 * Data class for register request body with updated structure
 * to match the required API format
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String = "voter",
    val address: String = "",  // Ethereum wallet address
    val nik: String = "",
    val full_name: String = "",
    val gender: String = "",
    val birth_place: String = "",
    val birth_date: String = "",
    val residential_address: String = "",
    val ktp_photo_path: String = "",
    val kpu_name: String = "",
    val region: String = "",
    val telephone: String = "",
)

/**
 * Data class for register response that matches the actual API structure
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
    val error: ApiError?
)

/**
 * Data class for API error information
 */
data class ApiError(
    val error_code: Int,
    val error_message: String
)

/**
 * Data class for user registration response data
 */
data class UserRegistrationData(
    val email: String,
    val id: String,
    val message: String,
    val requested_role: String,
    val verification_status: String
)

/**
 * Data class for login request body
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Data class for user login response data
 */
data class UserLoginData(
    val token: String,
    val message: String,
    val is_active: Boolean,
    val requested_role: String,
    val verification_status: String,
    val expires_at: String
)

/**
 * Data class for verification status response data
 * Used for GET /v1/user/verification-status/{email} endpoint
 */
data class VerificationStatusData(
    val id: String,
    val email: String,
    val requested_role: String,
    val verification_status: String,
    val created_at: String
)