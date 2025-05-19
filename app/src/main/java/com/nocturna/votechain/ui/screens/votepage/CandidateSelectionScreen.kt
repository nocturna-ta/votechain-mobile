package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.viewmodel.vote.VotingViewModel

data class CandidatePair(
    val number: Int,
    val presidentName: String,
    val presidentImage: Int,
    val vicePresidentName: String,
    val vicePresidentImage: Int,
    val partyLogos: List<Int>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidateSelectionScreen(
    navController: NavController,
    categoryId: String,
    viewModel: VotingViewModel
) {
    var selectedCandidateNumber by remember { mutableStateOf<Int?>(null) }
    val scrollState = rememberScrollState()

    // Mock candidates data (in a real app, this would come from the ViewModel)
    val candidates = listOf(
        CandidatePair(
            number = 1,
            presidentName = "H. Anies Rasyid Baswedan, Ph. D.",
            presidentImage = R.drawable.pc_anies,
            vicePresidentName = "DR. (H.C.) H. A. Muhaimin Iskandar",
            vicePresidentImage = R.drawable.pc_imin,
            partyLogos = listOf(R.drawable.pp_pkb, R.drawable.pp_pks, R.drawable.pp_nasdem)
        ),
        CandidatePair(
            number = 2,
            presidentName = "H. Prabowo Subianto",
            presidentImage = R.drawable.pc_prabowo,
            vicePresidentName = "Gibran Rakabuming Raka",
            vicePresidentImage = R.drawable.pc_gibran,
            partyLogos = listOf(
                R.drawable.pp_pan,
                R.drawable.pp_golkar,
                R.drawable.pp_demokrat,
                R.drawable.pp_psi,
                R.drawable.pp_pbb,
                R.drawable.pp_nasdem
            )
        ),
        CandidatePair(
            number = 3,
            presidentName = "H. Ganjar Pranowo, S.H., M.I.P.",
            presidentImage = R.drawable.pc_ganjar,
            vicePresidentName = "Prof. Dr. H. M. Mahmud MD",
            vicePresidentImage = R.drawable.pc_mahfud,
            partyLogos = listOf(
                R.drawable.pp_pdip,
                R.drawable.pp_ppp,
                R.drawable.pp_hanura,
                R.drawable.pp_perindo
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Candidate Selection",
                        style = AppTypography.heading4Regular,
                        color = PrimaryColors.Primary80
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back",
                            tint = MainColors.Primary1
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp) // Space for the bottom button
        ) {
            // Candidate cards
            candidates.forEach { candidate ->
                CandidateCard(
                    candidate = candidate,
                    isSelected = selectedCandidateNumber == candidate.number,
                    onSelect = { selectedCandidateNumber = candidate.number }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Submit button at the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = {
                    // Submit the vote and navigate back
                    selectedCandidateNumber?.let { candidateNumber ->
                        viewModel.submitVote(categoryId, "candidate_$candidateNumber")
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColors.Primary1,
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

@Composable
fun CandidateCard(
    candidate: CandidatePair,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MainColors.Primary1 else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Candidate number
            Text(
                text = "Candidate",
                style = AppTypography.heading6Medium,
                color = PrimaryColors.Primary60
            )
            Text(
                text = candidate.number.toString(),
                style = AppTypography.heading5Bold,
                color = PrimaryColors.Primary60
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Candidate photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Red)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // President
                    Box(modifier = Modifier.weight(1f)) {
                        Image(
                            painter = painterResource(id = candidate.presidentImage),
                            contentDescription = "Presidential Candidate",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Vice President
                    Box(modifier = Modifier.weight(1f)) {
                        Image(
                            painter = painterResource(id = candidate.vicePresidentImage),
                            contentDescription = "Vice Presidential Candidate",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Candidate titles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Presidential Candidate",
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral50,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Vice Presidential Candidate",
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral50,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Candidate names
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = candidate.presidentName,
                    style = AppTypography.paragraphSemiBold,
                    color = NeutralColors.Neutral80,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = candidate.vicePresidentName,
                    style = AppTypography.paragraphSemiBold,
                    color = NeutralColors.Neutral80,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Proposing parties
            Text(
                text = "Proposing Parties",
                style = AppTypography.paragraphRegular,
                color = NeutralColors.Neutral50
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Party logos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                candidate.partyLogos.forEach { logo ->
                    Image(
                        painter = painterResource(id = logo),
                        contentDescription = "Party Logo",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}