package com.nocturna.votechain.ui.screens.votepage

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.ElectionPair
import com.nocturna.votechain.data.network.ElectionNetworkClient
import com.nocturna.votechain.data.network.PartyPhotoHelper
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.utils.CandidatePhotoHelper
import com.nocturna.votechain.utils.CoilAuthHelper
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.candidate.ElectionViewModel

/**
 * Enhanced CandidateSelectionScreen with OTP integration
 *
 * Changes from original:
 * 1. Removed direct vote casting logic
 * 2. Added OTP token validation
 * 3. Vote button now navigates to vote confirmation
 * 4. Enhanced error handling for OTP-related issues
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateSelectionScreen(
    onBackClick: () -> Unit = {},
    navController: NavController,
    categoryId: String,
    electionViewModel: ElectionViewModel = viewModel(factory = ElectionViewModel.Factory)
) {
    val strings = LanguageManager.getLocalizedStrings()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // === STATE MANAGEMENT ===
    val electionPairs by electionViewModel.electionPairs.collectAsState()
    val error by electionViewModel.error.collectAsState()
    val isLoading by electionViewModel.isLoading.collectAsState()

    var selectedCandidateId by remember { mutableStateOf<String?>(null) }
    var otpValidationError by remember { mutableStateOf<String?>(null) }
    var isOTPValid by remember { mutableStateOf(false) }

    // Check token status
    val hasToken = remember { CoilAuthHelper.hasValidToken(context) }

    // === OTP VALIDATION ===
    LaunchedEffect(Unit) {
        // Reset image loader and fetch election pairs
        CoilAuthHelper.reset()
        electionViewModel.fetchElectionPairs()

        // Validate OTP token when screen loads
        validateOTPToken(context) { isValid, errorMessage ->
            isOTPValid = isValid
            otpValidationError = errorMessage
        }
    }

    // Log token status
    LaunchedEffect(hasToken) {
        Log.d("CandidateSelectionScreen", "Token status: ${if (hasToken) "Available" else "Missing"}")
        Log.d("CandidateSelectionScreen", "OTP validation status: ${if (isOTPValid) "Valid" else "Invalid"}")
    }

    // === UI IMPLEMENTATION ===
    Column(modifier = Modifier.fillMaxSize()) {

        // === TOP BAR ===
        TopAppBar(
            title = {
                Text(
                    text = "Select Candidate",
                    style = AppTypography.heading4Regular
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Back",
                        tint = MainColors.Primary1
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // === OTP VALIDATION STATUS ===
        OTPValidationIndicator(
            isValid = isOTPValid,
            error = otpValidationError,
            onRetryOTP = {
                // Navigate back to OTP verification
                navController.navigate("otp_verification/$categoryId") {
                    popUpTo("candidate_selection/$categoryId") { inclusive = true }
                }
            }
        )

        // === CONTENT BASED ON STATE ===
        when {
            // Loading state
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingScreen()
                }
            }

            // Error state
            error != null -> {
                ErrorContent(
                    error = error,
                    onRetry = {
                        CoilAuthHelper.reset()
                        electionViewModel.fetchElectionPairs()
                    }
                )
            }

            // Success state with candidates
            electionPairs.isNotEmpty() -> {
                CandidatesContent(
                    electionPairs = electionPairs,
                    selectedCandidateId = selectedCandidateId,
                    onCandidateSelect = { candidateId ->
                        selectedCandidateId = candidateId
                        Log.d("CandidateSelectionScreen", "Candidate selected: $candidateId")
                    },
                    isOTPValid = isOTPValid,
                    onVoteClick = { electionPairId ->
                        if (isOTPValid) {
                            // Navigate to vote confirmation with the selected candidate
                            navController.navigate("vote_confirmation/$categoryId/$electionPairId")
                        } else {
                            // Navigate back to OTP verification
                            navController.navigate("otp_verification/$categoryId") {
                                popUpTo("candidate_selection/$categoryId") { inclusive = true }
                            }
                        }
                    },
                    onViewProfile = { candidateId ->
                        navController.navigate("candidate_detail_api/$candidateId")
                    },
                    scrollState = scrollState
                )
            }

            // Empty state
            else -> {
                EmptyContent()
            }
        }
    }
}

// === HELPER FUNCTIONS ===

/**
 * Validate OTP token from SharedPreferences
 */
private fun validateOTPToken(
    context: Context,
    callback: (isValid: Boolean, error: String?) -> Unit
) {
    val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
    val otpToken = sharedPreferences.getString("current_otp_token", null)
    val otpTimestamp = sharedPreferences.getLong("otp_token_timestamp", 0)

    when {
        otpToken.isNullOrEmpty() -> {
            Log.w("CandidateSelectionScreen", "No OTP token found")
            callback(false, "OTP verification required")
        }
        (System.currentTimeMillis() - otpTimestamp) > 10 * 60 * 1000 -> {
            Log.w("CandidateSelectionScreen", "OTP token expired")
            callback(false, "OTP token expired")
        }
        else -> {
            Log.d("CandidateSelectionScreen", "OTP token is valid")
            callback(true, null)
        }
    }
}

// === COMPOSABLE COMPONENTS ===

@Composable
fun OTPValidationIndicator(
    isValid: Boolean,
    error: String?,
    onRetryOTP: () -> Unit
) {
    if (!isValid || error != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Red.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OTP Verification Required",
                        style = AppTypography.paragraphMedium,
                        color = Color.Red
                    )
                    if (error != null) {
                        Text(
                            text = error,
                            style = AppTypography.smallParagraphRegular,
                            color = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }

                TextButton(onClick = onRetryOTP) {
                    Text(
                        text = "Verify OTP",
                        color = Color.Red
                    )
                }
            }
        }
    } else {
        // Success indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Green.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OTP Verified - Ready to vote",
                    style = AppTypography.smallParagraphMedium,
                    color = Color.Green
                )
            }
        }
    }
}

@Composable
fun CandidatesContent(
    electionPairs: List<ElectionPair>,
    selectedCandidateId: String?,
    onCandidateSelect: (String) -> Unit,
    isOTPValid: Boolean,
    onVoteClick: (String) -> Unit,
    onViewProfile: (String) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Candidate cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            electionPairs.forEach { pair ->
                EnhancedCandidateCard(
                    electionPair = pair,
                    isSelected = selectedCandidateId == pair.id,
                    onSelect = { onCandidateSelect(pair.id) },
                    onViewProfile = onViewProfile
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Vote Button
        VoteActionButton(
            selectedCandidateId = selectedCandidateId,
            isOTPValid = isOTPValid,
            onVoteClick = onVoteClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun EnhancedCandidateCard(
    electionPair: ElectionPair,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onViewProfile: (String) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MainColors.Primary1.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(2.dp, MainColors.Primary1) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Candidate number and selection indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Candidate ${electionPair.election_no}",
                    style = AppTypography.heading6Bold,
                    color = if (isSelected) MainColors.Primary1 else PrimaryColors.Primary70
                )

                if (isSelected) {
                    Icon(
                        painter = painterResource(id = R.drawable.tickcircle),
                        contentDescription = "Selected",
                        tint = MainColors.Primary1,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // President and Vice President info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // President info
                CandidateInfo(
                    title = "President",
                    name = electionPair.president.full_name,
                    photoUrl = CandidatePhotoHelper.getPresidentPhotoUrl(electionPair.id),
                    onViewProfile = {
                        val candidateId = "president_${electionPair.id}"
                        onViewProfile(candidateId)
                    },
                    modifier = Modifier.weight(1f)
                )

                // Vice President info
                CandidateInfo(
                    title = "Vice President",
                    name = electionPair.vice_president.full_name,
                    photoUrl = CandidatePhotoHelper.getVicePresidentPhotoUrl(electionPair.id),
                    onViewProfile = {
                        val candidateId = "vice_president_${electionPair.id}"
                        onViewProfile(candidateId)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Supporting parties
            if (electionPair.supporting_parties?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Supporting Parties",
                    style = AppTypography.smallParagraphMedium,
                    color = NeutralColors.Neutral60
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(electionPair.supporting_parties?.take(3) ?: emptyList()) { party ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(PartyPhotoHelper.getPartyPhotoUrl(party.party.id))
                                .crossfade(true)
                                .build(),
                            contentDescription = party.party.name,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CandidateInfo(
    title: String,
    name: String,
    photoUrl: String,
    onViewProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Photo
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "$title photo",
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(NeutralColors.Neutral20),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(NeutralColors.Neutral20),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Default photo",
                        tint = NeutralColors.Neutral50
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = title,
            style = AppTypography.smallParagraphMedium,
            color = NeutralColors.Neutral60,
            textAlign = TextAlign.Center
        )

        // Name
        Text(
            text = name,
            style = AppTypography.paragraphMedium,
            color = PrimaryColors.Primary80,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(4.dp))

        // View profile button
        TextButton(
            onClick = onViewProfile,
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                text = "View Profile",
                style = AppTypography.smallParagraphMedium,
                color = MainColors.Primary1
            )
        }
    }
}

@Composable
fun VoteActionButton(
    selectedCandidateId: String?,
    isOTPValid: Boolean,
    onVoteClick: (String) -> Unit
) {
    Button(
        onClick = {
            selectedCandidateId?.let { candidateId ->
                onVoteClick(candidateId)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        enabled = selectedCandidateId != null && isOTPValid,
        colors = ButtonDefaults.buttonColors(
            containerColor = MainColors.Primary1,
            disabledContainerColor = MainColors.Primary1.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = when {
                selectedCandidateId == null -> "Select Candidate First"
                !isOTPValid -> "OTP Verification Required"
                else -> "Confirm Vote"
            },
            style = AppTypography.paragraphMedium,
            color = Color.White
        )
    }
}

@Composable
fun ErrorContent(
    error: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = error ?: "Unknown error occurred",
                style = AppTypography.heading5Regular,
                color = NeutralColors.Neutral70,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColors.Primary1
                )
            ) {
                Text("Try Again")
            }
        }
    }
}

@Composable
fun EmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No candidates available",
                style = AppTypography.heading5Regular,
                color = NeutralColors.Neutral70
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please check back later",
                style = AppTypography.paragraphRegular,
                color = NeutralColors.Neutral50
            )
        }
    }
}