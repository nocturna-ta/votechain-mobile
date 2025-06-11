package com.nocturna.votechain.ui.screens.homepage

import android.util.Log
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

@Composable
fun CandidatePresidentScreen(
    onBackClick: () -> Unit = {},
    onViewProfileClick: (String) -> Unit = {},
    navController: NavController? = null,
    viewModel: ElectionViewModel = viewModel(factory = ElectionViewModel.Factory())
) {
    val strings = LanguageManager.getLocalizedStrings()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val electionPairs by viewModel.electionPairs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedFilter by remember { mutableStateOf(strings.allCandidates) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val currentRoute = navController?.currentBackStackEntry?.destination?.route
    val voteId = if (currentRoute?.contains("candidate_president") == true) {
        navController.currentBackStackEntry?.arguments?.getString("voteId") ?: ""
    } else {
        ""
    }

    // Fetch election pairs when screen first loads
    LaunchedEffect(Unit) {
        viewModel.fetchElectionPairs()
    }

    // Define the onVisionMissionClick function that uses the NavController
    val onVisionMissionClick = { candidateNumber: Int ->
        navController?.navigate("vision_mission/$candidateNumber/$voteId")
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MainColors.Primary1)
            }
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
                    Text(
                        text = error ?: "Unknown error",
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral70
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchElectionPairs() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainColors.Primary1
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
        // Show candidates when data is loaded
        else {
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
                        onVisionMissionClick = { onVisionMissionClick(pair.election_no.toIntOrNull() ?: 1) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Add bottom padding to ensure the last item is fully visible
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun CandidateCardFromApi(
    electionPair: com.nocturna.votechain.data.model.ElectionPair,
    onViewPresidentProfile: () -> Unit,
    onViewVicePresidentProfile: () -> Unit,
    onVisionMissionClick: () -> Unit
) {
    val strings = LanguageManager.getLocalizedStrings()
    val context = LocalContext.current

    // Generate photo URLs untuk debugging
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

            Spacer(modifier = Modifier.height(14.dp))

            // Headers with dividers
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 0.5.dp,
                            color = NeutralColors.Neutral30
                        )
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.presidentialCandidate,
                        style = AppTypography.paragraphRegular,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.vicePresidentialCandidate,
                        style = AppTypography.paragraphRegular,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Candidate Photos and Info with vertical divider
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Presidential Candidate
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 0.5.dp,
                            color = NeutralColors.Neutral30
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        // Load candidate image from API if available
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(presidentPhotoUrl)
                                .crossfade(true)
                                .error(R.drawable.ic_launcher_background)
                                .placeholder(R.drawable.ic_launcher_background)
                                .listener(
                                    onStart = {
                                        Log.d("CandidateCardFromApi", "Loading president photo: $presidentPhotoUrl")
                                    },
                                    onSuccess = { _, _ ->
                                        Log.d("CandidateCardFromApi", "President photo loaded successfully")
                                    },
                                    onError = { _, error ->
                                        Log.e("CandidateCardFromApi", "President photo loading failed", error.throwable)
                                    }
                                )
                                .build(),
                            contentDescription = "Presidential Candidate ${electionPair.president.full_name}",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = electionPair.president.full_name,
                            style = AppTypography.heading6SemiBold,
                            color = MaterialTheme.colorScheme.onTertiary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .height(24.dp)
                                .clickable(onClick = onViewPresidentProfile)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text(
                                    text = strings.viewProfile,
                                    style = AppTypography.smallParagraphRegular,
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.right2),
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            }
                        }
                    }
                }

                // Vice Presidential Candidate
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 0.5.dp,
                            color = NeutralColors.Neutral30
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        // Load candidate image from API if available
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(CandidatePhotoHelper.getVicePresidentPhotoUrl(electionPair.id))
                                .crossfade(true)
                                .error(R.drawable.ic_launcher_background)
                                .build(),
                            contentDescription = "Vice Presidential Candidate ${electionPair.vice_president.full_name}",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = electionPair.vice_president.full_name,
                            style = AppTypography.heading6SemiBold,
                            color = MaterialTheme.colorScheme.onTertiary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .height(24.dp)
                                .clickable(onClick = onViewVicePresidentProfile)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text(
                                    text = strings.viewProfile,
                                    style = AppTypography.smallParagraphRegular,
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.right2),
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Proposing Parties", // You can add this to strings
                style = AppTypography.paragraphRegular,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Supporting parties logos
            SupportingPartiesRow(
                supportingParties = electionPair.supporting_parties ?: emptyList(),
                modifier = Modifier.fillMaxWidth()
            )

            // Vision & Mission Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onVisionMissionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainColors.Primary1,
                        contentColor = NeutralColors.Neutral10
                    ),
                    modifier = Modifier
                        .height(34.dp)
                ) {
                    Text(
                        text = strings.visionMission,
                        style = AppTypography.smallParagraphRegular,
                        color = NeutralColors.Neutral10
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.right2),
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
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
private fun SupportingPartiesRow(
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
            modifier = modifier,
            horizontalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(supportingParties) { supportingParty ->
                PartyLogoItem(
                    party = supportingParty.party,
                    modifier = Modifier.padding(horizontal = 4.dp)
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