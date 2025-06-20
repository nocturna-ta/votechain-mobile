package com.nocturna.votechain.data.network

import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.UserRegistrationData
import com.nocturna.votechain.data.model.UserRegistrationRequest
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * API Service interface untuk VoteChain
 */
interface VoteChainApiService {

    @Multipart
    @POST("/v1/user/register")
    suspend fun registerUser(
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("full_name") fullName: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("birth_place") birthPlace: RequestBody,
        @Part("birth_date") birthDate: RequestBody,
        @Part("residential_address") residentialAddress: RequestBody,
        @Part("region") region: RequestBody,
        @Part("role") role: RequestBody,
        @Part("voter_address") voterAddress: RequestBody,
        @Part ktpFile: MultipartBody.Part?
    ): Response<ApiResponse<UserRegistrationData>>

    @POST("/v1/user/login")
    suspend fun loginUser(
        @Body loginRequest: Map<String, String>
    ): Response<ApiResponse<Any>>

    @GET("/v1/user/verify-email")
    suspend fun verifyEmail(
        @Query("token") token: String
    ): Response<ApiResponse<Any>>

    @POST("/v1/user/resend-verification")
    suspend fun resendVerification(
        @Body request: Map<String, String>
    ): Response<ApiResponse<Any>>
}

/**
 * API Client singleton untuk mengelola Retrofit instance
 */
object ApiClient {
    private const val BASE_URL = "https://api.votechain.com/" // Ganti dengan URL API yang sebenarnya

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: VoteChainApiService = retrofit.create(VoteChainApiService::class.java)
}

/**
 * API Repository untuk mengelola panggilan API
 */
class ApiRepository {
    private val apiService = ApiClient.apiService

    suspend fun registerUser(request: UserRegistrationRequest, ktpFile: MultipartBody.Part?): Response<ApiResponse<UserRegistrationData>> {
        return apiService.registerUser(
            email = RequestBody.create(MultipartBody.FORM, request.email),
            password = RequestBody.create(MultipartBody.FORM, request.password),
            nik = RequestBody.create(MultipartBody.FORM, request.nik),
            fullName = RequestBody.create(MultipartBody.FORM, request.fullName),
            gender = RequestBody.create(MultipartBody.FORM, request.gender),
            birthPlace = RequestBody.create(MultipartBody.FORM, request.birthPlace),
            birthDate = RequestBody.create(MultipartBody.FORM, request.birthDate),
            residentialAddress = RequestBody.create(MultipartBody.FORM, request.residentialAddress),
            region = RequestBody.create(MultipartBody.FORM, request.region),
            role = RequestBody.create(MultipartBody.FORM, request.role),
            voterAddress = RequestBody.create(MultipartBody.FORM, request.voterAddress),
            ktpFile = ktpFile
        )
    }

    suspend fun loginUser(email: String, password: String): Response<ApiResponse<Any>> {
        val loginRequest = mapOf(
            "email" to email,
            "password" to password
        )
        return apiService.loginUser(loginRequest)
    }

    suspend fun verifyEmail(token: String): Response<ApiResponse<Any>> {
        return apiService.verifyEmail(token)
    }

    suspend fun resendVerification(email: String): Response<ApiResponse<Any>> {
        val request = mapOf("email" to email)
        return apiService.resendVerification(request)
    }
}