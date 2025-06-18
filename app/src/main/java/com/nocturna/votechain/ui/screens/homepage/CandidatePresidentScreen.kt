package com.nocturna.votechain.ui.screens.homepage

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.ElectionPair
import com.nocturna.votechain.data.network.PartyPhotoHelper
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.ui.theme.VotechainTheme
import com.nocturna.votechain.utils.CandidateHelper
import com.nocturna.votechain.utils.CandidatePhotoHelper
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.candidate.ElectionViewModel
import com.nocturna.votechain.data.network.ElectionNetworkClient
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.theme.AdditionalColors
import com.nocturna.votechain.utils.CoilAuthHelper

@Composable
fun CandidatePresidentScreen(
    onBackClick: () -> Unit = {},
    onViewProfileClick: (String) -> Unit = {},
    navController: NavController? = null,
    electionViewModel: ElectionViewModel = viewModel(factory = ElectionViewModel.Factory)
) {
    val strings = LanguageManager.getLocalizedStrings()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val electionPairs by electionViewModel.electionPairs.collectAsState()
    val isLoading by electionViewModel.isLoading.collectAsState()
    val error by electionViewModel.error.collectAsState()

    var selectedFilter by remember { mutableStateOf(strings.allCandidates) }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Check if user is logged in
    val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
    val userToken = sharedPreferences.getString("user_token", "") ?: ""

    Log.d("CandidatePresidentScreen", "User token status: ${if (userToken.isNotEmpty()) "Available" else "Missing"}")

    val currentRoute = navController?.currentBackStackEntry?.destination?.route
    val voteId = if (currentRoute?.contains("candidate_president") == true) {
        navController.currentBackStackEntry?.arguments?.getString("voteId") ?: ""
    } else {
        ""
    }

    // Check authentication before fetching data
    LaunchedEffect(Unit) {
        // Ensure ElectionNetworkClient is properly initialized with the context
        val isNetworkClientReady = ElectionNetworkClient.ensureInitialized(context)
        Log.d("CandidatePresidentScreen", "ElectionNetworkClient initialization status: $isNetworkClientReady")

        if (userToken.isEmpty()) {
            Log.e("CandidatePresidentScreen", "No authentication token found - redirecting to login")
            // Navigate back to login if no token
            navController?.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            Log.d("CandidatePresidentScreen", "Token found - fetching election pairs")

            // If token exists but not in NetworkClient, store it there
            if (userToken.isNotEmpty() && !ElectionNetworkClient.hasValidToken()) {
                Log.d("CandidatePresidentScreen", "Setting token in ElectionNetworkClient")
                ElectionNetworkClient.saveUserToken(userToken)
            }

            electionViewModel.fetchElectionPairs()
        }
    }

    // Define the onVisionMissionClick function that uses the NavController
    val onVisionMissionClick = { pairId: String ->
        Log.d("CandidatePresidentScreen", "Vision Mission clicked for pair ID: $pairId")
        navController?.navigate("vision_mission/$pairId")
    }

    // Determine if we're using fallback data
    val isUsingFallbackData = electionPairs.any { it.id.startsWith("fallback-") }

    if (isUsingFallbackData) {
        Log.d("CandidatePresidentScreen", "Using fallback data - API is unavailable")
    }

    // Show authentication error if no token
    if (userToken.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Authentication Error",
                    tint = MainColors.Primary1,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Authentication Required",
                    style = AppTypography.heading4Medium,
                    color = MainColors.Primary1,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please login to view candidate information",
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral70,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        navController?.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainColors.Primary1
                    )
                ) {
                    Text("Go to Login")
                }
            }
        }
        return
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
                text = strings.candidatePresident,
                style = AppTypography.heading4Regular,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Show loading indicator when loading
        if (isLoading) {
            LoadingScreen()
        }
        // Show error message when there's an error
        else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Error loading candidates",
                        style = AppTypography.heading5Bold,
                        color = PrimaryColors.Primary70
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Check if it's an authentication error
                    val isAuthError = error?.contains("401") == true ||
                            error?.contains("Unauthenticated") == true ||
                            error?.contains("authorization") == true

                    if (isAuthError) {
                        Text(
                            text = "Authentication failed. Please login again.",
                            style = AppTypography.paragraphRegular,
                            color = NeutralColors.Neutral70,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                // Clear token and redirect to login
                                with(sharedPreferences.edit()) {
                                    remove("user_token")
                                    apply()
                                }
                                navController?.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColors.Primary1
                            )
                        ) {
                            Text("Login Again")
                        }
                    } else {
                        Text(
                            text = error ?: "Unknown error",
                            style = AppTypography.paragraphRegular,
                            color = NeutralColors.Neutral70,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { electionViewModel.fetchElectionPairs()},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColors.Primary1
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
        // Show candidates when data is loaded
        else {
            // Show API status banner if using fallback data
            if (isUsingFallbackData) {
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
                            text = "Using offline data due to authentication issues",
                            style = AppTypography.smallParagraphRegular,
                            color = MainColors.Primary1
                        )
                    }
                }
            }

            // Filter dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // Generate dropdown options based on available candidates
                val dropdownOptions = mutableListOf(strings.allCandidates)
                dropdownOptions.addAll(electionPairs.map { "Candidate ${it.election_no}" })

                // Make the entire field clickable to toggle dropdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedFilter,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        textStyle = AppTypography.heading5Regular,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.down2),
                                contentDescription = "Filter",
                                tint = if (expandedDropdown) MainColors.Primary1 else NeutralColors.Neutral40,
                                modifier = Modifier.clickable { expandedDropdown = !expandedDropdown }
                            )
                        }
                    )
                }

                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    dropdownOptions.forEach { option ->
                        DropdownMenuItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (option == selectedFilter)
                                        MainColors.Primary1
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                                .padding(vertical = 4.dp),
                            text = {
                                Text(
                                    text = option,
                                    style = AppTypography.heading5Regular,
                                    color = if (option == selectedFilter)
                                        NeutralColors.Neutral10
                                    else
                                        MaterialTheme.colorScheme.inverseSurface
                                )
                            },
                            onClick = {
                                selectedFilter = option
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            // Candidates list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                // Filter candidates based on the selected filter
                val filteredPairs = if (selectedFilter == strings.allCandidates) {
                    electionPairs
                } else {
                    val candidateNumber = selectedFilter.removePrefix("Candidate ").trim()
                    electionPairs.filter { it.election_no == candidateNumber }
                }

                // Display candidate cards
                filteredPairs.forEach { pair ->
                    CandidateCardFromApi(
                        electionPair = pair,
                        onViewPresidentProfile = {
                            // Navigate to detail screen with president data
                            val candidateId = CandidateHelper.createCandidateId(
                                CandidateHelper.CandidateType.PRESIDENT,
                                pair.id
                            )
                            Log.d("CandidatePresidentScreen", "President profile clicked")
                            Log.d("CandidatePresidentScreen", "- Pair ID: ${pair.id}")
                            Log.d("CandidatePresidentScreen", "- President Name: ${pair.president.full_name}")
                            Log.d("CandidatePresidentScreen", "- Generated Candidate ID: $candidateId")
                            Log.d("CandidatePresidentScreen", "- Navigating to: candidate_detail_api/$candidateId")

                            navController?.navigate("candidate_detail_api/$candidateId")
                        },
                        onViewVicePresidentProfile = {
                            // Navigate to detail screen with vice president data
                            val candidateId = CandidateHelper.createCandidateId(
                                CandidateHelper.CandidateType.VICE_PRESIDENT,
                                pair.id
                            )
                            Log.d("CandidatePresidentScreen", "Vice President profile clicked")
                            Log.d("CandidatePresidentScreen", "- Pair ID: ${pair.id}")
                            Log.d("CandidatePresidentScreen", "- Vice President Name: ${pair.vice_president.full_name}")
                            Log.d("CandidatePresidentScreen", "- Generated Candidate ID: $candidateId")
                            Log.d("CandidatePresidentScreen", "- Navigating to: candidate_detail_api/$candidateId")

                            navController?.navigate("candidate_detail_api/$candidateId")
                        },
                        onVisionMissionClick = {
                            // Pass the actual pair.id (UUID) instead of election_no
                            onVisionMissionClick(pair.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
fun CandidateCardFromApi(
    electionPair: ElectionPair,
    onViewPresidentProfile: () -> Unit,
    onViewVicePresidentProfile: () -> Unit,
    onVisionMissionClick: () -> Unit
) {
    val strings = LanguageManager.getLocalizedStrings()
    val context = LocalContext.current

    // Generate photo URLs for debugging
    val presidentPhotoUrl = CandidatePhotoHelper.getPresidentPhotoUrl(electionPair.id)
    val vicePresidentPhotoUrl = CandidatePhotoHelper.getVicePresidentPhotoUrl(electionPair.id)

    Log.d("CandidateCardFromApi", "Card created for pair: ${electionPair.id}")
    Log.d("CandidateCardFromApi", "President photo URL: $presidentPhotoUrl")
    Log.d("CandidateCardFromApi", "Vice President photo URL: $vicePresidentPhotoUrl")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Candidate Number
            Text(
                text = strings.candidate,
                style = AppTypography.heading6Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = electionPair.election_no,
                style = AppTypography.heading5Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Table structure with 2 columns
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Row 1: Candidate Titles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Presidential Column Header
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.presidentialCandidate,
                            style = AppTypography.paragraphRegular,
                            color = MaterialTheme.colorScheme.tertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Vice Presidential Column Header
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.vicePresidentialCandidate,
                            style = AppTypography.paragraphRegular,
                            color = MaterialTheme.colorScheme.tertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Row 2: Candidate Photos and Names
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Presidential Column
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // President Photo
                        Box(
                            modifier = Modifier
                                .size(height = 120.dp, width = 90.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(presidentPhotoUrl)
                                    .crossfade(true)
                                    .error(R.drawable.ic_launcher_background)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .listener(
                                        onStart = {
                                            Log.d("CandidateCard", "🔄 Loading president photo...")
                                            Log.d("CandidateCard", "📍 URL: $presidentPhotoUrl")
                                        },
                                        onSuccess = { _, result ->
                                            Log.d(
                                                "CandidateCard",
                                                "✅ President photo loaded successfully"
                                            )
                                            Log.d(
                                                "CandidateCard",
                                                "📊 Image size: ${result.drawable.intrinsicWidth}x${result.drawable.intrinsicHeight}"
                                            )
                                        },
                                        onError = { _, error ->
                                            Log.e(
                                                "CandidateCard",
                                                "❌ President photo loading failed"
                                            )
                                            Log.e("CandidateCard", "📍 URL: $presidentPhotoUrl")
                                            Log.e(
                                                "CandidateCard",
                                                "🔥 Error: ${error.throwable?.message}"
                                            )
                                            error.throwable?.printStackTrace()

                                            if (presidentPhotoUrl.isNotEmpty()) {
                                                CandidatePhotoHelper.validatePhotoUrl(
                                                    presidentPhotoUrl
                                                )
                                            }
                                        }
                                    )
                                    .build(),
                                imageLoader = CoilAuthHelper.getImageLoader(context),
                                contentDescription = "Presidential Candidate ${electionPair.president.full_name}",
                                contentScale = ContentScale.FillHeight
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // President Name
                        Text(
                            text = electionPair.president.full_name,
                            style = AppTypography.heading6SemiBold,
                            color = MaterialTheme.colorScheme.onTertiary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // President View Profile Button
                        OutlinedButton(
                            onClick = onViewPresidentProfile,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.height(24.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = strings.viewProfile,
                                style = AppTypography.smallParagraphRegular,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.right2),
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Vice Presidential Column
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Vice President Photo
                        Box(
                            modifier = Modifier
                                .size(height = 120.dp, width = 90.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(vicePresidentPhotoUrl)
                                    .crossfade(true)
                                    .error(R.drawable.ic_launcher_background)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .listener(
                                        onStart = {
                                            Log.d(
                                                "CandidateCard",
                                                "🔄 Loading vice president photo..."
                                            )
                                            Log.d("CandidateCard", "📍 URL: $vicePresidentPhotoUrl")
                                        },
                                        onSuccess = { _, result ->
                                            Log.d(
                                                "CandidateCard",
                                                "✅ Vice president photo loaded successfully"
                                            )
                                            Log.d(
                                                "CandidateCard",
                                                "📊 Image size: ${result.drawable.intrinsicWidth}x${result.drawable.intrinsicHeight}"
                                            )
                                        },
                                        onError = { _, error ->
                                            Log.e(
                                                "CandidateCard",
                                                "❌ Vice president photo loading failed"
                                            )
                                            Log.e("CandidateCard", "📍 URL: $vicePresidentPhotoUrl")
                                            Log.e(
                                                "CandidateCard",
                                                "🔥 Error: ${error.throwable?.message}"
                                            )
                                            error.throwable?.printStackTrace()

                                            if (vicePresidentPhotoUrl.isNotEmpty()) {
                                                CandidatePhotoHelper.validatePhotoUrl(
                                                    vicePresidentPhotoUrl
                                                )
                                            }
                                        }
                                    )
                                    .build(),
                                imageLoader = CoilAuthHelper.getImageLoader(context),
                                contentDescription = "Vice Presidential Candidate ${electionPair.vice_president.full_name}",
                                contentScale = ContentScale.FillHeight,
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Vice President Name
                        Text(
                            text = electionPair.vice_president.full_name,
                            style = AppTypography.heading6SemiBold,
                            color = MaterialTheme.colorScheme.onTertiary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Vice President View Profile Button
                        OutlinedButton(
                            onClick = onViewVicePresidentProfile,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.height(24.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = strings.viewProfile,
                                style = AppTypography.smallParagraphRegular,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.right2),
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 3: Proposing Parties Section (spans both columns)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = NeutralColors.Neutral10
                        )
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.proposingParties,
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral50
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Supporting parties logos
                    SupportingPartiesRow(
                        supportingParties = electionPair.supporting_parties ?: emptyList(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vision & Mission Button at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onVisionMissionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainColors.Primary1,
                        contentColor = NeutralColors.Neutral10
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = strings.candidateVisionMission,
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral10
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.right2),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = NeutralColors.Neutral10
                    )
                }
            }
        }
    }
}

/**
 * Composable to display supporting parties in a horizontal row
 */
@Composable
fun SupportingPartiesRow(
    supportingParties: List<com.nocturna.votechain.data.model.SupportingParty>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (supportingParties.isEmpty()) {
        // Show placeholder when no parties available
        Box(
            modifier = modifier.height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Data partai tidak tersedia",
                style = AppTypography.smallParagraphRegular,
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(supportingParties ?: emptyList()) { party ->
                val partyPhotoUrl = PartyPhotoHelper.getPartyPhotoUrl(party.party.id)

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(partyPhotoUrl)
                        .crossfade(true)
                        .error(R.drawable.ic_launcher_background)
                        .placeholder(R.drawable.ic_launcher_background)
                        .listener(
                            onStart = {
                                Log.d("PartyLogo", "🔄 Loading party logo: ${party.party.name}")
                                Log.d("PartyLogo", "📍 URL: $partyPhotoUrl")
                            },
                            onSuccess = { _, _ ->
                                Log.d("PartyLogo", "✅ Party logo loaded: ${party.party.name}")
                            },
                            onError = { _, error ->
                                Log.e("PartyLogo", "❌ Party logo failed: ${party.party.name}")
                                Log.e("PartyLogo", "📍 URL: $partyPhotoUrl")
                                Log.e("PartyLogo", "🔥 Error: ${error.throwable?.message}")
                            }
                        )
                        .build(),
                    imageLoader = CoilAuthHelper.getImageLoader(context),
                    contentDescription = "Logo ${party.party.name}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

/**
 * Individual party logo item
 */
@Composable
private fun PartyLogoItem(
    party: com.nocturna.votechain.data.model.Party,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoUrl = PartyPhotoHelper.getPartyPhotoUrl(party.id)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(width = 32.dp, height = 42.dp)
            .padding(4.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "${party.name} Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),

            placeholder = painterResource(id = R.drawable.ic_launcher_background), // Replace with party placeholder
            error = painterResource(id = R.drawable.ic_launcher_background) // Replace with party placeholder
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}


@Preview(showBackground = true)
@Composable
fun CandidatePresidentScreenPreview() {
    VotechainTheme {
        val previewNavController = rememberNavController()

        CandidatePresidentScreen(
            navController = previewNavController
        )
    }
}