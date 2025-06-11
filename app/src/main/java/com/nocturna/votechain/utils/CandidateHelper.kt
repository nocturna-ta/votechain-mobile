package com.nocturna.votechain.utils

import android.util.Log
import com.nocturna.votechain.data.model.Candidate
import com.nocturna.votechain.data.model.ElectionPair

/**
 * Helper object for candidate-related operations
 */
object CandidateHelper {

    private const val TAG = "CandidateHelper"

    /**
     * Create candidate ID for navigation
     * Format: {type_prefix}_{pair_id}
     * Example: "president_123" or "vicepresident_123"
     */
    fun createCandidateId(type: CandidateType, pairId: String): String {
        val candidateId = "${type.prefix}_$pairId"
        Log.d(TAG, "Created candidate ID: $candidateId (type: ${type.prefix}, pairId: $pairId)")
        return candidateId
    }

    /**
     * Parse candidate ID and return candidate data
     * @param candidateId Format: "president_pairId" atau "vicepresident_pairId"
     * @param electionPairs List of election pairs to search from
     * @return Candidate object or null if not found
     */
    fun getCandidateFromId(candidateId: String, electionPairs: List<ElectionPair>): Candidate? {
        Log.d(TAG, "getCandidateFromId called with ID: $candidateId")

        val parts = candidateId.split("_", limit = 2)
        if (parts.size != 2) {
            Log.e(TAG, "Invalid candidate ID format: $candidateId. Expected format: 'type_pairId'")
            return null
        }

        val typePrefix = parts[0]
        val pairId = parts[1]

        Log.d(TAG, "Parsed candidate ID - Type: '$typePrefix', PairId: '$pairId'")

        // Find the election pair
        val pair = electionPairs.find { it.id == pairId }
        if (pair == null) {
            Log.e(TAG, "No election pair found for ID: $pairId")
            Log.d(TAG, "Available pair IDs: ${electionPairs.map { it.id }}")
            return null
        }

        Log.d(TAG, "Found election pair: ${pair.id}")
        Log.d(TAG, "- President: ${pair.president.full_name}")
        Log.d(TAG, "- Vice President: ${pair.vice_president.full_name}")

        // Return the appropriate candidate based on type
        return when (typePrefix) {
            CandidateType.PRESIDENT.prefix -> {
                Log.d(TAG, "Returning president: ${pair.president.full_name}")
                pair.president
            }
            CandidateType.VICE_PRESIDENT.prefix -> {
                Log.d(TAG, "Returning vice president: ${pair.vice_president.full_name}")
                pair.vice_president
            }
            else -> {
                Log.e(TAG, "Unknown candidate type prefix: '$typePrefix'. Expected: '${CandidateType.PRESIDENT.prefix}' or '${CandidateType.VICE_PRESIDENT.prefix}'")
                null
            }
        }
    }

    /**
     * Get candidate type from candidate ID
     * @param candidateId Candidate ID in format "type_pairId"
     * @return CandidateType or null if invalid format
     */
    fun getCandidateTypeFromId(candidateId: String): CandidateType? {
        val parts = candidateId.split("_", limit = 2)
        if (parts.size != 2) {
            Log.e(TAG, "Invalid candidate ID format for type extraction: $candidateId")
            return null
        }

        val typePrefix = parts[0]
        return when (typePrefix) {
            CandidateType.PRESIDENT.prefix -> CandidateType.PRESIDENT
            CandidateType.VICE_PRESIDENT.prefix -> CandidateType.VICE_PRESIDENT
            else -> {
                Log.e(TAG, "Unknown candidate type prefix: $typePrefix")
                null
            }
        }
    }

    /**
     * Get pair ID from candidate ID
     * @param candidateId Candidate ID in format "type_pairId"
     * @return Pair ID or null if invalid format
     */
    fun getPairIdFromCandidateId(candidateId: String): String? {
        val parts = candidateId.split("_", limit = 2)
        if (parts.size != 2) {
            Log.e(TAG, "Invalid candidate ID format for pair ID extraction: $candidateId")
            return null
        }

        return parts[1]
    }

    /**
     * Validate candidate ID format
     * @param candidateId Candidate ID to validate
     * @return true if valid format, false otherwise
     */
    fun isValidCandidateId(candidateId: String): Boolean {
        val parts = candidateId.split("_", limit = 2)
        if (parts.size != 2) {
            Log.w(TAG, "Invalid candidate ID format: $candidateId")
            return false
        }

        val typePrefix = parts[0]
        val pairId = parts[1]

        val isValidType = typePrefix == CandidateType.PRESIDENT.prefix ||
                typePrefix == CandidateType.VICE_PRESIDENT.prefix
        val hasValidPairId = pairId.isNotBlank()

        val isValid = isValidType && hasValidPairId
        Log.d(TAG, "Candidate ID validation: $candidateId -> $isValid")

        return isValid
    }

    /**
     * Create a candidate ID from existing candidate data and election pair
     * This is useful when you have the candidate object and need to generate the ID
     * @param candidate The candidate object
     * @param electionPair The election pair containing the candidate
     * @return Candidate ID or null if candidate not found in pair
     */
    fun createCandidateIdFromData(candidate: Candidate, electionPair: ElectionPair): String? {
        return when {
            electionPair.president.full_name == candidate.full_name -> {
                createCandidateId(CandidateType.PRESIDENT, electionPair.id)
            }
            electionPair.vice_president.full_name == candidate.full_name -> {
                createCandidateId(CandidateType.VICE_PRESIDENT, electionPair.id)
            }
            else -> {
                Log.e(TAG, "Candidate ${candidate.full_name} not found in election pair ${electionPair.id}")
                null
            }
        }
    }

    /**
     * Enum for candidate types with their corresponding prefixes
     */
    enum class CandidateType(val prefix: String, val displayName: String) {
        PRESIDENT("president", "Presidential Candidate"),
        VICE_PRESIDENT("vicepresident", "Vice Presidential Candidate");

        companion object {
            /**
             * Get CandidateType from prefix string
             * @param prefix The prefix string (e.g., "president", "vicepresident")
             * @return CandidateType or null if not found
             */
            fun fromPrefix(prefix: String): CandidateType? {
                return values().find { it.prefix == prefix }
            }
        }
    }
}