package com.nocturna.votechain.data.model

/**
 * Data models for OTP verification API endpoints
 */

/**
 * Request model for /v1/otp/generate
 */
data class OTPGenerateRequest(
    val phone_number: String,
    val purpose: String,
    val voter_id: String
)

/**
 * Response model for /v1/otp/generate
 */
data class OTPGenerateResponse(
    val code: Int,
    val data: OTPData?,
    val error: OTPError?,
    val message: String
)

/**
 * OTP data contained in generate response
 */
data class OTPData(
    val expires_at: String,
    val max_attempts: Int,
    val message: String,
    val purpose: String,
    val remaining_attempts: Int,
    val time_remaining_seconds: String,
    val voter_id: String
)

/**
 * Request model for /v1/otp/verify
 */
data class OTPVerifyRequest(
    val code: String,
    val purpose: String,
    val voter_id: String
)

/**
 * Response model for /v1/otp/verify
 */
data class OTPVerifyResponse(
    val code: Int,
    val data: OTPVerifyData?,
    val error: OTPError?,
    val message: String
)

/**
 * OTP verification data contained in verify response
 */
data class OTPVerifyData(
    val is_valid: Boolean,
    val message: String,
    val otp_token: String,
    val purpose: String,
    val token_expiry: String,
    val verified_at: String,
    val voter_id: String
)

/**
 * Error model for OTP API responses
 */
data class OTPError(
    val error_code: Int,
    val error_message: String
)