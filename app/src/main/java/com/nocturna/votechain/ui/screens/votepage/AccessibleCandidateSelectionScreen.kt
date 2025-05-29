//package com.nocturna.votechain.ui.screens.votepage
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.nocturna.votechain.R
//import com.nocturna.votechain.ui.components.AccessibleButton
//import com.nocturna.votechain.ui.components.AccessibleCandidateCard
//import com.nocturna.votechain.ui.components.ScreenAnnouncement
//import com.nocturna.votechain.ui.theme.AppTypography
//import com.nocturna.votechain.ui.theme.MainColors
//import com.nocturna.votechain.ui.theme.NeutralColors
//import com.nocturna.votechain.ui.theme.PrimaryColors
//import com.nocturna.votechain.utils.AccessibilityManager
//import com.nocturna.votechain.viewmodel.vote.VotingViewModel
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AccessibleCandidateSelectionScreen(
//    navController: NavController,
//    categoryId: String,
//    viewModel: VotingViewModel
//) {
//    val context = LocalContext.current
//    val accessibilityManager = remember { AccessibilityManager.getInstance(context) }
//    val isAccessibilityEnabled by accessibilityManager.isEnabled.collectAsState()
//
//    var selectedCandidateNumber by remember { mutableStateOf<Int?>(null) }
//    val scrollState = rememberScrollState()
//
//    // Screen announcement
//    ScreenAnnouncement(
//        screenName = "Pemilihan Kandidat",
//        announcement = "Halaman pemilihan kandidat. Pilih salah satu kandidat dengan mengetuk kartu kandidat. Tekan lama untuk mendengar informasi lengkap kandidat."
//    )
//
//    // Mock candidates data
//    val candidates = listOf(
//        CandidatePair(
//            number = 1,
//            presidentName = "H. Anies Rasyid Baswedan, Ph. D.",
//            presidentImage = R.drawable.pc_anies,
//            vicePresidentName = "DR. (H.C.) H. A. Muhaimin Iskandar",
//            vicePresidentImage = R.drawable.pc_imin,
//            partyLogos = listOf(R.drawable.pp_pkb, R.drawable.pp_pks, R.drawable.pp_nasdem)
//        ),
//        CandidatePair(
//            number = 2,
//            presidentName = "H. Prabowo Subianto",
//            presidentImage = R.drawable.pc_prabowo,
//            vicePresidentName = "Gibran Rakabuming Raka",
//            vicePresidentImage = R.drawable.pc_gibran,
//            partyLogos = listOf(
//                R.drawable.pp_pan,
//                R.drawable.pp_golkar,
//                R.drawable.pp_demokrat,
//                R.drawable.pp_psi,
//                R.drawable.pp_pbb,
//                R.drawable.pp_nasdem
//            )
//        ),
//        CandidatePair(
//            number = 3,
//            presidentName = "H. Ganjar Pranowo, S.H., M.I.P.",
//            presidentImage = R.drawable.pc_ganjar,
//            vicePresidentName = "Prof. Dr. H. M. Mahmud MD",
//            vicePresidentImage = R.drawable.pc_mahfud,
//            partyLogos = listOf(
//                R.drawable.pp_pdip,
//                R.drawable.pp_ppp,
//                R.drawable.pp_hanura,
//                R.drawable.pp_perindo
//            )
//        )
//    )
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Pemilihan Kandidat",
//                        style = AppTypography.heading4Regular,
//                        color = PrimaryColors.Primary80
//                    )
//                },
//                navigationIcon = {
//                    IconButton(
//                        onClick = {
//                            if (isAccessibilityEnabled) {
//                                accessibilityManager.speakAction("Kembali ke halaman sebelumnya")
//                            }
//                            navController.popBackStack()
//                        }
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.back),
//                            contentDescription = "Kembali",
//                            tint = MainColors.Primary1
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = androidx.compose.ui.graphics.Color.White
//                )
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .verticalScroll(scrollState)
//                .padding(bottom = 80.dp)
//        ) {
//            // Instructions for accessibility
//            if (isAccessibilityEnabled) {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = MainColors.Primary1.copy(alpha = 0.1f)
//                    )
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp)
//                    ) {
//                        Text(
//                            text = "Petunjuk Aksesibilitas:",
//                            style = AppTypography.heading6SemiBold,
//                            color = MainColors.Primary1
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            text = "• Ketuk sekali untuk memilih kandidat\n• Tekan lama untuk mendengar informasi lengkap\n• Kandidat yang dipilih akan diumumkan",
//                            style = AppTypography.paragraphRegular,
//                            color = NeutralColors.Neutral70
//                        )
//                    }
//                }
//            }
//
//            // Candidate cards
//            candidates.forEach { candidate ->
//                AccessibleCandidateCard(
//                    candidateNumber = candidate.number,
//                    presidentName = candidate.presidentName,
//                    vicePresidentName = candidate.vicePresidentName,
//                    isSelected = selectedCandidateNumber == candidate.number,
//                    onClick = {
//                        selectedCandidateNumber = if (selectedCandidateNumber == candidate.number) {
//                            null // Deselect if already selected
//                        } else {
//                            candidate.number
//                        }
//                    },
//                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
//                ) {
//                    // Original candidate card content here
//                    CandidateCardContent(candidate = candidate)
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//        }
//
//        // Submit button at the bottom
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            contentAlignment = Alignment.BottomCenter
//        ) {
//            AccessibleButton(
//                onClick = {
//                    selectedCandidateNumber?.let { candidateNumber ->
//                        val selectedCandidate = candidates.find { it.number == candidateNumber }
//                        if (isAccessibilityEnabled && selectedCandidate != null) {
//                            accessibilityManager.speakSuccess(
//                                "Suara Anda untuk kanditat nomor $candidateNumber, ${selectedCandidate.presidentName}, telah berhasil dikirim"
//                            )
//                        }
//                        viewModel.submitVote(categoryId, "candidate_$candidateNumber")
//                        navController.popBackStack()
//                    }
//                },
//                speechText = when {
//                    selectedCandidateNumber != null -> {
//                        val selectedCandidate = candidates.find { it.number == selectedCandidateNumber }
//                        "Kirim suara untuk kandidat nomor $selectedCandidateNumber, ${selectedCandidate?.presidentName}"
//                    }
//                    else -> "Pilih kandidat terlebih dahulu sebelum mengirim suara"
//                },
//                contentDescription = "Tombol kirim suara",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                enabled = selectedCandidateNumber != null,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MainColors.Primary1,
//                    disabledContainerColor = NeutralColors.Neutral30
//                )
//            ) {
//                Text(
//                    text = "Kirim Suara",
//                    style = AppTypography.heading4SemiBold,
//                    color = androidx.compose.ui.graphics.Color.White
//                )
//            }
//        }
//    }
//
//    // Announce selection changes
//    LaunchedEffect(selectedCandidateNumber) {
//        if (isAccessibilityEnabled && selectedCandidateNumber != null) {
//            val selectedCandidate = candidates.find { it.number == selectedCandidateNumber }
//            if (selectedCandidate != null) {
//                accessibilityManager.speakText(
//                    "Kandidat nomor ${selectedCandidate.number} dipilih. ${selectedCandidate.presidentName} dan ${selectedCandidate.vicePresidentName}"
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun CandidateCardContent(candidate: CandidatePair) {
//    // This would contain the original candidate card UI content
//    // Keeping it simple for this example
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Kandidat ${candidate.number}",
//            style = AppTypography.heading6Medium,
//            color = PrimaryColors.Primary60
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = candidate.presidentName,
//            style = AppTypography.heading6SemiBold,
//            color = PrimaryColors.Primary70
//        )
//
//        Text(
//            text = candidate.vicePresidentName,
//            style = AppTypography.paragraphRegular,
//            color = NeutralColors.Neutral70
//        )
//    }
//}