package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.*
import com.nocturna.votechain.viewmodel.DetailedResultViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class CandidateResult(
    val name: String,
    val percentage: Float,
    val votes: Int,
    val color: Color
)

data class RegionFilter(
    val id: String,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedResultScreen(
    navController: NavController,
    electionTitle: String = "2024 Presidential Election - Indonesia"
) {
    var selectedRegion by remember { mutableStateOf("all_regions") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Sample data - replace with actual data from ViewModel
    val regions = listOf(
        RegionFilter("all_regions", "All Regions"),
        RegionFilter("jakarta", "Jakarta"),
        RegionFilter("west_java", "West Java"),
        RegionFilter("east_java", "East Java"),
        RegionFilter("central_java", "Central Java"),
        RegionFilter("bali", "Bali")
    )

    val candidateResults = listOf(
        CandidateResult("Candidate 1", 0.57f, 12500000, Color(0xFF5B9C96)),
        CandidateResult("Candidate 2", 0.27f, 5900000, Color(0xFF4A7A74)),
        CandidateResult("Candidate 3", 0.21f, 4600000, Color(0xFF2D4B45))
    )

    val totalVotes = candidateResults.sumOf { it.votes }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = electionTitle,
                        style = AppTypography.heading4SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Region Dropdown
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = regions.find { it.id == selectedRegion }?.name ?: "All Regions",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown Arrow",
                            tint = NeutralColors.Neutral50
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            NeutralColors.Neutral20,
                            RoundedCornerShape(8.dp)
                        )
                )

                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    regions.forEach { region ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = region.name,
                                    style = AppTypography.paragraphRegular
                                )
                            },
                            onClick = {
                                selectedRegion = region.id
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Instruction Text
            Text(
                text = "Tap on any section of the pie chart below to view detailed information",
                style = AppTypography.paragraphRegular,
                color = NeutralColors.Neutral50,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Pie Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                PieChart(
                    data = candidateResults,
                    modifier = Modifier.size(250.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Legend
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                candidateResults.forEach { candidate ->
                    LegendItem(
                        candidate = candidate,
                        totalVotes = totalVotes,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun PieChart(
    data: List<CandidateResult>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = minOf(canvasWidth, canvasHeight) / 2.5f
        val center = androidx.compose.ui.geometry.Offset(canvasWidth / 2, canvasHeight / 2)

        var startAngle = -90f

        data.forEach { candidate ->
            val sweepAngle = candidate.percentage * 360f

            drawArc(
                color = candidate.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )

            // Draw percentage text
            val textAngle = startAngle + sweepAngle / 2
            val textRadius = radius * 0.7f
            val textX = center.x + textRadius * cos(Math.toRadians(textAngle.toDouble())).toFloat()
            val textY = center.y + textRadius * sin(Math.toRadians(textAngle.toDouble())).toFloat()

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 48f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                drawText(
                    "${(candidate.percentage * 100).toInt()}%",
                    textX,
                    textY + paint.textSize / 3,
                    paint
                )
            }

            startAngle += sweepAngle
        }
    }
}

@Composable
fun LegendItem(
    candidate: CandidateResult,
    totalVotes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(candidate.color, CircleShape)
        )

        // Candidate name
        Text(
            text = candidate.name,
            style = AppTypography.paragraphMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}