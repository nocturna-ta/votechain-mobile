package com.nocturna.votechain.ui.screens.homepage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.WorkProgram
import com.nocturna.votechain.viewmodel.candidate.VisionMissionViewModelImpl
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.theme.*
import com.nocturna.votechain.utils.LanguageManager

@Composable
fun VisionMissionScreen(
    navController: NavController,
    pairId: String, // Changed from candidateNumber to pairId
    onBackClick: () -> Unit = { navController.popBackStack() },
    modifier: Modifier = Modifier
) {
    val strings = LanguageManager.getLocalizedStrings()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showDocumentError by remember { mutableStateOf(false) }

    // Create and remember the presenter
    val presenter = remember { VisionMissionViewModelImpl() }
    val uiState by presenter.uiState.collectAsState()

    // Load data when the screen is first composed or when pairId changes
    LaunchedEffect(pairId) {
        if (pairId.isNotBlank()) {
            presenter.loadDataFromAPI(pairId) // Load using API with pairId
        } else {
            // Fallback to old method if somehow pairId is empty
            presenter.loadData(1)
        }
    }

    // Main screen content
    Column(modifier = Modifier.fillMaxSize()) {
        // Custom top bar with shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
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
                    modifier = Modifier.size(20.dp)
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
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingScreen()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
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
                            onClick = { presenter.loadDataFromAPI(pairId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColors.Primary1
                            )
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Vision Section
                    item {
                        VisionSection(vision = uiState.vision)
                    }

                    // Mission Section
                    item {
                        MissionSection(mission = uiState.mission)
                    }

                    // Work Programs Section
                    if (uiState.workPrograms.isNotEmpty()) {
                        item {
                            WorkProgramsSection(programs = uiState.workPrograms)
                        }
                    }

                    // More Information Card (only if programDocs exists)
                    item {
                        // Build the API URL directly using the pairId
                        val programDocsUrl = "https://8f7e-36-69-142-17.ngrok-free.app/v1/election/pairs/$pairId/detail/program-docs"

                        MoreInformationCard(
                            onCardClick = {
                                try {
                                    uriHandler.openUri(programDocsUrl)
                                } catch (e: Exception) {
                                    showDocumentError = true
                                }
                            }
                        )
                    }
                    // Add some bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    // Show error dialog for document opening issues
    if (showDocumentError) {
        AlertDialog(
            onDismissRequest = { showDocumentError = false },
            title = { Text("Cannot Open Document") },
            text = { Text("Unable to open the program document. Please check your internet connection and try again.") },
            confirmButton = {
                TextButton(onClick = { showDocumentError = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun VisionSection(vision: String) {
    val strings = LanguageManager.getLocalizedStrings()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            // Vision Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MainColors.Primary1.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "V",
                    style = AppTypography.heading5Bold,
                    color = MainColors.Primary1
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.vision,
                    style = AppTypography.heading6SemiBold,
                    color = NeutralColors.Neutral80
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = vision.ifBlank { "Vision not available" },
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral80,
                    textAlign = TextAlign.Justify
                )
            }
        }
    }
}

@Composable
private fun MissionSection(mission: String) {
    val strings = LanguageManager.getLocalizedStrings()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            // Mission Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        PrimaryColors.Primary50.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    style = AppTypography.heading5Bold,
                    color = PrimaryColors.Primary50
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.mission,
                    style = AppTypography.heading6SemiBold,
                    color = NeutralColors.Neutral80
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = mission.ifBlank { "Mission not available" },
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral80,
                    textAlign = TextAlign.Justify
                )
            }
        }
    }
}

@Composable
private fun WorkProgramsSection(programs: List<WorkProgram>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                // Work Programs Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            PrimaryColors.Primary50.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        style = AppTypography.heading5Bold,
                        color = PrimaryColors.Primary50
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Work Programs:",
                    style = AppTypography.heading6SemiBold,
                    color = NeutralColors.Neutral80
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            programs.forEach { program ->
                WorkProgramItem(program = program)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun WorkProgramItem(program: WorkProgram) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                NeutralColors.Neutral10,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = program.programName,
            style = AppTypography.paragraphSemiBold,
            color = NeutralColors.Neutral80
        )

        if (program.programDesc.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            program.programDesc.forEach { desc ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "• ",
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral60
                    )
                    Text(
                        text = desc,
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral60,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        program.programPhoto?.let { photoUrl ->
            if (photoUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = program.programName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun MoreInformationCard(onCardClick: () -> Unit) {
    val strings = LanguageManager.getLocalizedStrings()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Content
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
                    text = "Click here to access detailed information about the vision, mission, and complete work programs of this candidate pair.",
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral10
                )
            }
        }
    }
}
