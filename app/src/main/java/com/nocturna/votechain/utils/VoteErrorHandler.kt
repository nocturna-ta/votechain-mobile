package com.nocturna.votechain.utils

import com.nocturna.votechain.data.model.VoteCastResponse

object VoteErrorHandler {

    fun getErrorMessage(response: VoteCastResponse): String {
        return when (response.code) {
            0 -> "Success"
            400 -> "Invalid vote data: ${response.error?.error_message ?: "Bad request"}"
            401 -> "Authentication required. Please log in again."
            403 -> "You are not authorized to vote in this election."
            409 -> "You have already voted in this election."
            422 -> "Invalid election or voter data: ${response.error?.error_message ?: "Validation failed"}"
            500 -> "Server error. Please try again later."
            else -> response.error?.error_message ?: "Unknown error occurred"
        }
    }

    fun isRetryableError(errorCode: Int): Boolean {
        return errorCode in listOf(500, 502, 503, 504) // Server errors that might be temporary
    }
}