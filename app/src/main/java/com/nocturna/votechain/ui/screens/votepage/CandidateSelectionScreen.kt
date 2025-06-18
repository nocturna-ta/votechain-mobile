package com.nocturna.votechain.ui.screens.homepage

import android.content.Context
import android.util.Log
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
import com.nocturna.votechain.viewmodel.vote.VotingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateSelectionScreen(
    onBackClick: () -> Unit = {},
    navController: NavController,
    categoryId: String,
    viewModel: VotingViewModel,
    electionViewModel: ElectionViewModel = viewModel(factory = ElectionViewModel.Factory)
) {
    val strings = LanguageManager.getLocalizedStrings()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val electionPairs by electionViewModel.electionPairs.collectAsState()
    val isLoading by electionViewModel.isLoading.collectAsState()
    val error by electionViewModel.error.collectAsState()
    var selectedCandidateNumber by remember { mutableStateOf<Int?>(null) }

    // Check token status
    val hasToken = remember { CoilAuthHelper.hasValidToken(context) }

    // Reset image loader when screen is opened to ensure fresh token
    LaunchedEffect(Unit) {
        CoilAuthHelper.reset()
        electionViewModel.fetchElectionPairs()
    }

    // Log token status
    LaunchedEffect(hasToken) {
        Log.d("CandidateSelectionScreen", "Token status: ${if (hasToken) "Available" else "Missing"}")
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
                    modifier = Modifier.size(20.dp)
                )
            }

            // Screen title
            Text(
                text = strings.selectCandidate,
                style = AppTypography.heading4Regular,
                color = PrimaryColors.Primary80,
                modifier = Modifier.align(Alignment.Center)
            )
        }
//
//    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = {
//                    Text(
//                        text = strings.selectCandidate,
//                        style = AppTypography.heading4Bold,
//                        color = NeutralColors.Neutral80
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.back),
//                            contentDescription = "Back",
//                            tint = NeutralColors.Neutral80
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                    containerColor = Color.White
//                )
//            )
//        },
//        containerColor = Color(0xFFF8F9FA),
//        bottomBar = {
//            // Bottom button section
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color.White)
//                    .padding(24.dp)
//            ) {
//                Button(
//                    onClick = {
//                        selectedCandidateNumber?.let { number ->
//                            val selectedPair = electionPairs.find {
//                                it.election_no.toIntOrNull() == number
//                            }
//                            selectedPair?.let { pair ->
////                                viewModel.setSelectedCandidate(pair.id)
//                                navController.navigate("vote/$categoryId")
//                            }
//                        }
//                    },
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = selectedCandidateNumber != null,
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MainColors.Primary1,
//                        disabledContainerColor = MainColors.Primary1.copy(alpha = 0.5f)
//                    ),
//                    shape = RoundedCornerShape(100.dp)
//                ) {
//                    Text(
//                        text = strings.candidateSelection,
//                        style = AppTypography.paragraphSemiBold,
//                        modifier = Modifier.padding(vertical = 8.dp)
//                    )
//                }
//            }
//        }
//    ) { paddingValues ->
        when {
            isLoading -> {
                LoadingScreen()
            }
            error != null -> {
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
                            text = error ?: "Unknown error occurred",
                            style = AppTypography.heading5Regular,
                            color = NeutralColors.Neutral70,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                CoilAuthHelper.reset()
                                electionViewModel.fetchElectionPairs()
                            },
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Candidate cards
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        electionPairs.forEach { pair ->
                            CandidateCard(
                                electionPair = pair,
                                isSelected = selectedCandidateNumber == pair.election_no.toIntOrNull(),
                                onSelect = {
                                    selectedCandidateNumber = pair.election_no.toIntOrNull()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun CandidateCard(
    electionPair: ElectionPair,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val context = LocalContext.current
    val isUsingFallbackData = electionPair.id.startsWith("fallback-")
    val strings = LanguageManager.getLocalizedStrings()

    // Get the authenticated image loader
    val imageLoader = remember { CoilAuthHelper.getImageLoader(context) }

    // Get appropriate local drawable resources for fallback data (combined photo)
    val getCombinedPhotoDrawable = when (electionPair.election_no) {
        "1" -> R.drawable.pc_anies
        "2" -> R.drawable.pc_prabowo
        "3" -> R.drawable.pc_ganjar
        else -> R.drawable.pc_anies
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onSelect() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = PrimaryColors.Primary50,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Candidate number
            Text(
                text = strings.candidate,
                style = AppTypography.heading6Medium,
                color = PrimaryColors.Primary60
            )

            Text(
                text = electionPair.election_no,
                style = AppTypography.heading5Bold,
                color = PrimaryColors.Primary60
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Single combined candidate photo using API endpoint /v1/election/pairs/{id}/photo
            Box(
                modifier = Modifier
                    .width(224.dp)
                    .height(167.dp)
                    .clip(RoundedCornerShape(2.dp))
            ) {
                if (isUsingFallbackData) {
                    // Use local drawable for fallback data (single combined photo)
                    Image(
                        painter = painterResource(id = getCombinedPhotoDrawable),
                        contentDescription = "Candidate Pair Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // For API data, use the /v1/election/pairs/{id}/photo endpoint
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
                            // On error, show fallback image
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

            Spacer(modifier = Modifier.height(12.dp))

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

                // Party logos in a horizontal scrollable row
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
                            modifier = Modifier
                                .size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(partyPhotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "${supportingParty.party.name} Logo",
                                imageLoader = imageLoader,
                                modifier = Modifier
                                    .fillMaxSize(),
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