package com.nocturna.votechain.utils

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.nocturna.votechain.data.network.ElectionNetworkClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

/**
 * Helper class to create and manage authenticated ImageLoader for Coil
 */
object CoilAuthHelper {
    private const val TAG = "CoilAuthHelper"
    private const val PREFS_NAME = "VoteChainPrefs"
    private const val KEY_USER_TOKEN = "user_token"

    @Volatile
    private var imageLoader: ImageLoader? = null

    /**
     * Get authenticated ImageLoader instance (singleton)
     */
    fun getImageLoader(context: Context): ImageLoader {
        return imageLoader ?: synchronized(this) {
            imageLoader ?: createImageLoader(context).also { imageLoader = it }
        }
    }

    /**
     * Get clean token without Bearer prefix
     */
    private fun getCleanToken(context: Context): String {
        var token = ""

        try {
            // Try ElectionNetworkClient first
            if (ElectionNetworkClient.isInitialized()) {
                token = ElectionNetworkClient.getUserToken()
            }

            // Fallback to SharedPreferences
            if (token.isEmpty()) {
                val sharedPreferences = context.applicationContext
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                token = sharedPreferences.getString(KEY_USER_TOKEN, "") ?: ""
            }

            // Clean token - remove Bearer prefix if present
            token = token.trim()
            if (token.startsWith("Bearer ", ignoreCase = true)) {
                token = token.substring(7).trim()
            }

            // Validate JWT format (should have 2 dots)
            if (token.isNotEmpty() && token.split(".").size != 3) {
                Log.e(TAG, "⚠️ Invalid JWT token format - expected 3 segments, got ${token.split(".").size}")
                Log.e(TAG, "Token preview: ${token.take(20)}...")
                return ""
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error getting clean token", e)
        }

        return token
    }

    /**
     * Get user token from multiple sources for fallback
     */
    private fun getUserToken(context: Context): String {
        var token = ""

        try {
            // Try to get from ElectionNetworkClient first
            if (ElectionNetworkClient.isInitialized()) {
                token = ElectionNetworkClient.getUserToken()
                Log.d(TAG, "Token from ElectionNetworkClient: ${if (token.isNotEmpty()) "Found" else "Not found"}")
            }

            // Fallback: get directly from SharedPreferences
            if (token.isEmpty()) {
                val ctx = context.applicationContext
                val sharedPreferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                token = sharedPreferences.getString(KEY_USER_TOKEN, "") ?: ""
                Log.d(TAG, "Token from SharedPreferences: ${if (token.isNotEmpty()) "Found" else "Not found"}")

                // Save it to ElectionNetworkClient for future use if we found it
                if (token.isNotEmpty() && ElectionNetworkClient.isInitialized()) {
                    ElectionNetworkClient.saveUserToken(token)
                    Log.d(TAG, "Saved token to ElectionNetworkClient")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user token", e)
        }

        return token
    }

    /**
     * Create a new ImageLoader with enhanced authentication and debugging
     */
    private fun createImageLoader(context: Context): ImageLoader {
        val token = getUserToken(context)

        // Create a comprehensive auth interceptor
        val authInterceptor = object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest = chain.request()
                val url = originalRequest.url.toString()

                Log.d(TAG, "🔄 Image request starting...")
                Log.d(TAG, "📍 URL: $url")
                Log.d(TAG, "🔑 Token available: ${if (token.isNotEmpty()) "YES" else "NO"}")

                val newRequestBuilder = originalRequest.newBuilder()

                // Add essential headers
                if (token.isNotEmpty()) {
                    newRequestBuilder.addHeader("Authorization", "Bearer $token")
                    Log.d(TAG, "🔒 Added Authorization header")
                }

                // Add ngrok-specific headers
                newRequestBuilder.addHeader("ngrok-skip-browser-warning", "true")
                newRequestBuilder.addHeader("User-Agent", "VoteChain-Android-App")
                newRequestBuilder.addHeader("Accept", "*/*")

                val newRequest = newRequestBuilder.build()

                Log.d(TAG, "📋 Request headers:")
                newRequest.headers.forEach { header ->
                    Log.d(TAG, "   ${header.first}: ${header.second}")
                }

                val response = chain.proceed(newRequest)

                Log.d(TAG, "📥 Response received:")
                Log.d(TAG, "   Status: ${response.code} ${response.message}")
                Log.d(TAG, "   Content-Type: ${response.header("Content-Type")}")
                Log.d(TAG, "   Content-Length: ${response.header("Content-Length")}")

                if (!response.isSuccessful) {
                    Log.e(TAG, "❌ Image loading failed for: $url")
                    Log.e(TAG, "   Response code: ${response.code}")
                    Log.e(TAG, "   Response message: ${response.message}")

                    // Log response body for debugging (only first 500 chars)
                    try {
                        val responseBody = response.peekBody(500)
                        val bodyString = responseBody.string()
                        Log.e(TAG, "   Response body: ${bodyString.take(200)}...")
                    } catch (e: Exception) {
                        Log.e(TAG, "   Could not read response body", e)
                    }
                } else {
                    Log.d(TAG, "✅ Image loaded successfully from: $url")
                }

                return response
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of available RAM
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Use 2% of available disk space
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(true)
            .build()
    }

    /**
     * Clear invalid token from both sources
     */
    private fun clearInvalidToken(context: Context) {
        try {
            // Clear from SharedPreferences
            val sharedPreferences = context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                remove(KEY_USER_TOKEN)
                apply()
            }

            // Clear from ElectionNetworkClient
            if (ElectionNetworkClient.isInitialized()) {
                ElectionNetworkClient.clearUserToken()
            }

            Log.w(TAG, "🧹 Cleared invalid token from all sources")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing invalid token", e)
        }
    }

    /**
     * Clear the current image loader to force re-creation
     */
    fun reset() {
        Log.d(TAG, "Resetting image loader to force recreation with latest token")
        synchronized(this) {
            imageLoader = null
        }
    }

    /**
     * Preload an image URL for better performance
     */
    fun preloadImage(context: Context, url: String) {
        Log.d(TAG, "🚀 Preloading image: $url")
        val loader = getImageLoader(context)
        val request = coil.request.ImageRequest.Builder(context)
            .data(url)
            .build()
        loader.enqueue(request)
    }
}