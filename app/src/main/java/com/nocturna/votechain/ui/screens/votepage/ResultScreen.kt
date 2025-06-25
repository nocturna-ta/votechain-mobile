package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.foundation.background
import com.nocturna.votechain.data.model.VotingResult
import com.nocturna.votechain.viewmodel.vote.VotingViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.VotingCategory
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.utils.LanguageManager

@Composable
fun ResultsScreen(
    navController: NavController,
    viewModel: VotingViewModel = viewModel()
) {
    val votingResults by viewModel.votingResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val strings = LanguageManager.getLocalizedStrings()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        if (isLoading) {
            LoadingScreen()
        } else if (error != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error loading results",
                    style = AppTypography.heading5Medium,
                    color = NeutralColors.Neutral70
                )
                Text(
                    text = "error",
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral50
                )
            }
        } else {
            // Always show at least the default presidential election result card
            val resultsToShow = if (votingResults.isEmpty()) {
                // Create default presidential election result card when no data
                listOf(
                    VotingCategory(
                        id = "presidential_2024",
                        title = strings.cardTitle,
                        description = if (LanguageManager.getLanguage(LocalContext.current) == LanguageManager.LANGUAGE_INDONESIAN) {
                            "Lihat hasil pemilihan dan distribusi suara"
                        } else {
                            "View the election results and vote distribution"
                        },
                        isActive = false // Results are for completed elections
                    )
                )
            } else {
                votingResults
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp) // Match active voting spacing
            ) {
                items(resultsToShow) { result ->
                    // Convert VotingCategory to VotingResult for ResultCard
                    ResultCard(
                        result = VotingResult(
                            categoryId = result.id,
                            categoryTitle = result.title,
                            options = emptyList(),
                            totalVotes = 0
                        ),
                        onClick = {
                            // Navigate to detailed results
                            if (result.id == "presidential_2024") {
                                navController.navigate("live_result/${result.id}")
                            } else {
                                navController.navigate("detail_result/${result.id}/${result.title}")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    result: VotingResult,
    onClick: () -> Unit = {}
) {
    // Match the exact same card styling as active voting
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp), // Match active voting corner radius
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Match active voting background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp // Match active voting elevation
        ),
        onClick = onClick // Use Card's onClick for consistency
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp), // Match active voting padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.categoryTitle,
                    style = AppTypography.heading5Bold, // Match active voting text style
                    color = MaterialTheme.colorScheme.onSurface // Match active voting text color
                )

                Spacer(modifier = Modifier.height(6.dp)) // Match active voting spacing

                // Create description showing winner or status
//                val description = if (result.options.isNotEmpty()) {
//                    val winner = result.options.maxByOrNull { it.votes }
//                    "Winner: ${winner?.name} with ${winner?.votes} votes"
//                } else {
//                    "Total votes: ${result.totalVotes}"
//                }

                Text(
                    text = "Presents the official vote counts, percentages, and detailed outcomes",
                    style = AppTypography.heading6Medium, // Match active voting description style
                    color = MaterialTheme.colorScheme.onBackground, // Match active voting description color
                    maxLines = 1,
                    modifier = Modifier.width(270.dp), // Match active voting width constraint
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.right2),
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurface // Match active voting icon color
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
            style = AppTypography.heading5Medium,
            textAlign = TextAlign.Center,
            color = NeutralColors.Neutral70
        )

        Text(
            text = "Results will appear here after voting periods end.",
            style = AppTypography.paragraphRegular,
            textAlign = TextAlign.Center,
            color = NeutralColors.Neutral50,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}