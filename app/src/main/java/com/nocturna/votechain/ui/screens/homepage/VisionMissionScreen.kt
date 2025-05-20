package com.nocturna.votechain.ui.screens.homepage

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nocturna.votechain.R
import com.nocturna.votechain.viewmodel.candidate.VisionMissionViewModelImpl
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.ui.theme.VotechainTheme
import com.nocturna.votechain.utils.LanguageManager

@Composable
fun VisionMissionScreen(
    navController: NavController,
    candidateNumber: Int,
    onBackClick: () -> Unit = { navController.popBackStack() },
    modifier: Modifier = Modifier
) {
    val strings = LanguageManager.getLocalizedStrings()

    // Create and remember the presenter
    val scrollState = rememberScrollState()
    val presenter = remember { VisionMissionViewModelImpl() }
    val uiState by presenter.uiState.collectAsState()

    // Load data when the screen is first composed or when candidateNumber changes
    LaunchedEffect(candidateNumber) {
        presenter.loadData(candidateNumber)
    }

    // Main screen content
    Column(modifier = Modifier.fillMaxSize()) {
        // Custom top bar with shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
                    .clickable(onClick = onBackClick)
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = strings.back,
                    tint = MainColors.Primary1,
                    modifier = Modifier.size(20.dp) // Smaller icon size
                )
            }

            // Centered title
            Text(
                text = strings.visionMission,
                style = AppTypography.heading4Regular,
                color = PrimaryColors.Primary80,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Main content based on UI state
        if (uiState.isLoading) {
            // Handle loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingScreen()
            }
        } else if (uiState.error != null) {
            // Handle error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error Loading Data",
                        style = AppTypography.heading4Medium,
                        color = MainColors.Primary1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.error ?: "Unknown error occurred",
                        style = AppTypography.heading5Regular,
                        color = NeutralColors.Neutral70,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { presenter.loadData(candidateNumber) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainColors.Primary1
                        )
                    ) {
                        Text("Try Again")
                    }
                }
            }
        } else {
            // Content when data is successfully loaded
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Vision Section
                Text(
                    text = strings.vision,
                    style = AppTypography.heading5Bold,
                    color = PrimaryColors.Primary70,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "\"${uiState.vision}\"",
                    style = AppTypography.paragraphMedium,
                    color = NeutralColors.Neutral80,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Mission Section
                Text(
                    text = strings.mission,
                    style = AppTypography.heading5Bold,
                    color = PrimaryColors.Primary70,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mission list
                uiState.missions.forEachIndexed { index, mission ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = AppTypography.paragraphRegular,
                            color = NeutralColors.Neutral80,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = mission,
                            style = AppTypography.paragraphRegular,
                            color = NeutralColors.Neutral80,
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // More Information Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        val candidateId = when (candidateNumber) {
                            1 -> "anies"
                            2 -> "prabowo"
                            3 -> "ganjar"
                            else -> "anies"
                        }
                        navController.navigate("candidate_detail/$candidateId")
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.background),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = strings.moreInformation,
                                style = AppTypography.heading6SemiBold,
                                color = NeutralColors.Neutral10
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Click here to access detailed information about the vision, mission, and work programs of candidate number ${uiState.candidateNumber}",
                                style = AppTypography.paragraphRegular,
                                color = NeutralColors.Neutral10
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}