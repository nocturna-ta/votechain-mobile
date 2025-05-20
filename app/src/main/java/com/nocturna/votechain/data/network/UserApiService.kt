package com.nocturna.votechain.data.network

import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.LoginRequest
import com.nocturna.votechain.data.model.RegisterRequest
import com.nocturna.votechain.data.model.UserLoginData
import com.nocturna.votechain.data.model.UserRegistrationData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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
}