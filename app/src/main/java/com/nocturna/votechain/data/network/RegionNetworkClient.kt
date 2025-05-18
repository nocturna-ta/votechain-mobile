package com.nocturna.votechain.data.network

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// Models for province and regency data
data class Province(
    val code: String,
    val name: String
)

data class Regency(
    val code: String,
    val name: String
)

// Wrapper classes for API responses
data class ProvinceResponse(
    val data: List<Province>
)

data class RegencyResponse(
    val data: List<Regency>
)

// API Service Interface
interface WilayahApiService {
    @GET("provinces.json")
    suspend fun getProvinces(): ProvinceResponse

    @GET("regencies/{provinceCode}.json")
    suspend fun getRegencies(@Path("provinceCode") provinceCode: String): RegencyResponse
}

// API Client
object WilayahApiClient {
    private const val BASE_URL = "https://wilayah.id/api/"
    private const val TAG = "WilayahApiClient"

    /**
     * Create and configure OkHttpClient with logging and timeouts
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Create and configure Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Create API service instance
     */
    val apiService: WilayahApiService by lazy {
        retrofit.create(WilayahApiService::class.java)
    }
}