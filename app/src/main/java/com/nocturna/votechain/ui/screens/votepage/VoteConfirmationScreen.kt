package com.nocturna.votechain.ui.screens.votepage

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.nocturna.votechain.data.network.PartyPhotoHelper
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.utils.CandidatePhotoHelper
import com.nocturna.votechain.utils.CoilAuthHelper
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.utils.VoteErrorHandler
import com.nocturna.votechain.viewmodel.candidate.ElectionViewModel
import com.nocturna.votechain.viewmodel.vote.VotingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteConfirmationScreen(
    navController: NavController,
    electionPairId: String,
    viewModel: VotingViewModel,
    electionViewModel: ElectionViewModel = viewModel(factory = ElectionViewModel.Factory)
) {
    val strings = LanguageManager.getLocalizedStrings()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val electionPairs by electionViewModel.electionPairs.collectAsState()
    val selectedElectionPair = electionPairs.find { it.id == electionPairId }

    // Observe vote result
    val voteResult by viewModel.voteResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasVoted by viewModel.hasVoted.collectAsState()
    val voteError by viewModel.error.collectAsState()

    // Handle vote result
    LaunchedEffect(voteResult) {
        voteResult?.let { result ->
            if (result.code == 0) {
                // Success - navigate to success screen
                navController.navigate("vote_success") {
                    popUpTo("candidate_selection") { inclusive = true }
                }
            } else {
                // Error handling is done through voteError state
                Log.e("VoteConfirmation", "Vote failed: ${VoteErrorHandler.getErrorMessage(result)}")
            }
        }
    }

    // Handle vote error
    LaunchedEffect(voteError) {
        voteError?.let { error ->
            Log.e("VoteConfirmation", "Vote error: $error")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Custom top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
                    .clickable { navController.popBackStack() }
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

            // Screen title
            Text(
                text = "Confirm Your Vote", // You can add this to strings
                style = AppTypography.heading4Regular,
                color = PrimaryColors.Primary80,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (selectedElectionPair == null) {
            // Error state - candidate not found
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Candidate not found",
                        style = AppTypography.heading5Regular,
                        color = NeutralColors.Neutral70,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainColors.Primary1
                        )
                    ) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                // Confirmation message
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryColors.Primary10
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with appropriate icon
                            contentDescription = "Confirmation",
                            tint = PrimaryColors.Primary50,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "You are about to vote for:",
                            style = AppTypography.heading6Medium,
                            color = PrimaryColors.Primary70,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Selected candidate card (non-clickable version)
                ConfirmationCandidateCard(electionPair = selectedElectionPair)

                Spacer(modifier = Modifier.height(24.dp))

                // Warning message
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD) // Light yellow warning color
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with warning icon
                            contentDescription = "Warning",
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Once submitted, your vote cannot be changed. Please make sure this is your final choice.",
                            style = AppTypography.paragraphRegular,
                            color = Color(0xFF856404)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Confirm Vote Button
                    Button(
                        onClick = {
                            if (!hasVoted && !isLoading) {
                                // Get user region from SharedPreferences or use default
                                val sharedPrefs = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
                                val userRegion = sharedPrefs.getString("voter_region", "default") ?: "default"
                                val otpToken = sharedPrefs.getString("otp_token", "") ?: ""

                                viewModel.castVote(electionPairId, userRegion, otpToken)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !hasVoted && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainColors.Primary1,
                            disabledContainerColor = MainColors.Primary1.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Submitting Vote...",
                                    style = AppTypography.paragraphSemiBold,
                                    color = Color.White
                                )
                            }
                        } else {
                            Text(
                                text = "Confirm Vote",
                                style = AppTypography.paragraphSemiBold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color.White
                            )
                        }
                    }

                    // Cancel Button
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MainColors.Primary1
                        ),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = "Go Back",
                            style = AppTypography.paragraphSemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // Error message display
                voteError?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            style = AppTypography.paragraphRegular,
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ConfirmationCandidateCard(
    electionPair: ElectionPair
) {
    val context = LocalContext.current
    val isUsingFallbackData = electionPair.id.startsWith("fallback-")
    val strings = LanguageManager.getLocalizedStrings()

    // Get the authenticated image loader
    val imageLoader = remember { CoilAuthHelper.getImageLoader(context) }

    // Get appropriate local drawable resources for fallback data
    val getCombinedPhotoDrawable = when (electionPair.election_no) {
        "1" -> R.drawable.pc_anies
        "2" -> R.drawable.pc_prabowo
        "3" -> R.drawable.pc_ganjar
        else -> R.drawable.pc_anies
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Candidate number with highlighted background
            Box(
                modifier = Modifier
                    .background(
                        color = PrimaryColors.Primary50,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${strings.candidate} ",
                        style = AppTypography.heading6Medium,
                        color = Color.White
                    )
                    Text(
                        text = electionPair.election_no,
                        style = AppTypography.heading5Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Single combined candidate photo
            Box(
                modifier = Modifier
                    .width(224.dp)
                    .height(167.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (isUsingFallbackData) {
                    Image(
                        painter = painterResource(id = getCombinedPhotoDrawable),
                        contentDescription = "Candidate Pair Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    val pairPhotoUrl = CandidatePhotoHelper.getPairPhotoUrl(electionPair.id)

                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(pairPhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Candidate Pair Photo",
                        imageLoader = imageLoader,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MainColors.Primary1,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        },
                        error = {
                            Image(
                                painter = painterResource(id = getCombinedPhotoDrawable),
                                contentDescription = "Candidate Pair Photo (Fallback)",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Combined candidate names
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // President name
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.presidentialCandidate,
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral50
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = electionPair.president.full_name,
                        style = AppTypography.heading6SemiBold.copy(lineHeight = 22.sp),
                        color = PrimaryColors.Primary70,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }

                // Vice President name
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.vicePresidentialCandidate,
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral50
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = electionPair.vice_president.full_name,
                        style = AppTypography.heading6SemiBold.copy(lineHeight = 22.sp),
                        color = PrimaryColors.Primary70,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Supporting parties section
            if (!electionPair.supporting_parties.isNullOrEmpty()) {
                Text(
                    text = strings.proposingParties,
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral50,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                ) {
                    items(
                        items = electionPair.supporting_parties,
                        key = { it.id }
                    ) { supportingParty ->
                        val partyPhotoUrl = PartyPhotoHelper.getPartyPhotoUrl(supportingParty.party.id)

                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(partyPhotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "${supportingParty.party.name} Logo",
                                imageLoader = imageLoader,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                error = painterResource(id = R.drawable.ic_launcher_foreground),
                                placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                            )
                        }
                    }
                }
            }
        }
    }
}