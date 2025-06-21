package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.data.model.VotingOption
import com.nocturna.votechain.data.model.VotingResult
import com.nocturna.votechain.data.network.LiveResultsWebSocketManager
import com.nocturna.votechain.viewmodel.vote.LiveResultViewModel
import com.nocturna.votechain.viewmodel.vote.LiveResultViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Composable screen for displaying live voting results
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveResultsScreen(
    categoryId: String,
    regionCode: String? = null,
    viewModel: LiveResultViewModel = viewModel(factory = LiveResultViewModelFactory())
) {
    // Collect states from ViewModel
    val liveResult by viewModel.liveResult.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Start live results when screen appears
    LaunchedEffect(categoryId, regionCode) {
        viewModel.startLiveResults(categoryId, regionCode)
    }

    // Stop live results when screen disappears
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLiveResults()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with connection status
        LiveResultsHeader(
            connectionState = connectionState,
            onRetry = { viewModel.retryConnection(categoryId, regionCode) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on state
        when {
            isLoading && liveResult == null -> {
                LoadingContent()
            }
            error != null && liveResult == null -> {
                ErrorContent(
                    error = error ?: "Unknown error",
                    onRetry = { viewModel.retryConnection(categoryId, regionCode) }
                )
            }
            liveResult != null -> {
                LiveResultsContent(
                    result = liveResult!!,
                    isConnected = connectionState == LiveResultsWebSocketManager.ConnectionState.CONNECTED
                )
            }
            else -> {
                NoDataContent()
            }
        }
    }
}

@Composable
private fun LiveResultsHeader(
    connectionState: LiveResultsWebSocketManager.ConnectionState,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                LiveResultsWebSocketManager.ConnectionState.CONNECTED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                LiveResultsWebSocketManager.ConnectionState.CONNECTING -> Color(0xFFFF9800).copy(alpha = 0.1f)
                LiveResultsWebSocketManager.ConnectionState.FAILED -> Color(0xFFF44336).copy(alpha = 0.1f)
                LiveResultsWebSocketManager.ConnectionState.DISCONNECTED -> Color(0xFF9E9E9E).copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Live Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (connectionState) {
                        LiveResultsWebSocketManager.ConnectionState.CONNECTED -> "🟢 Connected - Real-time updates"
                        LiveResultsWebSocketManager.ConnectionState.CONNECTING -> "🟡 Connecting..."
                        LiveResultsWebSocketManager.ConnectionState.FAILED -> "🔴 Connection failed"
                        LiveResultsWebSocketManager.ConnectionState.DISCONNECTED -> "⚫ Disconnected"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (connectionState) {
                        LiveResultsWebSocketManager.ConnectionState.CONNECTED -> Color(0xFF4CAF50)
                        LiveResultsWebSocketManager.ConnectionState.CONNECTING -> Color(0xFFFF9800)
                        LiveResultsWebSocketManager.ConnectionState.FAILED -> Color(0xFFF44336)
                        LiveResultsWebSocketManager.ConnectionState.DISCONNECTED -> Color(0xFF9E9E9E)
                    }
                )
            }

            if (connectionState == LiveResultsWebSocketManager.ConnectionState.FAILED) {
                IconButton(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry Connection",
                        tint = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connecting to live results...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = Color(0xFFF44336),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Failed to connect to live results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry Connection")
            }
        }
    }
}

@Composable
private fun NoDataContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No live results available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Waiting for live data...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun LiveResultsContent(
    result: VotingResult,
    isConnected: Boolean
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Connection indicator
            if (!isConnected) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Showing cached data - reconnecting...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            // Result summary card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Presidential Election Results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Votes: ${result.totalVotes}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (isConnected) {
                        Text(
                            text = "🔴 Live",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            // Voting results chart (if you have this component)
            // VotingResultChart(result = result)

            // Or display results as list
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Results Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    result.options.forEach { option ->
                        ResultItem(option = option)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        item {
            // Last updated info
            if (isConnected) {
                Text(
                    text = "Last updated: ${
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(
                            Date()
                        )}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ResultItem(option: VotingOption) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
//        Text(
//            text = option.candidateName,
//            style = MaterialTheme.typography.bodyLarge,
//            modifier = Modifier.weight(1f)
//        )
//        Text(
//            text = "${option.voteCount} votes (${String.format("%.1f", option.percentage)}%)",
//            style = MaterialTheme.typography.bodyMedium,
//            fontWeight = FontWeight.Medium
//        )
    }

    LinearProgressIndicator(
        progress = option.percentage / 100f,
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}