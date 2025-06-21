package com.nocturna.votechain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.data.network.LiveResultsWebSocketManager

/**
 * Reusable component to show WebSocket connection status
 */
@Composable
fun ConnectionStatusIndicator(
    connectionState: LiveResultsWebSocketManager.ConnectionState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    when (connectionState) {
                        LiveResultsWebSocketManager.ConnectionState.CONNECTED -> Color(0xFF4CAF50)
                        LiveResultsWebSocketManager.ConnectionState.CONNECTING -> Color(0xFFFF9800)
                        LiveResultsWebSocketManager.ConnectionState.FAILED -> Color(0xFFF44336)
                        LiveResultsWebSocketManager.ConnectionState.DISCONNECTED -> Color(0xFF9E9E9E)
                    }
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = when (connectionState) {
                LiveResultsWebSocketManager.ConnectionState.CONNECTED -> "Live"
                LiveResultsWebSocketManager.ConnectionState.CONNECTING -> "Connecting"
                LiveResultsWebSocketManager.ConnectionState.FAILED -> "Failed"
                LiveResultsWebSocketManager.ConnectionState.DISCONNECTED -> "Offline"
            },
            style = MaterialTheme.typography.bodySmall,
            color = when (connectionState) {
                LiveResultsWebSocketManager.ConnectionState.CONNECTED -> Color(0xFF4CAF50)
                LiveResultsWebSocketManager.ConnectionState.CONNECTING -> Color(0xFFFF9800)
                LiveResultsWebSocketManager.ConnectionState.FAILED -> Color(0xFFF44336)
                LiveResultsWebSocketManager.ConnectionState.DISCONNECTED -> Color(0xFF9E9E9E)
            }
        )
    }
}