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
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

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
                Log.d(TAG, "Token from ElectionNetworkClient: ${if (token.isNotEmpty()) "Found (${token.length} chars)" else "Not found"}")
            }

            // Fallback to SharedPreferences
            if (token.isEmpty()) {
                val sharedPreferences = context.applicationContext
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                token = sharedPreferences.getString(KEY_USER_TOKEN, "") ?: ""
                Log.d(TAG, "Token from SharedPreferences: ${if (token.isNotEmpty()) "Found (${token.length} chars)" else "Not found"}")

                // Save it to ElectionNetworkClient for future use if we found it
                if (token.isNotEmpty() && ElectionNetworkClient.isInitialized()) {
                    ElectionNetworkClient.saveUserToken(token)
                    Log.d(TAG, "Saved token to ElectionNetworkClient")
                }
            }

            // Clean token - remove Bearer prefix if present
            token = token.trim()
            if (token.startsWith("Bearer ", ignoreCase = true)) {
                token = token.substring(7).trim()
                Log.d(TAG, "Removed 'Bearer ' prefix from token")
            }

            // Validate JWT format (should have 2 dots)
            if (token.isNotEmpty()) {
                val segments = token.split(".")
                if (segments.size != 3) {
                    Log.e(TAG, "⚠️ Invalid JWT token format - expected 3 segments, got ${segments.size}")
                    Log.e(TAG, "Token preview: ${token.take(20)}...${token.takeLast(20)}")
                    // Clear invalid token
                    clearInvalidToken(context)
                    token = ""
                } else {
                    Log.d(TAG, "✅ Valid JWT token format detected")
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
        val token = getCleanToken(context)

        // Create logging interceptor for debugging
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            if (message.contains("Image") || message.contains("photo") || message.contains("401")) {
                Log.d(TAG, "HTTP: $message")
            }
        }.apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        // Create auth interceptor that adds token to every request
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val url = originalRequest.url.toString()

            // Only log image-related requests
            if (url.contains("/photo") || url.contains("/image")) {
                Log.d(TAG, "🔄 Image request starting...")
                Log.d(TAG, "📍 URL: $url")
                Log.d(TAG, "🔑 Token available: ${if (token.isNotEmpty()) "YES (${token.length} chars)" else "NO"}")
            }

            // Build new request with headers
            val requestBuilder = originalRequest.newBuilder()
                .removeHeader("Authorization") // Remove any existing Authorization header first

            // Add auth header if token is available
            if (token.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
                if (url.contains("/photo")) {
                    Log.d(TAG, "🔒 Added Authorization header with token")
                }
            }

            // Add other required headers
            requestBuilder
                .addHeader("ngrok-skip-browser-warning", "true")
                .addHeader("User-Agent", "VoteChain-Android-App")
                .addHeader("Accept", "*/*")
                .addHeader("Content-Type", "application/json")

            val newRequest = requestBuilder.build()

            // Log headers for image requests
            if (url.contains("/photo")) {
                Log.d(TAG, "📋 Request headers:")
                newRequest.headers.forEach { header ->
                    if (header.first == "Authorization") {
                        Log.d(TAG, "   ${header.first}: Bearer [TOKEN_HIDDEN]")
                    } else {
                        Log.d(TAG, "   ${header.first}: ${header.second}")
                    }
                }
            }

            try {
                val response = chain.proceed(newRequest)

                // Log response for image requests
                if (url.contains("/photo")) {
                    Log.d(TAG, "📥 Response received:")
                    Log.d(TAG, "   Status: ${response.code} ${response.message}")
                    Log.d(TAG, "   Content-Type: ${response.header("Content-Type")}")
                    Log.d(TAG, "   Content-Length: ${response.header("Content-Length")}")

                    when (response.code) {
                        200 -> Log.d(TAG, "✅ Image loaded successfully")
                        401 -> {
                            Log.e(TAG, "❌ Authentication failed (401)")
                            Log.e(TAG, "Token might be expired or invalid")
                            // Try to peek at response body for more details
                            try {
                                val responseBody = response.peekBody(500)
                                val bodyString = responseBody.string()
                                Log.e(TAG, "Response body: ${bodyString.take(200)}")
                            } catch (e: Exception) {
                                Log.e(TAG, "Could not read response body", e)
                            }
                        }
                        403 -> Log.e(TAG, "❌ Access forbidden (403)")
                        404 -> Log.e(TAG, "❌ Image not found (404)")
                        else -> Log.e(TAG, "❌ Unexpected response code: ${response.code}")
                    }
                }

                response
            } catch (e: Exception) {
                Log.e(TAG, "❌ Request failed with exception", e)
                throw e
            }
        }

        // Create OkHttpClient with interceptors
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        // Create and return ImageLoader
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
            .respectCacheHeaders(false) // Ignore cache headers from server
            .crossfade(true)
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
        Log.d(TAG, "🔄 Resetting image loader to force recreation with latest token")
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

    /**
     * Check if we have a valid token
     */
    fun hasValidToken(context: Context): Boolean {
        val token = getCleanToken(context)
        return token.isNotEmpty()
    }
}