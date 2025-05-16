package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.foundation.background
import com.nocturna.votechain.data.model.VotingResult
import com.nocturna.votechain.viewmodel.vote.VotingViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun ResultsScreen(
    navController: NavController,
    viewModel: VotingViewModel = viewModel()
) {
    val votingResults by viewModel.votingResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = error ?: "Unknown error occurred",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    } else if (votingResults.isEmpty()) {
        EmptyResultsState()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(votingResults) { result ->
                ResultCard(result = result)
            }
        }
    }
}

@Composable
fun ResultCard(result: VotingResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = result.categoryTitle,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Total Votes: ${result.totalVotes}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            result.options.forEach { option ->
                VotingResultItem(
                    name = option.name,
                    votes = option.votes,
                    percentage = option.percentage
                )
            }
        }
    }
}

@Composable
fun VotingResultItem(
    name: String,
    votes: Int,
    percentage: Float
) {
    Column(
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium
        )

        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .weight(percentage)
                    .padding(end = 8.dp)
                    .background(color = MaterialTheme.colorScheme.primary)
                    .padding(vertical = 4.dp)
            )

            Text(
                text = "$votes votes (${String.format("%.1f", percentage * 100)}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyResultsState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "No voting results available",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Results will appear here after voting periods end.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}