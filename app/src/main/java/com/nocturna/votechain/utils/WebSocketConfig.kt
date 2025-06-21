package com.nocturna.votechain.utils

/**
 * WebSocket configuration constants
 */
object WebSocketConfig {
    const val WEBSOCKET_URL = "ws://4db6-36-69-141-188.ngrok-free.app/v1/live/ws"
    const val RECONNECT_DELAY_MS = 5000L
    const val MAX_RECONNECT_ATTEMPTS = 5
    const val CONNECTION_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 0L // No timeout for persistent connection
    const val WRITE_TIMEOUT_SECONDS = 30L

    // Subscription message
    val SUBSCRIPTION_MESSAGE = mapOf(
        "type" to "subscribe",
        "subscription" to "live_results"
    )

    // Message types
    object MessageTypes {
        const val SUBSCRIBE = "subscribe"
        const val SUBSCRIPTION_CONFIRMED = "subscription_confirmed"
        const val LIVE_RESULTS = "live_results"
        const val ERROR = "error"
        const val HEARTBEAT = "heartbeat"
    }
}