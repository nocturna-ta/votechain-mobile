package com.nocturna.votechain.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data classes for WebSocket messages
 */
data class WebSocketMessage(
    @SerializedName("type")
    val type: String,
    @SerializedName("data")
    val data: Any? = null,
    @SerializedName("subscription")
    val subscription: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("timestamp")
    val timestamp: Long? = null
)

data class SubscriptionMessage(
    @SerializedName("type")
    val type: String = "subscribe",
    @SerializedName("subscription")
    val subscription: String = "live_results"
)

data class LiveResultsMessage(
    @SerializedName("type")
    val type: String,
    @SerializedName("data")
    val data: VotingResult,
    @SerializedName("timestamp")
    val timestamp: Long
)