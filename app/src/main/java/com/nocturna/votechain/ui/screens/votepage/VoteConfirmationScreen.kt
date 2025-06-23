package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.viewmodel.vote.VoteConfirmationViewModel
import kotlinx.coroutines.delay

@Composable
fun VoteConfirmationScreen(
    categoryId: String,
    electionPairId: String,
    navController: NavController,
    onBackClick: () -> Unit = { navController.popBackStack() },
    onConfirmVote: () -> Unit = {
        navController.navigate("vote_success") {
            popUpTo("votes") { inclusive = false }
        }
    }
) {
    val context = LocalContext.current
    val viewModel: VoteConfirmationViewModel = viewModel(
        factory = VoteConfirmationViewModel.Factory(context, categoryId, electionPairId)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Handle successful vote
    LaunchedEffect(uiState.isVoteSuccess) {
        if (uiState.isVoteSuccess) {
            delay(2000) // Show success message
            onConfirmVote()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Custom top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    tint = MainColors.Primary1,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onBackClick)
                )
            }

            // Centered title
            Text(
                text = "Confirm Your Vote",
                style = AppTypography.heading4Regular,
                color = PrimaryColors.Primary80,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.weight(0.2f))

        // Main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Confirmation icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MainColors.Primary1.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Title
                Text(
                    text = "Confirm Your Vote",
                    style = AppTypography.heading2Bold,
                    color = PrimaryColors.Primary90,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "You are about to cast your vote for the selected candidate. This action cannot be undone.",
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral60,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Candidate info card (simplified)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NeutralColors.Neutral10
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selected Candidate",
                            style = AppTypography.paragraphMedium,
                            color = NeutralColors.Neutral60
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Candidate Pair #$electionPairId",
                            style = AppTypography.heading5Bold,
                            color = PrimaryColors.Primary90
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Presidential Election 2024",
                            style = AppTypography.smallParagraphRegular,
                            color = NeutralColors.Neutral50
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // OTP verification status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OTP Verified",
                        style = AppTypography.smallParagraphMedium,
                        color = Color.Green
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error message
                if (!uiState.error.isNullOrEmpty()) {
                    Text(
                        text = "$uiState.error",
                        style = AppTypography.paragraphRegular,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Success message
                if (uiState.isVoteSuccess) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Vote cast successfully!",
                                style = AppTypography.paragraphMedium,
                                color = Color.Green
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Redirecting to home...",
                            style = AppTypography.smallParagraphRegular,
                            color = NeutralColors.Neutral60
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Action buttons
                if (!uiState.isVoteSuccess) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button
                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MainColors.Primary1
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MainColors.Primary1)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isLoading
                        ) {
                            Text(
                                text = "Cancel",
                                style = AppTypography.paragraphMedium
                            )
                        }

                        // Confirm button
                        Button(
                            onClick = {
                                viewModel.castVote()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColors.Primary1,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = "Cast Vote",
                                    style = AppTypography.paragraphMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}