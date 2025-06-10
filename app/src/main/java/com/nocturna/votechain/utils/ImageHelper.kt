package com.nocturna.votechain.utils

object ImageHelper {
    private const val BASE_URL = "https://67d2-36-69-143-235.ngrok-free.app"

    /**
     * Convert relative path to full image URL
     */
    fun getFullImageUrl(relativePath: String?): String? {
        if (relativePath.isNullOrBlank()) return null

        // Handle different path formats
        return when {
            relativePath.startsWith("http") -> relativePath
            relativePath.startsWith("uploads") -> "$BASE_URL/$relativePath"
            else -> "$BASE_URL/uploads/$relativePath"
        }
    }
}