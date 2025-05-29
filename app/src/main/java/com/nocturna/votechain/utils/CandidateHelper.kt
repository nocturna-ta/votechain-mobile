package com.nocturna.votechain.utils

import com.nocturna.votechain.data.model.Candidate
import com.nocturna.votechain.data.model.ElectionPair

/**
 * Helper object for candidate-related operations
 */
object CandidateHelper {

    /**
     * Create candidate ID for navigation
     */
    fun createCandidateId(type: CandidateType, pairId: String): String {
        return "${type.prefix}_$pairId"
    }

    /**
     * Parse candidate ID and return candidate data
     */
    fun getCandidateFromId(candidateId: String, electionPairs: List<ElectionPair>): Candidate? {
        val parts = candidateId.split("_", limit = 2)
        if (parts.size != 2) {
            android.util.Log.e("CandidateHelper", "Invalid candidate ID format: $candidateId")
            return null
        }

        val typePrefix = parts[0]
        val pairId = parts[1]

        val pair = electionPairs.find { it.id == pairId }
        if (pair == null) {
            android.util.Log.e("CandidateHelper", "No election pair found for ID: $pairId")
            return null
        }

        return when (typePrefix) {
            CandidateType.PRESIDENT.prefix -> {
                android.util.Log.d("CandidateHelper", "Found president: ${pair.president.full_name}")
                pair.president
            }
            CandidateType.VICE_PRESIDENT.prefix -> {
                android.util.Log.d("CandidateHelper", "Found vice president: ${pair.vice_president.full_name}")
                pair.vice_president
            }
            else -> {
                android.util.Log.e("CandidateHelper", "Unknown candidate type prefix: $typePrefix")
                null
            }
        }
    }

    enum class CandidateType(val prefix: String) {
        PRESIDENT("president"),
        VICE_PRESIDENT("vicepresident")
    }
}