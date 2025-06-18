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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.nocturna.votechain.R
import com.nocturna.votechain.data.fallback.FallbackElectionData.getFallbackElectionPairs
import com.nocturna.votechain.data.model.ElectionPair
import com.nocturna.votechain.data.network.ElectionNetworkClient
import com.nocturna.votechain.data.network.PartyPhotoHelper
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.screens.homepage.CandidatePresidentScreen
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.ui.theme.VotechainTheme
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

    val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
    val userToken = sharedPreferences.getString("user_token", "") ?: ""

    // Check if we have data, otherwise use fallback
    val displayPairs = if (electionPairs.isNotEmpty()) electionPairs else getFallbackElectionPairs()
    val isUsingFallbackData = electionPairs.isEmpty()

    // Fetch data on screen load
    LaunchedEffect(Unit) {
        if (electionPairs.isEmpty()) {
            electionViewModel.fetchElectionPairs()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
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

            Text(
                text = "Select Candidate",
                style = AppTypography.heading4Regular,
                color = PrimaryColors.Primary80,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Main content
        when {
            isLoading -> {
                LoadingScreen()
            }
            error != null && !isUsingFallbackData -> {
                // Show error only if no fallback data
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error Loading Data",
                            style = AppTypography.heading4Medium,
                            color = MainColors.Primary1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "Unknown error occurred",
                            style = AppTypography.paragraphRegular,
                            color = NeutralColors.Neutral70,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { electionViewModel.fetchElectionPairs() },
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
                // Show candidates list
                if (isUsingFallbackData) {
                    // Show API status banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MainColors.Primary1.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.back), // Replace with info icon
                                contentDescription = "Info",
                                tint = MainColors.Primary1,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Using offline data due to connection issues",
                                style = AppTypography.smallParagraphRegular,
                                color = MainColors.Primary1
                            )
                        }
                    }
                }

                // Candidates list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    displayPairs.forEachIndexed { index, electionPair ->
                        CandidateSelectionCard(
                            electionPair = electionPair,
                            candidateNumber = index + 1,
                            isSelected = selectedCandidateNumber == (index + 1),
                            isUsingFallbackData = isUsingFallbackData,
                            onCandidateSelected = { candidateNumber ->
                                selectedCandidateNumber = candidateNumber
//                                viewModel.selectCandidate(candidateNumber, electionPair.id)
                            }
                        )

                        if (index < displayPairs.size - 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for voting button
                }
            }
        }

        // Fixed bottom voting button
        if (selectedCandidateNumber != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate("voting_detail/$categoryId")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainColors.Primary1
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Vote for Candidate ${selectedCandidateNumber}",
                        style = AppTypography.paragraphSemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateSelectionCard(
    electionPair: ElectionPair,
    candidateNumber: Int,
    isSelected: Boolean,
    isUsingFallbackData: Boolean,
    onCandidateSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val strings = LanguageManager.getLocalizedStrings()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCandidateSelected(candidateNumber) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MainColors.Primary1.copy(alpha = 0.1f) else Color.White
        ),
        border = if (isSelected) BorderStroke(2.dp, MainColors.Primary1) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Candidate number
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isSelected) MainColors.Primary1 else NeutralColors.Neutral30,
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = candidateNumber.toString(),
                    style = AppTypography.heading5SemiBold,
                    color = if (isSelected) Color.White else NeutralColors.Neutral80
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // UPDATED: Candidate photo menggunakan API /v1/election/pairs/{id}/photo
            Box(
                modifier = Modifier
                    .width(212.dp)
                    .height(167.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF0F0F0)) // Light gray background for loading
            ) {
                if (isUsingFallbackData) {
                    // Use local drawable for fallback data
                    val getCombinedPhotoDrawable = when (candidateNumber) {
                        1 -> R.drawable.pc_anies
                        2 -> R.drawable.pc_prabowo
                        3 -> R.drawable.pc_ganjar
                        else -> R.drawable.ic_launcher_background
                    }
                    Image(
                        painter = painterResource(id = getCombinedPhotoDrawable),
                        contentDescription = "Candidate Pair Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // UPDATED: Menggunakan API endpoint /v1/election/pairs/{id}/photo dengan authorization
                    val pairPhotoUrl = CandidatePhotoHelper.getPairPhotoUrl(electionPair.id)
                    val token = ElectionNetworkClient.getUserToken()

                    Log.d("CandidateSelectionCard", "Loading pair photo from API: $pairPhotoUrl")
                    Log.d("CandidateSelectionCard", "Token available: ${token.isNotEmpty()}")

                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(pairPhotoUrl)
                            .crossfade(true)
                            .apply {
                                // UPDATED: Menambahkan authorization header untuk mengakses API
                                if (token.isNotEmpty()) {
                                    addHeader("Authorization", "Bearer $token")
                                    addHeader("ngrok-skip-browser-warning", "true")
                                }
                            }
                            .listener(
                                onStart = {
                                    Log.d("CandidateSelectionCard", "Starting to load image: $pairPhotoUrl")
                                },
                                onSuccess = { _, _ ->
                                    Log.d("CandidateSelectionCard", "✅ Image loaded successfully: $pairPhotoUrl")
                                },
                                onError = { _, error ->
                                    Log.e("CandidateSelectionCard", "❌ Image loading failed: $pairPhotoUrl", error.throwable)
                                }
                            )
                            .build(),
                        imageLoader = CoilAuthHelper.getImageLoader(context), // UPDATED: Menggunakan CoilAuthHelper untuk authentication
                        contentDescription = "Candidate Pair Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Inside,
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
                            // Fallback image on error
                            val getCombinedPhotoDrawable = when (candidateNumber) {
                                1 -> R.drawable.ic_launcher_background
                                2 -> R.drawable.ic_launcher_background
                                3 -> R.drawable.ic_launcher_background
                                else -> R.drawable.ic_launcher_background
                            }
                            Image(
                                painter = painterResource(id = getCombinedPhotoDrawable),
                                contentDescription = "Candidate Pair Photo (Fallback)",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Inside
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Candidate names
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
                        text = "President",
                        style = AppTypography.smallParagraphSemiBold,
                        color = NeutralColors.Neutral60,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = electionPair.president.full_name,
                        style = AppTypography.paragraphSemiBold,
                        color = NeutralColors.Neutral80,
                        textAlign = TextAlign.Center
                    )
                }

                // Divider
                Spacer(modifier = Modifier.width(16.dp))

                // Vice President name
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Vice President",
                        style = AppTypography.smallParagraphSemiBold,
                        color = NeutralColors.Neutral60,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = electionPair.vice_president.full_name,
                        style = AppTypography.paragraphSemiBold,
                        color = NeutralColors.Neutral80,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // UPDATED: Proposing parties section - menggunakan API yang sama dengan CandidatePresidentScreen
            Text(
                text = "Proposing Parties",
                style = AppTypography.paragraphRegular,
                color = NeutralColors.Neutral60
            )

            Spacer(modifier = Modifier.height(8.dp))

            // UPDATED: Party logos menggunakan PartyPhotoHelper seperti di CandidatePresidentScreen
            if (electionPair.supporting_parties?.isNotEmpty() == true) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(electionPair.supporting_parties.size) { index ->
                        val supportingParty = electionPair.supporting_parties[index]
                        // UPDATED: Menggunakan PartyPhotoHelper.getPartyPhotoUrl() yang sama dengan CandidatePresidentScreen
                        val partyPhotoUrl = PartyPhotoHelper.getPartyPhotoUrl(supportingParty.party.id)
                        val token = ElectionNetworkClient.getUserToken()

                        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(partyPhotoUrl)
                                    .crossfade(true)
                                    .apply {
                                        // UPDATED: Menambahkan authorization header untuk logo party
                                        if (token.isNotEmpty()) {
                                            addHeader("Authorization", "Bearer $token")
                                            addHeader("ngrok-skip-browser-warning", "true")
                                        }
                                    }
                                    .listener(
                                        onStart = {
                                            Log.d("PartyLogo", "🔄 Loading party logo: ${supportingParty.party.name}")
                                            Log.d("PartyLogo", "📍 URL: $partyPhotoUrl")
                                        },
                                        onSuccess = { _, _ ->
                                            Log.d("PartyLogo", "✅ Party logo loaded: ${supportingParty.party.name}")
                                        },
                                        onError = { _, error ->
                                            Log.e("PartyLogo", "❌ Party logo failed: ${supportingParty.party.name}")
                                            Log.e("PartyLogo", "📍 URL: $partyPhotoUrl")
                                            Log.e("PartyLogo", "🔥 Error: ${error.throwable?.message}")
                                        }
                                    )
                                    .build(),
                                imageLoader = CoilAuthHelper.getImageLoader(context), // UPDATED: Menggunakan CoilAuthHelper untuk authentication
                                contentDescription = "Logo ${supportingParty.party.name}",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Fit,
                                loading = {
                                    CircularProgressIndicator(
                                        color = MainColors.Primary1,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                error = {
                                    // Fallback logo
                                    val fallbackLogo = when (supportingParty.party.name.uppercase()) {
                                        "PKB" -> R.drawable.pp_pkb
                                        "PKS" -> R.drawable.pp_pks
                                        "NASDEM" -> R.drawable.pp_nasdem
                                        "PAN" -> R.drawable.pp_pan
                                        "GOLKAR" -> R.drawable.pp_golkar
                                        "GERINDRA" -> R.drawable.pp_gerinda
                                        else -> R.drawable.ic_launcher_background
                                    }
                                    Image(
                                        painter = painterResource(id = fallbackLogo),
                                        contentDescription = "Party Logo (Fallback)",
                                        modifier = Modifier.size(36.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No party data available",
                    style = AppTypography.smallParagraphRegular,
                    color = NeutralColors.Neutral50,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CandidateSelectionScreenPreview() {
    VotechainTheme {
        val previewNavController = rememberNavController()
        CandidateSelectionScreen(
            navController = previewNavController,
            categoryId = "preview",
            viewModel = viewModel(), // Mock viewModel for preview
            electionViewModel = viewModel() // Mock viewModel for preview
        )
    }
}