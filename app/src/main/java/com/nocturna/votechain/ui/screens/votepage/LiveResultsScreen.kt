package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.ElectionPair
import com.nocturna.votechain.data.model.LiveElectionData
import com.nocturna.votechain.data.model.LiveRegionResult
import com.nocturna.votechain.data.model.LiveCityResult
import com.nocturna.votechain.data.network.LiveResultsWebSocketManager
import com.nocturna.votechain.ui.components.ConnectionStatusIndicator
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.viewmodel.candidate.ElectionViewModel
import com.nocturna.votechain.viewmodel.vote.LiveResultViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// Data class untuk kandidat dengan persentase
data class CandidateWithPercentage(
    val electionPair: ElectionPair,
    val votes: Int,
    val percentage: Float,
    val color: Color
)

// Warna yang digunakan untuk pie chart
val PieChartColors = listOf(
    Color(0xFF3B82F6), // Blue
    Color(0xFFEF4444), // Red
    Color(0xFF10B981), // Green
    Color(0xFFF59E0B), // Yellow
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEC4899), // Pink
    Color(0xFF06B6D4), // Cyan
    Color(0xFFEAB308), // Amber
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveResultScreen(
    electionId: String,
    navController: NavController,
    electionViewModel: ElectionViewModel = viewModel(),
    liveResultViewModel: LiveResultViewModel = viewModel()
) {
    val electionPairs by electionViewModel.electionPairs.collectAsState()
    val connectionState by liveResultViewModel.connectionState.collectAsState()
    val liveResultData by liveResultViewModel.rawLiveData.collectAsState()
    val error by liveResultViewModel.error.collectAsState()

    // Get election pairs for this election
    val currentElectionPairs = electionPairs.filter { it.id == electionId }

    // Create candidates with percentages from live data
    val candidatesWithPercentage = remember(currentElectionPairs, liveResultData) {
        currentElectionPairs.mapIndexed { index, pair ->
            // Calculate votes and percentage for each candidate
            // This is a mock calculation - adjust based on your actual data structure
            val votes = when (index) {
                0 -> liveResultData?.regions?.sumOf { it.votes } ?: 0
                else -> (liveResultData?.totalVotes ?: 0) - (liveResultData?.regions?.sumOf { it.votes } ?: 0)
            }
            val percentage = if (liveResultData?.totalVotes != null && liveResultData!!.totalVotes > 0) {
                (votes.toFloat() / liveResultData!!.totalVotes) * 100f
            } else 0f

            CandidateWithPercentage(
                electionPair = pair,
                votes = votes,
                percentage = percentage,
                color = PieChartColors[index % PieChartColors.size]
            )
        }.sortedByDescending { it.votes }
    }

    // Animations
    val pulseAnimation by rememberInfiniteTransition().animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Start live connection
    LaunchedEffect(electionId) {
        liveResultViewModel.startLiveResults(electionId)
    }

    // Show error if any
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Handle error display
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Live Election Results",
                        style = AppTypography.heading4Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    ConnectionStatusIndicator(connectionState)
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Stats Card
            item {
                LiveStatsCard(
                    liveResultData = liveResultData,
                    pulseAnimation = pulseAnimation
                )
            }

            // Pie Chart Card
            item {
                if (candidatesWithPercentage.isNotEmpty() && liveResultData?.totalVotes != null && liveResultData!!.totalVotes > 0) {
                    ModernPieChartCard(
                        candidates = candidatesWithPercentage,
                        rotation = rotation
                    )
                }
            }

            // Candidate Details
            items(candidatesWithPercentage) { candidate ->
                CandidateResultCard(
                    candidate = candidate,
                    rank = candidatesWithPercentage.indexOf(candidate) + 1
                )
            }

            // Regional Breakdown
            if (liveResultData?.regions?.isNotEmpty() == true) {
                item {
                    RegionalBreakdownCard(regions = liveResultData!!.regions)
                }
            }

            // Top Cities
            if (liveResultData?.topCities?.isNotEmpty() == true) {
                item {
                    TopCitiesCard(cities = liveResultData!!.topCities)
                }
            }
        }
    }
}

@Composable
fun LiveStatsCard(
    liveResultData: LiveElectionData?,
    pulseAnimation: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Voting Statistics",
                    style = AppTypography.heading5Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LiveIndicator()
            }

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    title = "Total Votes",
                    value = NumberFormat.getNumberInstance(Locale.US)
                        .format(liveResultData?.totalVotes ?: 0),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    title = "Participation",
                    value = "${String.format("%.1f", (liveResultData?.overallPercentage ?: 0.0) * 100)}%",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    title = "Active Regions",
                    value = "${liveResultData?.stats?.activeRegions ?: 0}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ModernPieChartCard(
    candidates: List<CandidateWithPercentage>,
    rotation: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Vote Distribution",
                style = AppTypography.heading5Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pie Chart
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedPieChart(
                    candidates = candidates,
                    modifier = Modifier.fillMaxSize()
                )

                // Center info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Leading",
                        style = AppTypography.paragraphRegular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    candidates.firstOrNull()?.let { leader ->
                        Text(
                            text = "${leader.percentage.toInt()}%",
                            style = AppTypography.heading3Bold,
                            color = leader.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Legend
            candidates.forEach { candidate ->
                LegendItem(
                    candidate = candidate,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AnimatedPieChart(
    candidates: List<CandidateWithPercentage>,
    modifier: Modifier = Modifier
) {
    val animatedValues = candidates.map { candidate ->
        animateFloatAsState(
            targetValue = candidate.percentage,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = minOf(canvasWidth, canvasHeight) / 2f * 0.8f
        val center = center

        var currentAngle = -90f
        val strokeWidth = 40.dp.toPx()

        candidates.forEachIndexed { index, candidate ->
            val animatedValue = animatedValues[index].value
            val sweepAngle = (animatedValue / 100f) * 360f

            // Draw pie slice
            drawArc(
                color = candidate.color,
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                topLeft = androidx.compose.ui.geometry.Offset(
                    center.x - radius,
                    center.y - radius
                )
            )

            currentAngle += sweepAngle
        }
    }
}

@Composable
fun LegendItem(
    candidate: CandidateWithPercentage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(candidate.color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "${candidate.electionPair.president} & ${candidate.electionPair.vice_president}",
                    style = AppTypography.paragraphMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.US).format(candidate.votes)} votes",
                    style = AppTypography.paragraphRegular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "${candidate.percentage.toInt()}%",
            style = AppTypography.paragraphBold,
            color = candidate.color
        )
    }
}

@Composable
fun CandidateResultCard(
    candidate: CandidateWithPercentage,
    rank: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle candidate detail */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(candidate.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = AppTypography.paragraphBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${candidate.electionPair.president} & ${candidate.electionPair.vice_president}",
                    style = AppTypography.paragraphBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.US).format(candidate.votes)} votes",
                    style = AppTypography.paragraphRegular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${candidate.percentage.toInt()}%",
                style = AppTypography.heading6Bold,
                color = candidate.color
            )
        }
    }
}

@Composable
fun RegionalBreakdownCard(regions: List<LiveRegionResult>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Regional Breakdown",
                style = AppTypography.heading5Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            regions.forEach { region ->
                RegionItem(region = region)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun RegionItem(region: LiveRegionResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = region.region,
            style = AppTypography.paragraphMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.US).format(region.votes)} votes",
                style = AppTypography.paragraphRegular,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${String.format("%.1f", region.percentage * 100)}%",
                style = AppTypography.paragraphBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TopCitiesCard(cities: List<LiveCityResult>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Top Cities",
                style = AppTypography.heading5Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            cities.take(5).forEach { city ->
                CityItem(city = city)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun CityItem(city: LiveCityResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${city.rank}",
                    style = AppTypography.paragraphBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = city.city,
                    style = AppTypography.paragraphMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.US).format(city.votes)}/${NumberFormat.getNumberInstance(Locale.US).format(city.voters)} voters",
                    style = AppTypography.paragraphRegular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "${String.format("%.1f", city.percentage * 100)}%",
            style = AppTypography.paragraphBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun LiveIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444))
                .graphicsLayer { this.alpha = alpha }
        )
        Text(
            text = "LIVE",
            style = AppTypography.paragraphBold,
            color = Color(0xFFEF4444)
        )
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = AppTypography.paragraphRegular,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = AppTypography.heading6Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}