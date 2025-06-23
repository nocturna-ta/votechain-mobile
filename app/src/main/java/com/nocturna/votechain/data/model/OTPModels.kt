package com.nocturna.votechain.data.model

/**
 * Data models for OTP verification
 */
data class OTPGenerateRequest(
    val phone_number: String,
    val purpose: String,
    val voter_id: String
)

data class OTPGenerateResponse(
    val code: Int,
    val data: OTPData?,
    val error: OTPError?,
    val message: String
)

data class OTPData(
    val expires_at: String,
    val max_attempts: Int,
    val message: String,
    val purpose: String,
    val remaining_attempts: Int,
    val time_remaining_seconds: String,
    val voter_id: String
)

data class OTPError(
    val error_code: Int,
    val error_message: String
)

data class OTPVerifyRequest(
    val phone_number: String,
    val purpose: String,
    val voter_id: String,
    val otp_code: String
)

data class OTPVerifyResponse(
    val code: Int,
    val data: OTPVerifyData?,
    val error: OTPError?,
    val message: String
)

data class OTPVerifyData(
    val is_valid: Boolean,
    val message: String,
    val voter_id: String
)