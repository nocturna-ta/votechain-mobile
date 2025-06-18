package com.nocturna.votechain.data.model

/**
 * Data class for register request body with updated structure
 * to match the required API format
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String = "voter",
    val address: String = "",
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
    val expires_at: String,
    val is_active: Boolean,
    val message: String,
    val requested_role: String,
    val token: String,
    val verification_status: String,

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

data class UserProfileData(
    val email: String,
    val id: String,
    val role: String,
    val publicAddress: String? = null,
    val userId: String? = null,
    val userRole: String? = null
)

data class UserProfileResponse(
    val code: Int,
    val data: UserProfileData?,
    val error: ApiError?,
    val message: String
)

data class CompleteUserProfile(
    val userProfile: UserProfileData,
    val voterProfile: VoterData?
)