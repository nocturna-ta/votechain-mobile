package com.nocturna.votechain.utils

object ImageHelper {
    // Updated to use the new ngrok endpoint
    private const val BASE_URL = "https://3d34-103-233-100-204.ngrok-free.app"

    /**
     * Convert relative path to full image URL
     */
    fun getFullImageUrl(relativePath: String?): String? {
        if (relativePath.isNullOrBlank()) return null

        // Handle different path formats
        return when {
            // Already a full URL
            relativePath.startsWith("http") -> relativePath

            // Path starts with uploads
            relativePath.startsWith("uploads") -> "$BASE_URL/$relativePath"

            // Path starts with /uploads
            relativePath.startsWith("/uploads") -> "$BASE_URL$relativePath"

            // Path starts with /v1/ (API endpoint)
            relativePath.startsWith("/v1/") -> "$BASE_URL$relativePath"

            // Path starts with v1/ (API endpoint without leading slash)
            relativePath.startsWith("v1/") -> "$BASE_URL/$relativePath"

            // Default case - assume it's in uploads folder
            else -> "$BASE_URL/uploads/$relativePath"
        }
    }

    /**
     * Get full URL for KTP photo specifically
     */
    fun getKtpPhotoUrl(ktpPhotoPath: String?): String? {
        return getFullImageUrl(ktpPhotoPath)
    }

    /**
     * Get full URL for profile photo
     */
    fun getProfilePhotoUrl(profilePhotoPath: String?): String? {
        return getFullImageUrl(profilePhotoPath)
    }

    /**
     * Get full URL for candidate photo using API endpoint
     */
    fun getCandidatePhotoUrl(candidateId: String): String {
        return "$BASE_URL/v1/candidate/$candidateId/photo"
    }

    /**
     * Get full URL for party photo using API endpoint
     */
    fun getPartyPhotoUrl(partyId: String): String {
        return "$BASE_URL/v1/party/$partyId/photo"
    }

    /**
     * Validate if a URL is accessible (placeholder for future implementation)
     */
    fun isUrlAccessible(url: String): Boolean {
        // TODO: Implement URL accessibility check if needed
        return url.isNotBlank()
    }

    /**
     * Clean and normalize image path
     */
    fun normalizePath(path: String?): String? {
        if (path.isNullOrBlank()) return null

        return path.trim()
            .replace("\\", "/") // Replace backslashes with forward slashes
            .replace("//", "/") // Remove double slashes
            .let { normalized ->
                // Ensure no leading slash unless it's an API endpoint
                if (normalized.startsWith("/") && !normalized.startsWith("/v1/")) {
                    normalized.substring(1)
                } else {
                    normalized
                }
            }
    }

    /**
     * Get image URL with fallback options
     */
    fun getImageUrlWithFallback(
        primaryPath: String?,
        fallbackPath: String? = null,
        defaultUrl: String? = null
    ): String? {
        // Try primary path first
        getFullImageUrl(primaryPath)?.let { return it }

        // Try fallback path
        getFullImageUrl(fallbackPath)?.let { return it }

        // Return default URL
        return defaultUrl
    }

    /**
     * Check if the given string is already a full URL
     */
    fun isFullUrl(url: String?): Boolean {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"))
    }
}