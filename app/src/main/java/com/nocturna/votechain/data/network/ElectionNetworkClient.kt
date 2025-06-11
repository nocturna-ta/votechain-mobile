package com.nocturna.votechain.data.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.nocturna.votechain.data.model.PartyResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network client for connecting to the Election API
 */
object ElectionNetworkClient {
    const val BASE_URL = "https://1069-36-69-142-76.ngrok-free.app"
    private const val TAG = "ElectionNetworkClient"

    /**
     * Create and configure OkHttpClient with logging and timeouts
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
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
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