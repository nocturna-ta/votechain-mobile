package com.nocturna.votechain.utils

import android.util.Log
import com.nocturna.votechain.data.network.ElectionNetworkClient

class CandidatePhotoHelper {
    companion object {
        private const val TAG = "CandidatePhotoHelper"

        /**
         * Generate full URL untuk foto pair (kombinasi presiden dan wakil presiden)
         * Endpoint: /v1/election/pairs/{id}/photo
         */
        fun getPairPhotoUrl(pairId: String): String {
            val url = "${ElectionNetworkClient.BASE_URL}/v1/election/pairs/$pairId/photo"
            Log.d(TAG, "Generated pair photo URL: $url")
            return url
        }

        /**
         * Generate full URL untuk foto presiden berdasarkan pair ID
         * Endpoint: /v1/election/pairs/{id}/photo/president
         */
        fun getPresidentPhotoUrl(pairId: String): String {
            if (pairId.isBlank()) {
                Log.e(TAG, "⚠️ Empty pairId provided for president photo")
                return ""
            }

            val url = "${ElectionNetworkClient.BASE_URL}/v1/election/pairs/$pairId/photo/president"
            Log.d(TAG, "🖼️ Generated president photo URL: $url")
            return url
        }

        /**
         * Generate full URL untuk foto wakil presiden berdasarkan pair ID
         * Endpoint: /v1/election/pairs/{id}/photo/vice-president
         */
        fun getVicePresidentPhotoUrl(pairId: String): String {
            if (pairId.isBlank()) {
                Log.e(TAG, "⚠️ Empty pairId provided for vice president photo")
                return ""
            }

            val url = "${ElectionNetworkClient.BASE_URL}/v1/election/pairs/$pairId/photo/vice-president"
            Log.d(TAG, "🖼️ Generated vice president photo URL: $url")
            return url
        }

        /**
         * Mendapatkan URL foto berdasarkan tipe kandidat dan pair ID
         * @param candidateType Tipe kandidat (president atau vice-president)
         * @param pairId ID dari election pair
         * @return URL foto kandidat
         */
        fun getCandidatePhotoUrl(candidateType: CandidateHelper.CandidateType, pairId: String): String {
            Log.d(TAG, "🎯 Getting candidate photo - Type: $candidateType, PairId: $pairId")

            return when (candidateType) {
                CandidateHelper.CandidateType.PRESIDENT -> {
                    Log.d(TAG, "🏛️ Getting president photo for pair: $pairId")
                    getPresidentPhotoUrl(pairId)
                }
                CandidateHelper.CandidateType.VICE_PRESIDENT -> {
                    Log.d(TAG, "🤝 Getting vice president photo for pair: $pairId")
                    getVicePresidentPhotoUrl(pairId)
                }
            }
        }

        /**
         * Normalize path separator dan bersihkan URL
         * @param path Path yang perlu dinormalisasi
         * @return Path yang sudah bersih
         */
        private fun normalizePath(path: String): String {
            return path.replace("\\", "/")
                .trim()
                .removePrefix("/")
        }

        /**
         * Cek apakah string adalah URL lengkap
         * @param url String yang akan dicek
         * @return true jika URL lengkap, false jika path relatif
         */
        private fun isFullUrl(url: String): Boolean {
            return url.startsWith("http://") || url.startsWith("https://")
        }

        /**
         * Mendapatkan URL foto terbaik untuk kandidat
         * Prioritas: 1. Endpoint foto API yang spesifik, 2. photo_path dari API (dengan normalisasi)
         * @param photoPath Foto path dari response API
         * @param candidateType Tipe kandidat
         * @param pairId ID dari election pair
         * @return URL foto terbaik
         */
        fun getBestCandidatePhotoUrl(
            photoPath: String?,
            candidateType: CandidateHelper.CandidateType,
            pairId: String
        ): String {
            Log.d(TAG, "🔍 getBestCandidatePhotoUrl called:")
            Log.d(TAG, "   📄 photoPath: $photoPath")
            Log.d(TAG, "   👤 candidateType: $candidateType")
            Log.d(TAG, "   🆔 pairId: $pairId")

            // Prioritas pertama: gunakan endpoint API yang spesifik
            val apiUrl = getCandidatePhotoUrl(candidateType, pairId)
            Log.d(TAG, "   🎯 Primary choice (API endpoint): $apiUrl")

            // Fallback: jika photo_path tersedia dan valid, gunakan itu sebagai backup
            var backupUrl: String? = null
            if (!photoPath.isNullOrBlank() && !photoPath.equals("null", ignoreCase = true)) {
                val normalizedPath = normalizePath(photoPath)
                Log.d(TAG, "   🔧 Normalized path: $normalizedPath")

                backupUrl = if (isFullUrl(normalizedPath)) {
                    // URL sudah lengkap
                    normalizedPath
                } else {
                    // Path relatif, tambahkan base URL
                    val fullUrl = "${ElectionNetworkClient.BASE_URL}/$normalizedPath"
                        .replace("//", "/")
                        .replace(":/", "://") // Fix untuk https://
                    fullUrl
                }

                Log.d(TAG, "   🔄 Backup choice (photo_path): $backupUrl")
            }

            Log.d(TAG, "   ✅ Final choice: $apiUrl")
            return apiUrl
        }

        /**
         * Mendapatkan URL foto berdasarkan candidate ID (format: president_pairId atau vicepresident_pairId)
         * @param candidateId Format: "president_pairId" atau "vicepresident_pairId"
         * @return URL foto kandidat atau null jika format tidak valid
         */
        fun getCandidatePhotoUrlFromId(candidateId: String): String? {
            Log.d(TAG, "🔍 Processing candidate ID: $candidateId")

            if (candidateId.isBlank()) {
                Log.e(TAG, "❌ Empty candidate ID provided")
                return null
            }

            val parts = candidateId.split("_", limit = 2)
            if (parts.size != 2) {
                Log.e(TAG, "❌ Invalid candidate ID format: $candidateId (expected: type_pairId)")
                return null
            }

            val typePrefix = parts[0]
            val pairId = parts[1]

            Log.d(TAG, "   📝 Parsed - Type: $typePrefix, PairId: $pairId")

            return when (typePrefix) {
                CandidateHelper.CandidateType.PRESIDENT.prefix -> {
                    Log.d(TAG, "   🏛️ Generating president photo URL for ID: $candidateId")
                    getPresidentPhotoUrl(pairId)
                }
                CandidateHelper.CandidateType.VICE_PRESIDENT.prefix -> {
                    Log.d(TAG, "   🤝 Generating vice president photo URL for ID: $candidateId")
                    getVicePresidentPhotoUrl(pairId)
                }
                else -> {
                    Log.e(TAG, "   ❌ Unknown candidate type prefix: $typePrefix")
                    null
                }
            }
        }

        /**
         * Simple method untuk mendapatkan URL foto dari photo_path dengan normalisasi
         * @param photoPath Path foto dari API response
         * @return URL foto yang sudah dinormalisasi
         */
        fun getPhotoUrlFromPath(photoPath: String?): String? {
            if (photoPath.isNullOrBlank() || photoPath.equals("null", ignoreCase = true)) {
                Log.d(TAG, "🚫 Photo path is null or empty: $photoPath")
                return null
            }

            val normalizedPath = normalizePath(photoPath)

            val finalUrl = if (isFullUrl(normalizedPath)) {
                normalizedPath
            } else {
                "${ElectionNetworkClient.BASE_URL}/$normalizedPath"
                    .replace("//", "/")
                    .replace(":/", "://") // Fix untuk https://
            }

            Log.d(TAG, "🔗 Generated photo URL from path: $photoPath -> $finalUrl")
            return finalUrl
        }

        /**
         * Validate and test photo URL accessibility
         * @param url Photo URL to validate
         * @return formatted validation message for logging
         */
        fun validatePhotoUrl(url: String): String {
            val isValid = url.isNotBlank() && (url.startsWith("https://") || url.startsWith("http://"))
            val message = "📊 Photo URL validation: $url - ${if (isValid) "✅ VALID" else "❌ INVALID"}"
            Log.d(TAG, message)
            return message
        }

        /**
         * Debug method to log all candidate photo URLs for a pair
         */
        fun debugCandidatePhotos(pairId: String) {
            Log.d(TAG, "🐛 DEBUG: All photo URLs for pair $pairId:")
            Log.d(TAG, "   🏛️ President: ${getPresidentPhotoUrl(pairId)}")
            Log.d(TAG, "   🤝 Vice President: ${getVicePresidentPhotoUrl(pairId)}")
        }
    }
}