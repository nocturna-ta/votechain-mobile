package com.nocturna.votechain.ui.screens.homepage

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.ui.theme.VotechainTheme
import com.nocturna.votechain.utils.LanguageManager

@Composable
fun CandidatePresidentScreen(
    onBackClick: () -> Unit = {},
    onViewProfileClick: (String) -> Unit = {},
    navController: NavController? = null
) {
    val strings = LanguageManager.getLocalizedStrings()
    val scrollState = rememberScrollState()
    var selectedFilter by remember { mutableStateOf(strings.allCandidates) }
    var expandedDropdown by remember { mutableStateOf(false) }
    val dropdownOptions = listOf(
        strings.allCandidates,
        "Candidate 1",
        "Candidate 2",
        "Candidate 3"
    )
    // Get the voteId from the current route
    val currentRoute = navController?.currentBackStackEntry?.destination?.route
    val voteId = if (currentRoute?.contains("candidate_president") == true) {
        navController.currentBackStackEntry?.arguments?.getString("voteId") ?: ""
    } else {
        ""
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

        // Filter dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
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
            // Display candidate cards based on filter selection
            if (selectedFilter == "All Candidates" || selectedFilter == "Candidate 1") {
                // Candidate 1
                CandidateCard(
                    number = 1,
                    president = "Anies",
                    vicePresident = "Imin",
                    presidentImageRes = R.drawable.pc_anies,
                    vicePresidentImageRes = R.drawable.pc_imin,
                    parties = listOf(
                        R.drawable.pp_pks,
                        R.drawable.pp_pkb,
                        R.drawable.pp_nasdem
                    ),
                    onViewPresidentProfile = { onViewProfileClick("anies") },
                    onViewVicePresidentProfile = { onViewProfileClick("imin") },
                    onVisionMissionClick = { onVisionMissionClick(1) }
                )

                if (selectedFilter == "All Candidates") {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            if (selectedFilter == "All Candidates" || selectedFilter == "Candidate 2") {
                // Candidate 2
                CandidateCard(
                    number = 2,
                    president = "Prabowo",
                    vicePresident = "Gibran",
                    presidentImageRes = R.drawable.pc_prabowo,
                    vicePresidentImageRes = R.drawable.pc_gibran,
                    parties = listOf(
                        R.drawable.pp_pan,
                        R.drawable.pp_golkar,
                        R.drawable.pp_pdip,
                        R.drawable.pp_demokrat,
                        R.drawable.pp_nasdem,
                        R.drawable.pp_pbb,
                        R.drawable.pp_psi
                    ),
                    onViewPresidentProfile = { onViewProfileClick("prabowo") },
                    onViewVicePresidentProfile = { onViewProfileClick("gibran") },
                    onVisionMissionClick = { onVisionMissionClick(2) }
                )

                if (selectedFilter == "All Candidates") {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            if (selectedFilter == "All Candidates" || selectedFilter == "Candidate 3") {
                // Candidate 3
                CandidateCard(
                    number = 3,
                    president = "Ganjar",
                    vicePresident = "Mahfud MD",
                    presidentImageRes = R.drawable.pc_ganjar,
                    vicePresidentImageRes = R.drawable.pc_mahfud,
                    parties = listOf(
                        R.drawable.pp_pdip,
                        R.drawable.pp_ppp,
                        R.drawable.pp_hanura,
                        R.drawable.pp_perindo
                    ),
                    onViewPresidentProfile = { onViewProfileClick("ganjar") },
                    onViewVicePresidentProfile = { onViewProfileClick("mahfud MD") },
                    onVisionMissionClick = { onVisionMissionClick(3) }
                )
            }

            // Add bottom padding to ensure the last item is fully visible
            Spacer(modifier = Modifier.height(80.dp))
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