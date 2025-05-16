//package com.nocturna.votechain.ui.screens.votepage
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Tab
//import androidx.compose.material3.TabRow
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.nocturna.votechain.viewmodel.vote.VotingViewModel
//
//@Composable
//fun VotingScreen(
//    navController: NavController,
//    viewModel: VotingViewModel = viewModel()
//) {
//    val activeVotings by viewModel.activeVotings.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
//    val error by viewModel.error.collectAsState()
//    val hasActiveVotings = viewModel.hasActiveVotings.value
//
//    var selectedTabIndex by remember { mutableStateOf(0) }
//    val tabs = listOf("Active Voting", "Results")
//
//    Column(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        TabRow(selectedTabIndex = selectedTabIndex) {
//            tabs.forEachIndexed { index, title ->
//                Tab(
//                    text = { Text(title) },
//                    selected = selectedTabIndex == index,
//                    onClick = {
//                        selectedTabIndex = index
//                        if (index == 0) {
//                            viewModel.fetchActiveVotings()
//                        } else {
//                            viewModel.fetchVotingResults()
//                        }
//                    }
//                )
//            }
//        }
//
//        if (isLoading) {
//            Box(
//                contentAlignment = Alignment.Center,
//                modifier = Modifier.fillMaxSize()
//            ) {
//                CircularProgressIndicator()
//            }
//        } else if (error != null) {
//            Box(
//                contentAlignment = Alignment.Center,
//                modifier = Modifier.fillMaxSize()
//            ) {
//                Text(
//                    text = error ?: "Unknown error occurred",
//                    color = MaterialTheme.colorScheme.error,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.padding(16.dp)
//                )
//            }
//        } else {
//            if (selectedTabIndex == 0) {
//                ActiveVotingTab(
//                    hasActiveVotings = hasActiveVotings,
//                    activeVotings = activeVotings,
//                    onVotingClick = { categoryId ->
//                        // Navigate to voting detail screen
//                        navController.navigate("voting_detail/$categoryId")
//                    }
//                )
//            } else {
//                ResultsScreen(navController = navController)
//            }
//        }
//    }
//}
//
//@Composable
//fun ActiveVotingTab(
//    hasActiveVotings: Boolean,
//    activeVotings: List<com.example.votingapp.data.model.VotingCategory>,
//    onVotingClick: (String) -> Unit
//) {
//    if (!hasActiveVotings) {
//        EmptyVotingState()
//    } else {
//        LazyColumn(
//            contentPadding = PaddingValues(16.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            modifier = Modifier.fillMaxSize()
//        ) {
//            items(activeVotings) { votingCategory ->
//                VotingCard(
//                    votingCategory = votingCategory,
//                    onClick = { onVotingClick(votingCategory.id) }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun EmptyVotingState() {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center,
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "There's no active voting right now",
//            style = MaterialTheme.typography.titleMedium,
//            textAlign = TextAlign.Center
//        )
//
//        Text(
//            text = "Please check back later for upcoming elections. Stay tuned and be ready to cast your vote when it matters!",
//            style = MaterialTheme.typography.bodyMedium,
//            textAlign = TextAlign.Center,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            modifier = Modifier.padding(top = 8.dp)
//        )
//    }
//}