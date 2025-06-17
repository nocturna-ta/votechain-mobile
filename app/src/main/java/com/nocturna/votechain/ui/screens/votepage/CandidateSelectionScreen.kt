package com.nocturna.votechain.ui.screens.votepage

import android.content.Context
import android.util.Log
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

    Log.d("CandidateSelectionScreen", "User token status: ${if (userToken.isNotEmpty()) "Available" else "Missing"}")

    // Check authentication before fetching data
    LaunchedEffect(Unit) {
        // Ensure ElectionNetworkClient is properly initialized with the context
        val isNetworkClientReady = ElectionNetworkClient.ensureInitialized(context)
        Log.d("CandidateSelectionScreen", "ElectionNetworkClient initialization status: $isNetworkClientReady")

        if (userToken.isEmpty()) {
            Log.e("CandidateSelectionScreen", "No authentication token found - redirecting to login")
            // Navigate back to login if no token
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            Log.d("CandidateSelectionScreen", "Token found - fetching election pairs")

            // If token exists but not in NetworkClient, store it there
            if (userToken.isNotEmpty() && !ElectionNetworkClient.hasValidToken()) {
                Log.d("CandidateSelectionScreen", "Setting token in ElectionNetworkClient")
                ElectionNetworkClient.saveUserToken(userToken)
            }

            electionViewModel.fetchElectionPairs()
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
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Centered title
            Text(
                text = strings.candidateSelection,
                style = AppTypography.heading4Regular,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Main content container with background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            when {
                isLoading -> {
                    LoadingScreen()
                }
                error != null -> {
                    // Show error message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error Loading Candidates",
                            style = AppTypography.heading4Medium,
                            color = MainColors.Primary1
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = error ?: "Unknown error",
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
                            Text("Retry")
                        }
                    }
                }
                electionPairs.isEmpty() -> {
                    // Show empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Candidates Available",
                            style = AppTypography.heading4Medium,
                            color = NeutralColors.Neutral70
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Please check back later",
                            style = AppTypography.paragraphRegular,
                            color = NeutralColors.Neutral60
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = 100.dp) // Space for the bottom button
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Show API status banner if using fallback data
                        val isUsingFallbackData = electionPairs.any { it.id.startsWith("fallback-") }
                        if (isUsingFallbackData) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                        text = "Using offline data",
                                        style = AppTypography.smallParagraphRegular,
                                        color = MainColors.Primary1
                                    )
                                }
                            }
                        }

                        // Use fallback data if API data is empty
                        val candidatesData = if (electionPairs.isEmpty()) {
                            getFallbackElectionPairs()
                        } else {
                            electionPairs
                        }

                        // Candidate cards from API data or fallback data
                        candidatesData.forEach { electionPair ->
                            CandidateCard(
                                electionPair = electionPair,
                                isSelected = selectedCandidateNumber == electionPair.election_no.toIntOrNull(),
                                onSelect = {
                                    selectedCandidateNumber = electionPair.election_no.toIntOrNull()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Submit button at the bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                // Submit the vote and navigate back
                                selectedCandidateNumber?.let { candidateNumber ->
                                    // Use the current candidates data (either API or fallback)
                                    val currentCandidatesData = if (electionPairs.isEmpty()) {
                                        getFallbackElectionPairs()
                                    } else {
                                        electionPairs
                                    }

                                    // Find the selected election pair
                                    val selectedPair = currentCandidatesData.find {
                                        it.election_no.toIntOrNull() == candidateNumber
                                    }
                                    selectedPair?.let { pair ->
                                        viewModel.submitVote(categoryId, "candidate_${pair.id}")
                                        navController.popBackStack()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4DD0E1), // Teal/Cyan color from the design
                                disabledContainerColor = NeutralColors.Neutral30
                            ),
                            enabled = selectedCandidateNumber != null
                        ) {
                            Text(
                                text = "Submit",
                                style = AppTypography.heading4SemiBold,
                                color = Color.White
                            )
                        }
                    }
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

    // Explicitly reset the image loader to ensure we get a fresh one with current token
    LaunchedEffect(Unit) {
        CoilAuthHelper.reset()
    }

    // Get the authenticated image loader
    val imageLoader = remember(context) {
        CoilAuthHelper.getImageLoader(context)
    }

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
                        color = Color(0xFF4DD0E1),
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
                text = "Candidate",
                style = AppTypography.paragraphRegular,
                color = NeutralColors.Neutral60
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = electionPair.election_no,
                style = AppTypography.heading2Bold,
                color = NeutralColors.Neutral80
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Single combined candidate photo using API endpoint /v1/election/pairs/{id}/photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F0)) // Light gray background for loading
            ) {
                if (isUsingFallbackData) {
                    // Use local drawable for fallback data (single combined photo)
                    Image(
                        painter = painterResource(id = getCombinedPhotoDrawable),
                        contentDescription = "Candidate Pair Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // For API data, use the /v1/election/pairs/{id}/photo endpoint
                    val pairPhotoUrl = CandidatePhotoHelper.getPairPhotoUrl(electionPair.id)
                    val token = ElectionNetworkClient.getUserToken()

                    Log.d("CandidateCard", "Loading pair photo from API: $pairPhotoUrl")
                    Log.d("CandidateCard", "Token available: ${token.isNotEmpty()}")

                    // Create an explicit ImageRequest with authentication headers
                    val imageRequest = ImageRequest.Builder(context)
                        .data(pairPhotoUrl)
                        .crossfade(true)
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("ngrok-skip-browser-warning", "true")
                        .build()

                    // Display a colored background to see boundaries
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)) // Light gray background
                    ) {
                        SubcomposeAsyncImage(
                            model = imageRequest,
                            contentDescription = "Candidate Pair Photo",
                            imageLoader = imageLoader,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Inside, // Changed from Crop to Inside to see full image
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MainColors.Primary1,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }
                            },
                            error = { state ->
                                Log.e("CandidateCard", "Failed to load pair photo from API: $pairPhotoUrl")
                                Log.e("CandidateCard", "Error: ${state.painter}")

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Show error icon
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                        contentDescription = "Error",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .padding(bottom = 8.dp)
                                    )

                                    // Show fallback image
                                    Image(
                                        painter = painterResource(id = getCombinedPhotoDrawable),
                                        contentDescription = "Candidate Pair Photo (Fallback)",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        contentScale = ContentScale.Inside
                                    )
                                }
                            },
                            success = { state ->
                                Log.d("CandidateCard", "Successfully loaded pair photo from API: $pairPhotoUrl")
                                Log.d("CandidateCard", "Image width: ${state.painter.intrinsicSize.width}, height: ${state.painter.intrinsicSize.height}")

                                // Display the image with a border to verify it's rendering
                                Image(
                                    painter = state.painter,
                                    contentDescription = "Candidate Pair Photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(2.dp, Color.Green),
                                    contentScale = ContentScale.Inside
                                )
                            }
                        )
                    }
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

            // Proposing parties from API data
            Text(
                text = "Proposing Parties",
                style = AppTypography.paragraphRegular,
                color = NeutralColors.Neutral60
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Party logos from API data
            if (electionPair.supporting_parties?.isNotEmpty() == true) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(electionPair.supporting_parties.size) { index ->
                        val supportingParty = electionPair.supporting_parties[index]
                        val partyPhotoUrl = PartyPhotoHelper.getPartyPhotoUrl(supportingParty.id)

                        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                            SubcomposeAsyncImage(
                                model = partyPhotoUrl,
                                contentDescription = "Party Logo",
                                imageLoader = imageLoader,
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
                                    Image(
                                        painter = painterResource(id = R.drawable.pp_pkb),
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
        // Note: For preview, we need to provide mock parameters
        // In actual implementation, these would be provided by the navigation system
        CandidateSelectionScreen(
            navController = previewNavController,
            categoryId = "preview",
            viewModel = viewModel(), // This will need proper initialization in real app
            electionViewModel = viewModel() // This will need proper initialization in real app
        )
    }
}