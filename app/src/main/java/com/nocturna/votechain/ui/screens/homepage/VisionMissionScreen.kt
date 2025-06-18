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
import androidx.compose.ui.unit.sp
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
    pairId: String,
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
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Vision Section
                    item {
                        VisionSection(vision = "\"${uiState.vision}\"")
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
                        val programDocsUrl = "https://f664-103-233-100-204.ngrok-free.app/v1/election/pairs/$pairId/detail/program-docs"

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

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Vision Title
        Text(
            text = strings.vision,
            style = AppTypography.heading5Bold,
            color = PrimaryColors.Primary70,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Vision Content
        Text(
            text = vision.ifBlank { "Vision not available" },
            style = AppTypography.paragraphMedium,
            color = NeutralColors.Neutral80,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MissionSection(mission: String) {
    val strings = LanguageManager.getLocalizedStrings()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Mission Title
        Text(
            text = strings.mission,
            style = AppTypography.heading5Bold,
            color = PrimaryColors.Primary70,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mission Content - parse numbered list if it contains mission points
        val missionText = mission.ifBlank { "Mission not available" }

        // Split mission text by numbered points (1., 2., 3., etc.)
        val missionPoints = missionText.split(Regex("\\d+\\.\\s*")).filter { it.isNotBlank() }

        if (missionPoints.size > 1) {
            // Display as numbered list
            missionPoints.forEachIndexed { index, point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}. ",
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral80
                    )
                    Text(
                        text = point.trim(),
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral80,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // Display as single text block
            Text(
                text = missionText,
                style = AppTypography.paragraphRegular,
                color = NeutralColors.Neutral80,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WorkProgramsSection(programs: List<WorkProgram>) {
    val strings = LanguageManager.getLocalizedStrings()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Work Programs Title
        Text(
            text = strings.workProgram,
            style = AppTypography.heading5Bold,
            color = PrimaryColors.Primary70,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        programs.forEachIndexed { index, program ->
            WorkProgramItem(program = program, index = index + 1)
            if (index < programs.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun WorkProgramItem(program: WorkProgram, index: Int) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Program Name with numbering (like mission items)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$index. ",
                style = AppTypography.paragraphRegular,
                color = NeutralColors.Neutral80
            )
            Text(
                text = program.programName,
                style = AppTypography.paragraphSemiBold,
                color = NeutralColors.Neutral80,
                modifier = Modifier.weight(1f)
            )
        }

        // Program Description (if available)
        if (program.programDesc.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            program.programDesc.forEach { desc ->
                Row(
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 2.dp, start = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral70
                    )
                    Text(
                        text = desc,
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral70,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Program Photo (if available)
        program.programPhoto?.let { photoUrl ->
            if (photoUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = program.programName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(start = 16.dp)
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
        Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
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
                    text = strings.cardDescription,
                    style = AppTypography.paragraphRegular.copy(lineHeight = 18.sp),
                    color = NeutralColors.Neutral10
                )
            }
        }
    }
}