//package com.nocturna.votechain.ui.screens.votepage
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.nativeCanvas
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.nocturna.votechain.R
//import com.nocturna.votechain.ui.theme.*
//import com.nocturna.votechain.viewmodel.DetailedResultViewModel
//import kotlin.math.PI
//import kotlin.math.cos
//import kotlin.math.sin
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DetailedResultScreen(
//    navController: NavController,
//    categoryId: String,
//    categoryTitle: String
//) {
//    // Get ViewModel instance
//    val viewModel: DetailedResultViewModel = viewModel(factory = DetailedResultViewModel.Factory())
//
//    // Collect states from ViewModel
//    val detailedResult by viewModel.detailedResult.collectAsState()
//    val availableRegions by viewModel.availableRegions.collectAsState()
//    val selectedRegion by viewModel.selectedRegion.collectAsState()
//    val selectedSlice by viewModel.selectedSlice.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
//    val error by viewModel.error.collectAsState()
//
//    // Dropdown state
//    var showRegionDropdown by remember { mutableStateOf(false) }
//
//    // Initialize ViewModel on first composition
//    LaunchedEffect(key1 = categoryId) {
//        viewModel.initialize(categoryId)
//    }
//
//    // Define colors for candidates
//    val candidateColors = listOf(
//        Color(0xFF5AA99E), // Teal - Candidate 1
//        Color(0xFF37615B), // Dark Teal - Candidate 2
//        Color(0xFF18312F)  // Very Dark Teal/Almost Black - Candidate 3
//    )
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = categoryTitle,
//                        style = AppTypography.heading4Regular,
//                        color = PrimaryColors.Primary80
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.back),
//                            contentDescription = "Back",
//                            tint = MainColors.Primary1
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color.White,
//                )
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            // Handle loading state
//            if (isLoading) {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center),
//                    color = MainColors.Primary1
//                )
//            }
//            // Handle error state
//            else if (error != null) {
//                Column(
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(
//                        text = "Error Loading Results",
//                        style = AppTypography.heading4Medium,
//                        color = MaterialTheme.colorScheme.error
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = error ?: "Unknown error occurred",
//                        style = AppTypography.paragraphRegular,
//                        color = MaterialTheme.colorScheme.error,
//                        textAlign = TextAlign.Center
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Button(
//                        onClick = { viewModel.initialize(categoryId) },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = MainColors.Primary1
//                        )
//                    ) {
//                        Text("Retry")
//                    }
//                }
//            }
//            // Main content when data is loaded
//            else if (detailedResult != null) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(horizontal = 16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    // Region Selector Dropdown
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 16.dp)
//                    ) {
//                        OutlinedTextField(
//                            value = selectedRegion?.name ?: "All Regions",
//                            onValueChange = { },
//                            readOnly = true,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable { showRegionDropdown = true },
//                            trailingIcon = {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.down2),
//                                    contentDescription = "Select Region",
//                                    tint = NeutralColors.Neutral40
//                                )
//                            },
//                            shape = RoundedCornerShape(8.dp),
//                            colors = TextFieldDefaults.outlinedTextFieldColors(
//                                focusedBorderColor = NeutralColors.Neutral30,
//                                unfocusedBorderColor = NeutralColors.Neutral30,
//                                containerColor = Color.White
//                            ),
//                            textStyle = AppTypography.heading5Regular
//                        )
//
//                        DropdownMenu(
//                            expanded = showRegionDropdown,
//                            onDismissRequest = { showRegionDropdown = false },
//                            modifier = Modifier
//                                .fillMaxWidth(0.9f)
//                                .background(Color.White)
//                        ) {
//                            availableRegions.forEach { region ->
//                                DropdownMenuItem(
//                                    text = { Text(region.name) },
//                                    onClick = {
//                                        viewModel.selectRegion(region, categoryId)
//                                        showRegionDropdown = false
//                                    }
//                                )
//                            }
//                        }
//                    }
//
//                    // Instruction Text
//                    Text(
//                        text = "Tap on any section of the pie chart below to view detailed information",
//                        style = AppTypography.paragraphRegular,
//                        color = NeutralColors.Neutral60,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier.padding(bottom = 24.dp)
//                    )
//
//                    // Pie Chart
//                    Box(
//                        modifier = Modifier
//                            .size(300.dp)
//                            .padding(8.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        detailedResult?.let { result ->
//                            InteractivePieChart(
//                                data = result.options,
//                                colors = candidateColors,
//                                selectedSlice = selectedSlice,
//                                onSliceSelected = { viewModel.selectSlice(it) }
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(24.dp))
//
//                    // Legend
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        detailedResult?.options?.forEachIndexed { index, option ->
//                            LegendItem(
//                                text = "Candidate ${index + 1}",
//                                color = candidateColors[index % candidateColors.size]
//                            )
//                            if (index < detailedResult?.options?.size?.minus(1) ?: 0) {
//                                Spacer(modifier = Modifier.width(16.dp))
//                            }
//                        }
//                    }
//
//                    // Show detailed info when a slice is selected
//                    selectedSlice?.let { selected ->
//                        detailedResult?.options?.getOrNull(selected)?.let { option ->
//                            CandidateDetailCard(
//                                option = option,
//                                candidateIndex = selected,
//                                color = candidateColors[selected % candidateColors.size]
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}