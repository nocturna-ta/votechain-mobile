package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nocturna.votechain.data.model.VotingCategory
import com.nocturna.votechain.ui.screens.BottomNavigation
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.viewmodel.vote.VotingViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Color
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.screens.votepage.ResultsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VotingScreen(
    navController: NavController,
    viewModel: VotingViewModel = viewModel(),
    onHomeClick: () -> Unit = { navController.navigate("home") },
    onVotesClick: () -> Unit = { /* Already on votes */ },
    onProfileClick: () -> Unit = { navController.navigate("profile") }
) {
    val activeVotings by viewModel.activeVotings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val hasActiveVotings = !activeVotings.isEmpty()

    val currentRoute = "votes" // Since we're on the voting screen

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Active Voting", "Results")

    Scaffold(
        bottomBar = {
            BottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    when (route) {
                        "home" -> onHomeClick()
                        "votes" -> onVotesClick()
                        "profile" -> onProfileClick()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = MainColors.Primary1,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 2.dp,
                        color = MainColors.Primary1
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = {
                            Text(
                                text = title,
                                style = AppTypography.heading3Medium,
                                color = if (selectedTabIndex == index) MainColors.Primary1 else NeutralColors.Neutral50
                            )
                        },
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            if (index == 0) {
                                viewModel.fetchActiveVotings()
                            } else {
                                viewModel.fetchVotingResults()
                            }
                        }
                    )
                }
            }

            // Content based on selected tab
            if (isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(color = MainColors.Primary1)
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
            } else {
                if (selectedTabIndex == 0) {
                    // Active Voting Tab
                    if (!hasActiveVotings) {
                        EmptyVotingState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(activeVotings) { votingCategory ->
                                VotingCard(
                                    votingCategory = votingCategory,
                                    onClick = { navController.navigate("otp_verification/${votingCategory.id}") }
                                )
                            }
                        }
                    }
                } else {
                    // Results Tab
                    ResultsScreen(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun VotingCard(
    votingCategory: VotingCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = votingCategory.title,
                    style = AppTypography.heading5Bold,
                    color = PrimaryColors.Primary60
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = votingCategory.description,
                    style = AppTypography.heading6Medium,
                    color = NeutralColors.Neutral40,
                    maxLines = 1,
                    modifier = Modifier.width(270.dp),
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.right2),
                contentDescription = "View Details",
                tint = MainColors.Primary1
            )
        }
    }
}

@Composable
fun EmptyVotingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "There's no active voting right now",
            style = AppTypography.heading5SemiBold,
            textAlign = TextAlign.Center,
            color = NeutralColors.Neutral60
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please check back later for upcoming elections. Stay tuned and be ready to cast your vote when it matters!",
            style = AppTypography.heading6Regular,
            textAlign = TextAlign.Center,
            color = NeutralColors.Neutral40,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}