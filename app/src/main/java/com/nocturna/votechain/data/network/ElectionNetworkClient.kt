package com.nocturna.votechain.data.network

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.nocturna.votechain.data.model.PartyResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network client for connecting to the Election API
 */
object ElectionNetworkClient {
    const val BASE_URL = "https://85a1-103-233-100-202.ngrok-free.app/"
    private const val TAG = "ElectionNetworkClient"
    private const val PREFS_NAME = "VoteChainPrefs"
    private const val KEY_USER_TOKEN = "user_token"

    private var applicationContext: Context? = null

    /**
     * Initialize the ElectionNetworkClient with application context
     */
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    /**
     * Get stored user token from SharedPreferences
     */
    private fun getUserToken(): String {
        return applicationContext?.let { context ->
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences.getString(KEY_USER_TOKEN, "") ?: ""
        } ?: ""
    }

    /**
     * Create authentication interceptor for election API
     */
    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val token = getUserToken()

            val newRequest = if (token.isNotEmpty()) {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()
            } else {
                originalRequest.newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()
            }

            chain.proceed(newRequest)
        }
    }

    /**
     * Create and configure OkHttpClient with logging and timeouts
     */
    /**
     * Create and configure OkHttpClient with logging, timeouts, and authentication
     */
    fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(createAuthInterceptor())
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Create and configure Retrofit instance
     */
    val retrofit: Retrofit by lazy {
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
     * Create API service instance
     */
    val electionApiService: ElectionApiService by lazy {
        retrofit.create(ElectionApiService::class.java)
    }

    /**
     * Create a new OkHttpClient with specific token for election requests
     */
    fun createClientWithToken(token: String): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val tokenInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(newRequest)
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(tokenInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}

class PartyPhotoHelper {
    companion object {
        /**
         * Generate full URL untuk foto partai berdasarkan party ID
         */
        fun getPartyPhotoUrl(partyId: String): String {
            return "${ElectionNetworkClient.BASE_URL}/v1/party/$partyId/photo"
        }

        /**
         * Generate URLs untuk semua partai dari response
         */
        fun getPartyPhotoUrls(partyResponse: PartyResponse): Map<String, String> {
            val photoUrls = mutableMapOf<String, String>()

            partyResponse.data.parties.forEach { partyPair ->
                val partyId = partyPair.party.id
                val partyName = partyPair.party.name
                photoUrls[partyName] = getPartyPhotoUrl(partyId)
            }

            return photoUrls
        }
    }
}