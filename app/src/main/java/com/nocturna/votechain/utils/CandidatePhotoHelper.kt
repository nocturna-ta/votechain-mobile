package com.nocturna.votechain.utils

import android.util.Log
import com.nocturna.votechain.data.network.ElectionNetworkClient

class CandidatePhotoHelper {
    companion object {
        private const val TAG = "CandidatePhotoHelper"

        /**
         * Generate full URL untuk foto presiden berdasarkan pair ID
         * Endpoint: /v1/election/pairs/{id}/photo/president
         */
        fun getPresidentPhotoUrl(pairId: String): String {
            val url = "${ElectionNetworkClient.BASE_URL}/v1/election/pairs/$pairId/photo/president"
            Log.d(TAG, "Generated president photo URL: $url")
            return url
        }

        /**
         * Generate full URL untuk foto wakil presiden berdasarkan pair ID
         * Endpoint: /v1/election/pairs/{id}/photo/vice-president
         */
        fun getVicePresidentPhotoUrl(pairId: String): String {
            val url = "${ElectionNetworkClient.BASE_URL}/v1/election/pairs/$pairId/photo/vice-president"
            Log.d(TAG, "Generated vice president photo URL: $url")
            return url
        }

        /**
         * Mendapatkan URL foto berdasarkan tipe kandidat dan pair ID
         * @param candidateType Tipe kandidat (president atau vice-president)
         * @param pairId ID dari election pair
         * @return URL foto kandidat
         */
        fun getCandidatePhotoUrl(candidateType: CandidateHelper.CandidateType, pairId: String): String {
            return when (candidateType) {
                CandidateHelper.CandidateType.PRESIDENT -> {
                    Log.d(TAG, "Getting president photo for pair: $pairId")
                    getPresidentPhotoUrl(pairId)
                }
                CandidateHelper.CandidateType.VICE_PRESIDENT -> {
                    Log.d(TAG, "Getting vice president photo for pair: $pairId")
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
            Log.d(TAG, "getBestCandidatePhotoUrl called with:")
            Log.d(TAG, "- photoPath: $photoPath")
            Log.d(TAG, "- candidateType: $candidateType")
            Log.d(TAG, "- pairId: $pairId")

            // Prioritas pertama: gunakan endpoint API yang spesifik
            val apiUrl = getCandidatePhotoUrl(candidateType, pairId)
            Log.d(TAG, "- primary choice (API endpoint): $apiUrl")

            // Fallback: jika photo_path tersedia dan valid, gunakan itu sebagai backup
            if (!photoPath.isNullOrBlank() && !photoPath.equals("null", ignoreCase = true)) {
                val normalizedPath = normalizePath(photoPath)
                Log.d(TAG, "- normalizedPath: $normalizedPath")

                val backupUrl = if (isFullUrl(normalizedPath)) {
                    // URL sudah lengkap
                    normalizedPath
                } else {
                    // Path relatif, tambahkan base URL
                    "${ElectionNetworkClient.BASE_URL}/$normalizedPath".replace("//", "/")
                        .replace(":/", "://") // Fix untuk https://
                }

                Log.d(TAG, "- backup choice (photo_path): $backupUrl")
                // Untuk saat ini, kita prioritaskan API endpoint
                // Bisa diubah logikanya jika diperlukan
            }

            Log.d(TAG, "- final choice: $apiUrl")
            return apiUrl
        }

        /**
         * Mendapatkan URL foto berdasarkan candidate ID (format: president_pairId atau vicepresident_pairId)
         * @param candidateId Format: "president_pairId" atau "vicepresident_pairId"
         * @return URL foto kandidat atau null jika format tidak valid
         */
        fun getCandidatePhotoUrlFromId(candidateId: String): String? {
            val parts = candidateId.split("_", limit = 2)
            if (parts.size != 2) {
                Log.e(TAG, "Invalid candidate ID format: $candidateId")
                return null
            }

            val typePrefix = parts[0]
            val pairId = parts[1]

            return when (typePrefix) {
                CandidateHelper.CandidateType.PRESIDENT.prefix -> {
                    Log.d(TAG, "Generating president photo URL for ID: $candidateId")
                    getPresidentPhotoUrl(pairId)
                }
                CandidateHelper.CandidateType.VICE_PRESIDENT.prefix -> {
                    Log.d(TAG, "Generating vice president photo URL for ID: $candidateId")
                    getVicePresidentPhotoUrl(pairId)
                }
                else -> {
                    Log.e(TAG, "Unknown candidate type prefix: $typePrefix")
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
                Log.d(TAG, "Photo path is null or empty: $photoPath")
                return null
            }

            val normalizedPath = normalizePath(photoPath)

            val finalUrl = if (isFullUrl(normalizedPath)) {
                normalizedPath
            } else {
                "${ElectionNetworkClient.BASE_URL}/$normalizedPath".replace("//", "/")
                    .replace(":/", "://") // Fix untuk https://
            }

            Log.d(TAG, "Generated photo URL from path: $photoPath -> $finalUrl")
            return finalUrl
        }

        /**
         * Validate if the photo URL is accessible
         * This is a helper method that can be used for debugging
         * @param url Photo URL to validate
         * @return formatted log message
         */
        fun validatePhotoUrl(url: String): String {
            val message = "Photo URL validation: $url"
            Log.d(TAG, message)
            return message
        }
    }
}