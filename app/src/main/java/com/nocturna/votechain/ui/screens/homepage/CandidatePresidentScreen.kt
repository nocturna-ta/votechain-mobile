package com.nocturna.votechain.ui.screens.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.ui.theme.VotechainTheme
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
//    val dropdownOptions = listOf(
//        strings.allCandidates,
//        "Candidate 1",
//        "Candidate 2",
//        "Candidate 3"
//    )

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
                    tint = MainColors.Primary1,
                    modifier = Modifier.size(20.dp) // Smaller icon size
                )
            }

            // Centered title
            Text(
                text = strings.candidatePresident,
                style = AppTypography.heading4Regular,
                color = PrimaryColors.Primary80,
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
                            focusedBorderColor = NeutralColors.Neutral30,
                            unfocusedBorderColor = NeutralColors.Neutral30,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = NeutralColors.Neutral40,
                            unfocusedTextColor = NeutralColors.Neutral40
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
                        .background(Color.White)
                ) {
                    dropdownOptions.forEach { option ->
                        DropdownMenuItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (option == selectedFilter)
                                        MainColors.Primary1
                                    else
                                        Color.White
                                )
                                .padding(vertical = 4.dp),
                            text = {
                                Text(
                                    text = option,
                                    style = AppTypography.heading5Regular,
                                    color = if (option == selectedFilter)
                                        NeutralColors.Neutral10  // White text for selected item
                                    else
                                        NeutralColors.Neutral70  // Dark gray for non-selected
                                )
                            },
                            onClick = {
                                selectedFilter = option
                                expandedDropdown = false
                                // Handle filter selection logic here
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
                        onViewPresidentProfile = { onViewProfileClick("president_${pair.id}") },
                        onViewVicePresidentProfile = { onViewProfileClick("vice_president_${pair.id}") },
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


//        // Filter dropdown
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 24.dp, vertical = 24.dp)
//        ) {
//            // Make the entire field clickable to toggle dropdown
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { expandedDropdown = !expandedDropdown }
//            ) {
//                OutlinedTextField(
//                    value = selectedFilter,
//                    onValueChange = {},
//                    readOnly = true,
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(8.dp),
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedBorderColor = NeutralColors.Neutral30,
//                        unfocusedBorderColor = NeutralColors.Neutral30,
//                        focusedContainerColor = Color.White,
//                        unfocusedContainerColor = Color.White,
//                        focusedTextColor = NeutralColors.Neutral40,
//                        unfocusedTextColor = NeutralColors.Neutral40
//                    ),
//                    textStyle = AppTypography.heading5Regular,
//                    trailingIcon = {
//                        Icon(
//                            painter = painterResource(id = R.drawable.down2),
//                            contentDescription = "Filter",
//                            tint = if (expandedDropdown) MainColors.Primary1 else NeutralColors.Neutral40,
//                            modifier = Modifier.clickable { expandedDropdown = !expandedDropdown }
//                        )
//                    }
//                )
//            }
//
//            DropdownMenu(
//                expanded = expandedDropdown,
//                onDismissRequest = { expandedDropdown = false },
//                modifier = Modifier
//                    .fillMaxWidth(0.88f)
//                    .background(Color.White)
//            ) {
//                dropdownOptions.forEach { option ->
//                    DropdownMenuItem(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(
//                                if (option == selectedFilter)
//                                    MainColors.Primary1
//                                else
//                                    Color.White
//                            )
//                            .padding(vertical = 4.dp),
//                        text = {
//                            Text(
//                                text = option,
//                                style = AppTypography.heading5Regular,
//                                color = if (option == selectedFilter)
//                                    NeutralColors.Neutral10  // White text for selected item
//                                else
//                                    NeutralColors.Neutral70  // Dark gray for non-selected
//                            )
//                        },
//                        onClick = {
//                            selectedFilter = option
//                            expandedDropdown = false
//                            // Handle filter selection logic here
//                        }
//                    )
//                }
//            }
//        }
//
//        // Candidates list
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f)
//                .verticalScroll(scrollState)
//                .padding(horizontal = 24.dp)
//        ) {
//            // Display candidate cards based on filter selection
//            if (selectedFilter == "All Candidates" || selectedFilter == "Candidate 1") {
//                // Candidate 1
//                CandidateCard(
//                    number = 1,
//                    president = "Anies",
//                    vicePresident = "Imin",
//                    presidentImageRes = R.drawable.pc_anies,
//                    vicePresidentImageRes = R.drawable.pc_imin,
//                    parties = listOf(
//                        R.drawable.pp_pks,
//                        R.drawable.pp_pkb,
//                        R.drawable.pp_nasdem
//                    ),
//                    onViewPresidentProfile = { onViewProfileClick("anies") },
//                    onViewVicePresidentProfile = { onViewProfileClick("imin") },
//                    onVisionMissionClick = { onVisionMissionClick(1) }
//                )
//
//                if (selectedFilter == "All Candidates") {
//                    Spacer(modifier = Modifier.height(14.dp))
//                }
//            }
//
//            if (selectedFilter == "All Candidates" || selectedFilter == "Candidate 2") {
//                // Candidate 2
//                CandidateCard(
//                    number = 2,
//                    president = "Prabowo",
//                    vicePresident = "Gibran",
//                    presidentImageRes = R.drawable.pc_prabowo,
//                    vicePresidentImageRes = R.drawable.pc_gibran,
//                    parties = listOf(
//                        R.drawable.pp_pan,
//                        R.drawable.pp_golkar,
//                        R.drawable.pp_pdip,
//                        R.drawable.pp_demokrat,
//                        R.drawable.pp_nasdem,
//                        R.drawable.pp_pbb,
//                        R.drawable.pp_psi
//                    ),
//                    onViewPresidentProfile = { onViewProfileClick("prabowo") },
//                    onViewVicePresidentProfile = { onViewProfileClick("gibran") },
//                    onVisionMissionClick = { onVisionMissionClick(2) }
//                )
//
//                if (selectedFilter == "All Candidates") {
//                    Spacer(modifier = Modifier.height(14.dp))
//                }
//            }
//
//            if (selectedFilter == "All Candidates" || selectedFilter == "Candidate 3") {
//                // Candidate 3
//                CandidateCard(
//                    number = 3,
//                    president = "Ganjar",
//                    vicePresident = "Mahfud MD",
//                    presidentImageRes = R.drawable.pc_ganjar,
//                    vicePresidentImageRes = R.drawable.pc_mahfud,
//                    parties = listOf(
//                        R.drawable.pp_pdip,
//                        R.drawable.pp_ppp,
//                        R.drawable.pp_hanura,
//                        R.drawable.pp_perindo
//                    ),
//                    onViewPresidentProfile = { onViewProfileClick("ganjar") },
//                    onViewVicePresidentProfile = { onViewProfileClick("mahfud MD") },
//                    onVisionMissionClick = { onVisionMissionClick(3) }
//                )
//            }
//
//            // Add bottom padding to ensure the last item is fully visible
//            Spacer(modifier = Modifier.height(80.dp))
//        }
//    }
//}

@Composable
fun CandidateCardFromApi(
    electionPair: com.nocturna.votechain.data.model.ElectionPair,
    onViewPresidentProfile: () -> Unit,
    onViewVicePresidentProfile: () -> Unit,
    onVisionMissionClick: () -> Unit
) {
    val strings = LanguageManager.getLocalizedStrings()
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
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
                color = PrimaryColors.Primary60
            )
            Text(
                text = electionPair.election_no,
                style = AppTypography.heading5Bold,
                color = PrimaryColors.Primary60
            )

            Spacer(modifier = Modifier.height(4.dp))

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
                        color = NeutralColors.Neutral50
                    )
                }

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
                        text = strings.vicePresidentialCandidate,
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral50
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
                                .data(electionPair.president.photo_path)
                                .crossfade(true)
                                .error(R.drawable.pc_anies) // Placeholder if image fails to load
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
                            color = PrimaryColors.Primary70
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = NeutralColors.Neutral30,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .height(24.dp) // Fixed compact height
                                .clickable(onClick = onViewPresidentProfile)
                                .padding(horizontal = 10.dp, vertical = 3.dp) // Minimal padding
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text(
                                    text = strings.viewProfile,
                                    style = AppTypography.smallParagraphRegular,
                                    color = NeutralColors.Neutral60
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.right2),
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = NeutralColors.Neutral60
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
                                .data(electionPair.vice_president.photo_path)
                                .crossfade(true)
                                .error(R.drawable.pc_imin) // Placeholder if image fails to load
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
                            color = PrimaryColors.Primary70
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = NeutralColors.Neutral30,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .height(24.dp) // Fixed compact height
                                .clickable(onClick = onViewVicePresidentProfile)
                                .padding(horizontal = 10.dp, vertical = 3.dp) // Minimal padding
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text(
                                    text = strings.viewProfile,
                                    style = AppTypography.smallParagraphRegular,
                                    color = NeutralColors.Neutral60
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.right2),
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = NeutralColors.Neutral60
                                )
                            }
                        }
                    }
                }
            }

            // Party logos section would go here, but since we don't have them in the API response,
            // we can either hide this section or use placeholder logos

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