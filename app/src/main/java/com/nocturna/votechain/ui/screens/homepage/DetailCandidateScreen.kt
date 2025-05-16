package com.nocturna.votechain.ui.screens.homepage

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.CandidatePersonalInfo
import com.nocturna.votechain.data.model.EducationEntry
import com.nocturna.votechain.data.model.WorkEntry
import com.nocturna.votechain.data.repository.CandidateRepository
import com.nocturna.votechain.domain.GetCandidateDetail
import com.nocturna.votechain.viewmodel.candidate.CandidateDetailViewModel
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.theme.AdditionalColors
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.ui.theme.VotechainTheme
import com.nocturna.votechain.ui.theme.CandidateDetailStyling

/**
 * Screen that displays detailed information about a candidate
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailCandidateScreen(
    candidateId: String,
    onBackClick: () -> Unit,
    styling: CandidateDetailStyling = CandidateDetailStyling(),
    viewModel: CandidateDetailViewModel = viewModel(
        factory = provideCandidateDetailViewModelFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Fetch candidate data when the screen is first composed
    LaunchedEffect(candidateId) {
        viewModel.fetchCandidateDetail(candidateId)
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
                    contentDescription = "Back",
                    tint = MainColors.Primary1,
                    modifier = Modifier.size(20.dp) // Smaller icon size
                )
            }

            // Screen title
            Text(
                text = "Detail Candidate",
                style = AppTypography.heading4Regular,
                color = PrimaryColors.Primary80,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Main content based on UI state
        when (uiState) {
            is CandidateDetailViewModel.CandidateDetailUiState.Loading -> {
                LoadingScreen()
            }

            is CandidateDetailViewModel.CandidateDetailUiState.Success -> {
                val data = (uiState as CandidateDetailViewModel.CandidateDetailUiState.Success).data

                // Main content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Candidate photo
                    Image(
                        painter = painterResource(id = data.personalInfo.photoResId),
                        contentDescription = "Candidate Photo",
                        modifier = Modifier
                            .size(200.dp)
                            .padding(vertical = 16.dp),
                        contentScale = ContentScale.Fit
                    )

                    // Candidate name
                    Text(
                        text = data.personalInfo.fullName,
                        style = AppTypography.heading5Bold,
                        color = PrimaryColors.Primary70,
                        textAlign = TextAlign.Center
                    )

                    // Candidate position
                    Text(
                        text = data.personalInfo.position,
                        style = AppTypography.heading6Regular,
                        color = NeutralColors.Neutral50,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    // Personal Information Section
                    PersonalInfoSection(data.personalInfo, styling)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Education History Section
                    Text(
                        text = "Education History",
                        style = styling.sectionTitleStyle,
                        color = styling.sectionTitleColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        textAlign = TextAlign.Center
                    )

                    EducationHistorySection(data.educationHistory, styling)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Work History Section
                    Text(
                        text = "Work History",
                        style = styling.sectionTitleStyle,
                        color = styling.sectionTitleColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        textAlign = TextAlign.Center
                    )

                    WorkHistorySection(data.workHistory, styling)

                    // Spacer at the bottom for better scrolling experience
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            is CandidateDetailViewModel.CandidateDetailUiState.Error -> {
                // Show error message
                val errorMessage = (uiState as CandidateDetailViewModel.CandidateDetailUiState.Error).message
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
                            text = "Terjadi kesalahan",
                            style = AppTypography.heading4Medium,
                            color = MainColors.Primary1
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = errorMessage,
                            style = AppTypography.heading5Regular,
                            color = NeutralColors.Neutral70,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.fetchCandidateDetail(candidateId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColors.Primary1
                            )
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalInfoSection(
    personalInfo: CandidatePersonalInfo,
    styling: CandidateDetailStyling
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Gender Row
        PersonalInfoRow(
            label = "Gender",
            value = personalInfo.gender,
            styling = styling
        )

        // Birth Place/Date Row
        PersonalInfoRow(
            label = "Birth Place/Date",
            value = personalInfo.birthInfo,
            styling = styling
        )

        // Religion Row
        PersonalInfoRow(
            label = "Religion",
            value = personalInfo.religion,
            styling = styling
        )

        // Education Row
        PersonalInfoRow(
            label = "Last Education",
            value = personalInfo.education,
            styling = styling,
        )

        // Occupation Row
        PersonalInfoRow(
            label = "Occupation",
            value = personalInfo.occupation,
            styling = styling
        )
    }
}

@Composable
fun PersonalInfoRow(
    label: String,
    value: String,
    styling: CandidateDetailStyling
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = label,
            style = styling.personalInfoLabelStyle,
            color = styling.personalInfoLabelColor,
            modifier = Modifier
                .weight(1f)
                .background(color = NeutralColors.Neutral10)
                .padding(vertical = 12.dp, horizontal = 8.dp)
        )

        Text(
            text = value,
            style = styling.personalInfoValueStyle,
            color = styling.personalInfoValueColor,
            modifier = Modifier
                .weight(1f)
                .background(color = NeutralColors.Neutral10)
                .padding(vertical = 12.dp, horizontal = 8.dp)
        )
    }

    Divider(
        color =  AdditionalColors.strokeColor,
        thickness = 1.dp
    )
}

@Composable
fun EducationHistorySection(
    educationHistory: List<EducationEntry>,
    styling: CandidateDetailStyling
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NeutralColors.Neutral10)
    ) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(styling.tableHeaderBackground)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Institution",
                style = styling.tableHeaderTextStyle,
                color = styling.tableHeaderTextColor,
                modifier = Modifier
                    .weight(1.5f)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Period",
                style = styling.tableHeaderTextStyle,
                color = styling.tableHeaderTextColor,
                modifier = Modifier
                    .weight(0.8f)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        // Dynamic education entries
        educationHistory.forEach { entry ->
            EducationEntryRow(entry, styling)
        }
    }
}

@Composable
fun EducationEntryRow(
    entry: EducationEntry,
    styling: CandidateDetailStyling
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeutralColors.Neutral10)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = entry.institution,
            style = styling.educationInstitutionStyle,
            color = styling.educationInstitutionColor,
            modifier = Modifier
                .weight(1.5f)
                .padding(horizontal = 8.dp)
        )

        Text(
            text = entry.period,
            style = styling.educationPeriodStyle,
            color = styling.educationPeriodColor,
            modifier = Modifier
                .weight(0.8f)
                .padding(horizontal = 8.dp)
        )
    }

    Divider(
        color = AdditionalColors.strokeColor,
        thickness = 1.dp
    )
}

@Composable
fun WorkHistorySection(
    workHistory: List<WorkEntry>,
    styling: CandidateDetailStyling
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NeutralColors.Neutral30)
    ) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(styling.tableHeaderBackground)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Institution",
                style = styling.tableHeaderTextStyle,
                color = styling.tableHeaderTextColor,
                modifier = Modifier
                    .weight(1.5f)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Position",
                style = styling.tableHeaderTextStyle,
                color = styling.tableHeaderTextColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Period",
                style = styling.tableHeaderTextStyle,
                color = styling.tableHeaderTextColor,
                modifier = Modifier
                    .weight(0.8f)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        // Dynamic work experience entries
        workHistory.forEach { entry ->
            WorkEntryRow(entry, styling)
        }
    }
}

@Composable
fun WorkEntryRow(
    entry: WorkEntry,
    styling: CandidateDetailStyling
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeutralColors.Neutral10)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = entry.institution,
            style = styling.workInstitutionStyle,
            color = styling.workInstitutionColor,
            modifier = Modifier
                .weight(1.5f)
                .padding(horizontal = 8.dp)
        )

        Text(
            text = entry.position,
            style = styling.workPositionStyle,
            color = styling.workPositionColor,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )

        Text(
            text = entry.period,
            style = styling.workPeriodStyle,
            color = styling.workPeriodColor,
            modifier = Modifier
                .weight(0.8f)
                .padding(horizontal = 8.dp)
        )
    }

    Divider(
        color =  AdditionalColors.strokeColor,
        thickness = 1.dp
    )
}

/**
 * Factory method to provide CandidateDetailViewModel
 */
@Composable
private fun provideCandidateDetailViewModelFactory(): CandidateDetailViewModel.Factory {
    val repository = remember { CandidateRepository() }
    val useCase = remember { GetCandidateDetail(repository) }
    return remember { CandidateDetailViewModel.Factory(useCase) }
}

/**
 * Preview function for DetailCandidateScreen with default styling
 */
@Preview(showBackground = true)
@Composable
fun DetailCandidateScreenPreview() {
    VotechainTheme {
        DetailCandidateScreen(
            candidateId = "anies",
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
            candidateId = "anies",
            onBackClick = {},
            styling = customStyling
        )
    }
}