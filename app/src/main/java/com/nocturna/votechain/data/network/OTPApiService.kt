package com.nocturna.votechain.data.network

import com.nocturna.votechain.data.model.OTPGenerateRequest
import com.nocturna.votechain.data.model.OTPGenerateResponse
import com.nocturna.votechain.data.model.OTPVerifyRequest
import com.nocturna.votechain.data.model.OTPVerifyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API Service interface for OTP voting verification endpoints
 */
interface OTPApiService {
    /**
     * Generate OTP for voting
     * Endpoint: /v1/otp/generate
     */
    @POST("v1/otp/generate")
    suspend fun generateOTP(
        @Header("Authorization") token: String,
        @Body request: OTPGenerateRequest
    ): Response<OTPGenerateResponse>

    /**
     * Resend OTP for voting
     * Endpoint: /v1/otp/resend
     */
    @POST("v1/otp/resend")
    suspend fun resendOTP(
        @Header("Authorization") token: String,
        @Body request: OTPGenerateRequest
    ): Response<OTPGenerateResponse>

    /**
     * Verify OTP for voting
     * Endpoint: /v1/otp/verify
     */
    @POST("v1/otp/verify")
    suspend fun verifyOTP(
        @Header("Authorization") token: String,
        @Body request: OTPVerifyRequest
    ): Response<OTPVerifyResponse>
}