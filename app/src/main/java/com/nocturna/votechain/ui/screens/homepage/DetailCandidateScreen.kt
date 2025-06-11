package com.nocturna.votechain.ui.screens.homepage

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.Candidate
import com.nocturna.votechain.data.model.CandidatePersonalInfo
import com.nocturna.votechain.data.model.EducationEntry
import com.nocturna.votechain.data.model.WorkEntry
import com.nocturna.votechain.data.repository.CandidateRepository
import com.nocturna.votechain.domain.GetCandidateDetail
import com.nocturna.votechain.ui.components.EducationHistoryTable
import com.nocturna.votechain.ui.components.TableRow
import com.nocturna.votechain.ui.components.WorkHistoryTable
import com.nocturna.votechain.viewmodel.candidate.CandidateDetailViewModel
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.theme.AdditionalColors
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.ui.theme.VotechainTheme
import com.nocturna.votechain.ui.theme.CandidateDetailStyling
import com.nocturna.votechain.utils.CandidateHelper
import com.nocturna.votechain.utils.CandidatePhotoHelper
import com.nocturna.votechain.utils.DateFormatter
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.candidate.ElectionViewModel

/**
 * Screen that displays detailed information about a candidate
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailCandidateScreen(
    candidateId: String,
    onBackClick: () -> Unit,
    styling: CandidateDetailStyling = CandidateDetailStyling(),
    viewModel: ElectionViewModel = viewModel(factory = ElectionViewModel.Factory())
) {
    val strings = LanguageManager.getLocalizedStrings()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val electionPairs by viewModel.electionPairs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Parse the candidateId to get type (president/vicePresident) and actual id
    var candidate: Candidate? by remember { mutableStateOf(null) }
    var candidatePhotoUrl: String? by remember { mutableStateOf(null) }
    var candidateType: CandidateHelper.CandidateType? by remember { mutableStateOf(null) }
    var pairId: String? by remember { mutableStateOf(null) }

    // Fetch election pairs if not already loaded
    LaunchedEffect(Unit) {
        if (electionPairs.isEmpty()) {
            viewModel.fetchElectionPairs()
        }
    }

    // Extract the candidate data based on the ID format
    LaunchedEffect(candidateId, electionPairs) {
        Log.d("DetailCandidateScreen", "LaunchedEffect triggered")
        Log.d("DetailCandidateScreen", "CandidateId received: $candidateId")
        Log.d("DetailCandidateScreen", "Election pairs count: ${electionPairs.size}")

        if (electionPairs.isNotEmpty()) {
            // Log all available pairs
            electionPairs.forEachIndexed { index, pair ->
                Log.d("DetailCandidateScreen", "Pair $index: ID=${pair.id}, President=${pair.president.full_name}, VP=${pair.vice_president.full_name}")
            }

            // Parse candidate ID untuk mendapatkan type dan pair ID
            val parts = candidateId.split("_", limit = 2)
            if (parts.size == 2) {
                val typePrefix = parts[0]
                pairId = parts[1]

                candidateType = when (typePrefix) {
                    CandidateHelper.CandidateType.PRESIDENT.prefix -> CandidateHelper.CandidateType.PRESIDENT
                    CandidateHelper.CandidateType.VICE_PRESIDENT.prefix -> CandidateHelper.CandidateType.VICE_PRESIDENT
                    else -> null
                }
                Log.d("DetailCandidateScreen", "Parsed - Type: $typePrefix, PairId: $pairId, CandidateType: $candidateType")
            }

            // Get candidate data using helper
            candidate = CandidateHelper.getCandidateFromId(candidateId, electionPairs)
            Log.d("DetailCandidateScreen", "Final candidate: ${candidate?.full_name}")
            Log.d("DetailCandidateScreen", "Candidate photo_path: ${candidate?.photo_path}")

            // Generate foto URL berdasarkan candidate type
            if (candidateType != null && pairId != null) {
                // Prioritas: gunakan endpoint API untuk foto yang spesifik
                candidatePhotoUrl = when (candidateType) {
                    CandidateHelper.CandidateType.PRESIDENT -> {
                        val presidentUrl = CandidatePhotoHelper.getPresidentPhotoUrl(pairId!!)
                        Log.d("DetailCandidateScreen", "Using president photo URL: $presidentUrl")
                        presidentUrl
                    }
                    CandidateHelper.CandidateType.VICE_PRESIDENT -> {
                        val vicePresidentUrl = CandidatePhotoHelper.getVicePresidentPhotoUrl(pairId!!)
                        Log.d("DetailCandidateScreen", "Using vice president photo URL: $vicePresidentUrl")
                        vicePresidentUrl
                    }
                    else -> null
                }

                Log.d("DetailCandidateScreen", "Final photo URL used: $candidatePhotoUrl")
            } else {
                Log.w("DetailCandidateScreen", "Cannot determine candidate type or pair ID")
            }
        } else {
            Log.w("DetailCandidateScreen", "No election pairs available")
        }
    }

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

            // Screen title
            Text(
                text = strings.detailCandidate,
                style = AppTypography.heading4Regular,
                color = PrimaryColors.Primary80,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Main content based on UI state
        when {
            isLoading -> {
                LoadingScreen()
            }
            error != null -> {
                // Show error message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
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
                            text = error ?: "Unknown error occurred",
                            style = AppTypography.heading5Regular,
                            color = NeutralColors.Neutral70,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.fetchElectionPairs() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColors.Primary1
                            )
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }
            candidate != null -> {
                // Main content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Candidate photo
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(candidatePhotoUrl)
                            .crossfade(true)
                            .error(R.drawable.ic_launcher_background)
                            .listener(
                                onStart = {
                                    Log.d("DetailCandidateScreen", "Image loading started for: $candidatePhotoUrl")
                                },
                                onSuccess = { _, _ ->
                                    Log.d("DetailCandidateScreen", "Image loaded successfully: $candidatePhotoUrl")
                                },
                                onError = { _, error ->
                                    Log.e("DetailCandidateScreen", "Image loading failed: $candidatePhotoUrl", error.throwable)
                                }
                            )
                            .build(),
                        contentDescription = "${candidate?.full_name} Photo",
                        modifier = Modifier
                            .size(200.dp)
                            .padding(vertical = 16.dp),
                        contentScale = ContentScale.Fit
                    )

                    // Candidate name
                    Text(
                        text = candidate?.full_name ?: "",
                        style = AppTypography.heading5Bold,
                        color = PrimaryColors.Primary70,
                        textAlign = TextAlign.Center
                    )

                    // Candidate type indicator
                    Text(
                        text = when (candidateType) {
                            CandidateHelper.CandidateType.PRESIDENT -> strings.presidentialCandidate
                            CandidateHelper.CandidateType.VICE_PRESIDENT -> strings.vicePresidentialCandidate
                            else -> ""
                        },
                        style = AppTypography.paragraphMedium,
                        color = NeutralColors.Neutral60,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Personal Information Section
                    CandidatePersonalInfoFromApi(candidate!!, strings, styling)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Education History Section
                    Text(
                        text = strings.educationHistory,
                        style = AppTypography.heading6SemiBold,
                        color = PrimaryColors.Primary70,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        textAlign = TextAlign.Center
                    )

                    // Convert API education data to the format used by our component
                    val educationEntries = candidate?.education_history?.map {
                        EducationEntry(
                            institution = it.institute_name,
                            period = it.year
                        )
                    } ?: emptyList()

                    EducationHistoryTable(educationEntries)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Work History Section
                    Text(
                        text = strings.workHistory,
                        style = AppTypography.heading6SemiBold,
                        color = PrimaryColors.Primary70,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        textAlign = TextAlign.Center
                    )

                    // Convert API work data to the format used by our component
                    val workEntries = candidate?.work_experience?.map {
                        WorkEntry(
                            institution = it.institute_name,
                            position = it.position,
                            period = it.year
                        )
                    } ?: emptyList()

                    WorkHistoryTable(workEntries)

                    // Spacer at the bottom for better scrolling experience
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            else -> {
                // Fallback in case no candidate data is found
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No candidate data found",
                        style = AppTypography.heading4Medium,
                        color = NeutralColors.Neutral50,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CandidatePersonalInfoFromApi(
    candidate: Candidate,
    strings: com.nocturna.votechain.utils.LocalizedStrings,
    styling: CandidateDetailStyling
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Gender
        TableRow(label = strings.genderCandidate, value = candidate.gender)

        // Birth Info - using formatted date
        val formattedBirthInfo = DateFormatter.formatBirthInfo(
            birthPlace = candidate.birth_place,
            birthDate = candidate.birth_date,
            language = "Indonesia" // You can get this from LanguageManager if needed
        )
        TableRow(label = strings.birthInfo, value = formattedBirthInfo)

        // Religion (only show if not empty)
        if (candidate.religion.isNotEmpty()) {
            TableRow(label = strings.religion, value = candidate.religion)
        }

        // Education
        TableRow(label = strings.education, value = candidate.last_education)

        // Job/Occupation
        TableRow(label = strings.occupation, value = candidate.job)
    }
}

/**
 * Preview function for DetailCandidateScreen with default styling
 */
@Preview(showBackground = true)
@Composable
fun DetailCandidateScreenPreview() {
    VotechainTheme {
        DetailCandidateScreen(
            candidateId = "president_test_id",
            onBackClick = {}
        )
    }
}

/**
 * Preview function for DetailCandidateScreen with custom styling
 */
@Preview(showBackground = true)
@Composable
fun DetailCandidateScreenCustomStylePreview() {
    // Custom styling for preview
    val customStyling = CandidateDetailStyling(
        sectionTitleColor = MainColors.Primary2,
        tableHeaderBackground = PrimaryColors.Primary20,
        tableHeaderTextColor = PrimaryColors.Primary90,
        personalInfoLabelColor = MainColors.Primary1
    )

    VotechainTheme {
        DetailCandidateScreen(
            candidateId = "president_test_id",
            onBackClick = {},
            styling = customStyling
        )
    }
}