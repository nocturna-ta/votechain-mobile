package com.nocturna.votechain.data.network

import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.LoginRequest
import com.nocturna.votechain.data.model.RegisterRequest
import com.nocturna.votechain.data.model.UserLoginData
import com.nocturna.votechain.data.model.UserRegistrationData
import com.nocturna.votechain.data.model.VerificationStatusData
import com.nocturna.votechain.data.model.VoterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * API Service interface for network requests
 */
interface ApiService {
    /**
     * Register a new user through JSON request
     * Endpoint: /v1/user/register
     */
    @POST("v1/user/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<ApiResponse<UserRegistrationData>>

    /**
     * Register a new user with form data and file upload
     * Endpoint: /v1/user/register
     */
    @Multipart
    @POST("v1/user/register")
    suspend fun registerUserWithFormData(
        @Part("user") user: RequestBody,
        @Part ktp_photo: MultipartBody.Part?
    ): Response<ApiResponse<UserRegistrationData>>

    /**
     * Login a user
     * Endpoint: /v1/user/login
     */
    @POST("v1/user/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<ApiResponse<UserLoginData>>

    /**
     * Get voter information
     * Endpoint: /v1/voter
     */
    @GET("v1/voter")
    suspend fun getVoterData(
        @Header("Authorization") token: String
    ): Response<VoterResponse>

    /**
     * Get user verification status by email
     * Endpoint: /v1/user/verification-status/{email}
     */
    @GET("v1/user/verification-status/{email}")
    suspend fun getVerificationStatus(
        @Path("email") email: String
    ): Response<ApiResponse<VerificationStatusData>>
}