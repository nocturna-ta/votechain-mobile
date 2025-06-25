package com.nocturna.votechain.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocturna.votechain.data.network.LiveResultsWebSocketManager
import com.nocturna.votechain.ui.theme.AppTypography
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Components untuk real-time data refresh dan controls
 */

@Composable
fun RefreshControlPanel(
    connectionState: LiveResultsWebSocketManager.ConnectionState,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit,
    lastUpdateTime: Long? = null,
    autoRefreshEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Connection status and last update
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ConnectionStatusDot(connectionState = connectionState)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (connectionState) {
                            LiveResultsWebSocketManager.ConnectionState.CONNECTED -> "Live Connected"
                            LiveResultsWebSocketManager.ConnectionState.CONNECTING -> "Connecting..."
                            LiveResultsWebSocketManager.ConnectionState.FAILED -> "Connection Failed"
                            LiveResultsWebSocketManager.ConnectionState.DISCONNECTED -> "Disconnected"
                        },
                        style = AppTypography.paragraphMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (lastUpdateTime != null) {
                    Text(
                        text = "Last update: ${formatUpdateTime(lastUpdateTime)}",
                        style = AppTypography.paragraphRegular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Auto refresh indicator
                if (autoRefreshEnabled) {
                    AutoRefreshIndicator()
                }

                // Manual refresh button
                RefreshButton(
                    onClick = onRefreshClick,
                    isLoading = connectionState == LiveResultsWebSocketManager.ConnectionState.CONNECTING
                )

                // Settings button
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusDot(
    connectionState: LiveResultsWebSocketManager.ConnectionState
) {
    val color = when (connectionState) {
        LiveResultsWebSocketManager.ConnectionState.CONNECTED -> Color(0xFF4CAF50)
        LiveResultsWebSocketManager.ConnectionState.CONNECTING -> Color(0xFFFF9800)
        LiveResultsWebSocketManager.ConnectionState.FAILED -> Color(0xFFF44336)
        LiveResultsWebSocketManager.ConnectionState.DISCONNECTED -> Color(0xFF9E9E9E)
    }

    val animatedAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = if (connectionState == LiveResultsWebSocketManager.ConnectionState.CONNECTED) 0.3f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = animatedAlpha))
    )
}

@Composable
fun RefreshButton(
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    val rotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = MaterialTheme.colorScheme.primary,
            modifier = if (isLoading) Modifier.rotate(rotation) else Modifier
        )
    }
}

@Composable
fun AutoRefreshIndicator() {
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50).copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "AUTO",
            style = AppTypography.paragraphBold,
            color = Color(0xFF4CAF50),
            fontSize = 10.sp
        )
    }
}

@Composable
fun DataUpdateNotification(
    isVisible: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        LaunchedEffect(isVisible) {
            delay(3000) // Auto dismiss after 3 seconds
            onDismiss()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = message,
                    style = AppTypography.paragraphMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ElectionUpdateTimer(
    startTime: Long,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000) // Update every second
        }
    }

    val elapsedTime = (currentTime - startTime) / 1000 // in seconds
    val hours = elapsedTime / 3600
    val minutes = (elapsedTime % 3600) / 60
    val seconds = elapsedTime % 60

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Election Duration",
                style = AppTypography.paragraphRegular,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                style = AppTypography.paragraphBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun RealTimeStatsBar(
    votesPerSecond: Double,
    activeConnections: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RealTimeStatItem(
                label = "Votes/sec",
                value = String.format("%.2f", votesPerSecond),
                color = Color(0xFF3B82F6)
            )

            VerticalDivider()

            RealTimeStatItem(
                label = "Live Users",
                value = "$activeConnections",
                color = Color(0xFF10B981)
            )

            VerticalDivider()

            RealTimeStatItem(
                label = "Status",
                value = "LIVE",
                color = Color(0xFFEF4444)
            )
        }
    }
}

@Composable
fun RealTimeStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = AppTypography.paragraphBold,
            color = color
        )
        Text(
            text = label,
            style = AppTypography.paragraphRegular,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

// Utility functions
private fun formatUpdateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}