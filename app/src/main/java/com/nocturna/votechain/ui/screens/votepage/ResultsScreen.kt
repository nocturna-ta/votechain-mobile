//package com.nocturna.votechain.ui.screens.votepage
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.nocturna.votechain.data.model.VotingResult
//import com.nocturna.votechain.ui.theme.AppTypography
//import com.nocturna.votechain.ui.theme.MainColors
//import com.nocturna.votechain.ui.theme.NeutralColors
//import com.nocturna.votechain.ui.theme.PrimaryColors
//import com.nocturna.votechain.viewmodel.vote.VotingViewModel
//
//@Composable
//fun ResultsScreen(
//    navController: NavController,
//    viewModel: VotingViewModel
//) {
//    val votingResults by viewModel.votingResults.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
//    val error by viewModel.error.collectAsState()
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp)
//    ) {
//        if (isLoading) {
//            CircularProgressIndicator(
//                modifier = Modifier.align(Alignment.Center),
//                color = MainColors.Primary1
//            )
//        } else if (error != errornull) {
//            Column(
//                modifier = Modifier.align(Alignment.Center),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Error loading results",
//                    style = AppTypography.heading5Medium,
//                    color = NeutralColors.Neutral70
//                )
//                Text(
//                    text = error ?: "Unknown error occurred",
//                    style = AppTypography.paragraphRegular,
//                    color = NeutralColors.Neutral50
//                )
//            }
//        } else if (votingResults.isEmpty()) {
//            EmptyResultsState()
//        } else {
//            LazyColumn(
//                contentPadding = PaddingValues(vertical = 8.dp),
//                modifier = Modifier.fillMaxSize()
//            ) {
//                items(votingResults) { result ->
//                    ResultCard(
//                        result = result,
//                        onDetailClick = {
//                            // Navigate to detailed results when clicked
//                            navController.navigate("detailed_result/${result.categoryId}/${result.categoryTitle}")
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ResultCard(
//    result: VotingResult,
//    onDetailClick: () -> Unit = {}
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//            .clickable(onClick = onDetailClick),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 0.dp
//        )
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = result.categoryTitle,
//                style = AppTypography.heading5SemiBold.copy(fontWeight = FontWeight.SemiBold),
//                color = PrimaryColors.Primary60
//            )
//
//            Text(
//                text = "Total Votes: ${result.totalVotes}",
//                style = AppTypography.paragraphRegular,
//                color = NeutralColors.Neutral50,
//                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
//            )
//
//            // Show the voting options with progress bars
//            result.options.forEach { option ->
//                VotingResultItem(
//                    name = option.name,
//                    votes = option.votes,
//                    percentage = option.percentage
//                )
//                Spacer(modifier = Modifier.height(12.dp))
//            }
//        }
//    }
//}
//
//@Composable
//fun VotingResultItem(
//    name: String,
//    votes: Int,
//    percentage: Float
//) {
//    Column(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(
//                text = name,
//                style = AppTypography.paragraphRegular,
//                color = NeutralColors.Neutral80
//            )
//
//            Text(
//                text = "${String.format("%.1f", percentage * 100)}%",
//                style = AppTypography.paragraphMedium,
//                color = MainColors.Primary1
//            )
//        }
//
//        Spacer(modifier = Modifier.height(4.dp))
//
//        // Progress bar
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(8.dp)
//                .clip(RoundedCornerShape(4.dp))
//                .background(NeutralColors.Neutral20)
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth(percentage)
//                    .height(8.dp)
//                    .clip(RoundedCornerShape(4.dp))
//                    .background(MainColors.Primary1)
//            )
//        }
//
//        Text(
//            text = "$votes votes",
//            style = AppTypography.smallParagraphRegular,
//            color = NeutralColors.Neutral50,
//            modifier = Modifier.padding(top = 4.dp)
//        )
//    }
//}
//
//@Composable
//fun EmptyResultsState() {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center,
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "No voting results available",
//            style = AppTypography.heading5SemiBold,
//            textAlign = TextAlign.Center,
//            color = NeutralColors.Neutral60
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = "Results will appear here after voting periods end.",
//            style = AppTypography.heading6Regular,
//            textAlign = TextAlign.Center,
//            color = NeutralColors.Neutral40,
//            modifier = Modifier.padding(horizontal = 24.dp)
//        )
//    }
//}