package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.screens.votepage.ResultsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VotingScreen(
    navController: NavController,
    viewModel: VotingViewModel = viewModel(),
    onHomeClick: () -> Unit = { navController.navigate("home") },
    onVotesClick: () -> Unit = { /* Already on votes */ },
    onProfileClick: () -> Unit = { navController.navigate("profile") }
) {
    val activeVotings by viewModel.activeVotings.collectAsState()
    val votingResults by viewModel.votingResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val currentRoute = "votes"

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

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
            // Tab Row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .padding(horizontal = 24.dp),
                        color = MainColors.Primary1
                    )
                },
               divider = { Divider(color = NeutralColors.Neutral10, thickness = 1.dp) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                style = AppTypography.heading4Medium,
                                color = if (pagerState.currentPage == index) MainColors.Primary1 else NeutralColors.Neutral50
                            )
                        }
                    )
                }
            }

            // Page content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ActiveVotingTab(
                        activeVotings = activeVotings,
                        isLoading = isLoading,
                        error = error,
                        onVoteItemClick = { categoryId, title ->
                            // Check if it's the 2024 presidential election - Indonesia card
                            if (title.contains("2024 presidential election", ignoreCase = true) &&
                                title.contains("Indonesia", ignoreCase = true)) {
                                // Navigate directly to candidate selection screen
                                navController.navigate("candidate_selection/$categoryId")
                            } else {
                                // Navigate to the general voting detail screen for other elections
                                navController.navigate("voting_detail/$categoryId")
                            }
                        }
                    )
                    1 -> ResultsScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }

            // Load appropriate data based on selected tab
            LaunchedEffect(pagerState.currentPage) {
                when (pagerState.currentPage) {
                    0 -> viewModel.fetchActiveVotings()
                    1 -> viewModel.fetchVotingResults()
                }
            }
        }
    }
}

@Composable
fun ActiveVotingTab(
    activeVotings: List<VotingCategory>,
    isLoading: Boolean,
    error: String?,
    onVoteItemClick: (String, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MainColors.Primary1
            )
        } else if (error != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error loading votings",
                    style = AppTypography.heading5Medium,
                    color = NeutralColors.Neutral70
                )
                Text(
                    text = error,
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral50
                )
            }
        } else if (activeVotings.isEmpty()) {
//            EmptyVotingState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp) // Add spacing between cards
            ) {
                items(activeVotings) { votingCategory ->
                    VotingCard(
                        votingCategory = votingCategory,
                        onClick = { onVoteItemClick(votingCategory.id, votingCategory.title) }
                    )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp), // Match home screen corner radius
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Match home screen background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp // Match home screen elevation
        ),
        onClick = onClick // Use Card's onClick for better touch feedback
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
                    style = AppTypography.heading5Bold, // Match home screen text style
                    color = MaterialTheme.colorScheme.onSurface // Match home screen text color
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = votingCategory.description,
                    style = AppTypography.heading6Medium, // Match home screen description style
                    color = MaterialTheme.colorScheme.onBackground, // Match home screen description color
                    maxLines = 1,
                    modifier = Modifier.width(270.dp), // Match home screen width constraint
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.right2),
                contentDescription = "View Details",
                tint = MainColors.Primary1,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}