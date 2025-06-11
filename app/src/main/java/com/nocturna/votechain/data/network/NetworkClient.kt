package com.nocturna.votechain.data.network

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton class for network client setup
 */
object NetworkClient {

    // Update BASE_URL to the new endpoint
    const val BASE_URL = "https://5cdb-36-69-142-76.ngrok-free.app"
    private const val TAG = "NetworkClient"

    /**
     * Create and configure OkHttpClient with logging and timeouts
     * Made public so it can be used directly if needed
     */
    fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                // Add any custom headers here if needed
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(60, TimeUnit.SECONDS) // Increased timeout for slow connections
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Create and configure Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Create API service instance for user-related operations
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /**
     * Create API service instance for voter-related operations
     */
    val voterApiService: VoterApiService by lazy {
        retrofit.create(VoterApiService::class.java)
    }
}