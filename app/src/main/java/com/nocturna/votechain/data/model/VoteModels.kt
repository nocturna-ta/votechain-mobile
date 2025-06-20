package com.nocturna.votechain.data.model

/** Request model for casting a vote
* POST /v1/vote/cast
*/
data class VoteCastRequest(
    val election_pair_id: String,
    val region: String,
    val signed_transaction: String,
    val voter_id: String
)

/**
 * Response model for vote casting
 */
data class VoteCastResponse(
    val code: Int,
    val data: VoteCastData?,
    val error: VoteCastError?,
    val message: String
)

/**
 * Vote cast data in response
 */
data class VoteCastData(
    val id: String,
    val status: String,
    val tx_hash: String,
    val voted_at: String
)

/**
 * Vote cast error details
 */
data class VoteCastError(
    val error_code: Int,
    val error_message: String
)
