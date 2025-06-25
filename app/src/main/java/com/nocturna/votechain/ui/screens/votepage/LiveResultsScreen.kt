package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.ElectionPair
import com.nocturna.votechain.utils.CandidatePhotoHelper
import com.nocturna.votechain.data.network.LiveResultsWebSocketManager
import com.nocturna.votechain.ui.theme.*
import com.nocturna.votechain.viewmodel.candidate.ElectionViewModel
import com.nocturna.votechain.viewmodel.vote.LiveResultViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// Data class untuk WebSocket response
data class LiveResultData(
    val election_id: String,
    val total_votes: Int,
    val total_voters: Int,
    val last_updated: String,
    val overall_percentage: Double,
    val regions: List<RegionResult>,
    val top_cities: List<CityResult>,
    val stats: LiveStats,
    val candidates: List<CandidateVoteData> = emptyList() // Tambahan untuk kandidat data
)

data class RegionResult(
    val region: String,
    val votes: Int,
    val percentage: Int
)

data class CityResult(
    val city: String,
    val votes: Int,
    val voters: Int,
    val percentage: Int,
    val rank: Int
)

data class LiveStats(
    val total_voters: Int,
    val total_regions: Int,
    val success_rate: Double,
    val votes_per_second: Double,
    val active_regions: Int,
    val completion_rate: Double
)

data class LiveResultFilter(
    val election_pair_id: String
)

// Data class untuk kandidat dengan vote data dari WebSocket
data class CandidateVoteData(
    val candidateId: String,
    val candidateName: String,
    val votes: Int,
    val percentage: Float
)

// Data class untuk kandidat dengan persentase dan warna untuk UI
data class CandidateWithPercentage(
    val electionPair: ElectionPair,
    val votes: Int,
    val percentage: Float,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveResultScreen(
    electionId: String,
    navController: NavController,
    electionViewModel: ElectionViewModel = viewModel(),
    liveResultViewModel: LiveResultViewModel = viewModel()
) {
    val context = LocalContext.current
    val electionPairs by electionViewModel.electionPairs.collectAsState()
    val isLoadingPairs by electionViewModel.isLoading.collectAsState()
    val connectionState by liveResultViewModel.connectionState.collectAsState()

    var liveResultData by remember { mutableStateOf<LiveResultData?>(null) }
    var candidatesWithPercentage by remember { mutableStateOf<List<CandidateWithPercentage>>(emptyList()) }
    var isDataLoaded by remember { mutableStateOf(false) }

    // Animation states
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Fetch election pairs
    LaunchedEffect(Unit) {
        electionViewModel.fetchElectionPairs()
    }

    // WebSocket connection
    LaunchedEffect(electionId) {
        // Start WebSocket connection
        liveResultViewModel.startLiveResults(electionId)
    }

    // Handle WebSocket messages
    val rawLiveData by liveResultViewModel.rawLiveData.collectAsState()
    val liveResult by liveResultViewModel.liveResult.collectAsState()

    // Process raw WebSocket data
    LaunchedEffect(rawLiveData) {
        rawLiveData?.let { data ->
            try {
                // Convert LiveElectionData to LiveResultData dengan data real
                liveResultData = LiveResultData(
                    election_id = data.electionId,
                    total_votes = data.totalVotes,
                    total_voters = data.totalVoters,
                    last_updated = data.lastUpdated,
                    overall_percentage = data.overallPercentage,
                    regions = data.regions.map { region ->
                        RegionResult(
                            region = region.region,
                            votes = region.votes,
                            percentage = region.percentage
                        )
                    },
                    top_cities = data.topCities.map { city ->
                        CityResult(
                            city = city.city,
                            votes = city.votes,
                            voters = city.voters,
                            percentage = city.percentage,
                            rank = city.rank
                        )
                    },
                    stats = LiveStats(
                        total_voters = data.stats.totalVoters,
                        total_regions = data.stats.totalRegions,
                        success_rate = data.stats.successRate,
                        votes_per_second = data.stats.votesPerSecond,
                        active_regions = data.stats.activeRegions,
                        completion_rate = data.stats.completionRate
                    )
                )
                isDataLoaded = true
            } catch (e: Exception) {
                android.util.Log.e("LiveResultScreen", "Error processing raw live data", e)
            }
        }
    }

    // Update live data when received from WebSocket
    LaunchedEffect(liveResult) {
        liveResult?.let { result ->
            // Parse the WebSocket data
            try {
                val gson = Gson()
                // Convert VotingResult to LiveResultData format
                // This is a simplified conversion - adapt based on actual data structure
                liveResultData = LiveResultData(
                    election_id = result.categoryId,
                    total_votes = result.totalVotes,
                    total_voters = 319, // From sample data
                    last_updated = System.currentTimeMillis().toString(),
                    overall_percentage = 27.586206896551722, // From sample data
                    regions = listOf(), // Populate from actual data
                    top_cities = listOf(), // Populate from actual data
                    stats = LiveStats(
                        total_voters = 319,
                        total_regions = 10,
                        success_rate = 27.586206896551722,
                        votes_per_second = 0.001018457809608789,
                        active_regions = 10,
                        completion_rate = 27.586206896551722
                    )
                )
            } catch (e: Exception) {
                // Handle parsing error
            }
        }
    }

    // Calculate percentages for candidates
    LaunchedEffect(electionPairs, liveResultData) {
        if (electionPairs.isNotEmpty() && liveResultData != null) {
            val totalVotes = liveResultData!!.total_votes
            val colors = listOf(
                Color(0xFF6366F1), // Indigo
                Color(0xFF10B981), // Emerald
                Color(0xFFF59E0B), // Amber
                Color(0xFFEF4444), // Red
                Color(0xFF8B5CF6)  // Purple
            )

            candidatesWithPercentage = electionPairs.mapIndexed { index, pair ->
                // Simulate vote distribution - in real app, get from WebSocket data
                val votes = when (index) {
                    0 -> (totalVotes * 0.45).toInt()
                    1 -> (totalVotes * 0.35).toInt()
                    else -> (totalVotes * 0.20 / (electionPairs.size - 2)).toInt()
                }

                CandidateWithPercentage(
                    electionPair = pair,
                    votes = votes,
                    percentage = if (totalVotes > 0) votes.toFloat() / totalVotes else 0f,
                    color = colors[index % colors.size]
                )
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            liveResultViewModel.stopLiveResults()
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
                    // Connection status indicator
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
                AnimatedVisibility(
                    visible = candidatesWithPercentage.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(600)),
                    exit = fadeOut()
                ) {
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
            if (liveResultData?.top_cities?.isNotEmpty() == true) {
                item {
                    TopCitiesCard(cities = liveResultData!!.top_cities)
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusIndicator(connectionState: LiveResultsWebSocketManager.ConnectionState) {
    val color = when (connectionState) {
        LiveResultsWebSocketManager.ConnectionState.CONNECTED -> Color(0xFF10B981)
        LiveResultsWebSocketManager.ConnectionState.CONNECTING -> Color(0xFFF59E0B)
        LiveResultsWebSocketManager.ConnectionState.DISCONNECTED,
        LiveResultsWebSocketManager.ConnectionState.FAILED -> Color(0xFFEF4444)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = connectionState.name.lowercase().capitalize(),
            style = AppTypography.paragraphRegular,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LiveStatsCard(
    liveResultData: LiveResultData?,
    pulseAnimation: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseAnimation)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MainColors.Primary1.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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

                    // Live indicator
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
                            .format(liveResultData?.total_votes ?: 0),
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        title = "Participation",
                        value = "${String.format("%.1f", liveResultData?.overall_percentage ?: 0.0)}%",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        title = "Active Regions",
                        value = "${liveResultData?.stats?.active_regions ?: 0}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
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
                .alpha(alpha)
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

            // Animated Pie Chart
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedPieChart(
                    candidates = candidates,
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation)
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
                            text = "${(leader.percentage * 100).toInt()}%",
                            style = AppTypography.heading3Bold,
                            color = leader.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Legend
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                candidates.forEach { candidate ->
                    PieChartLegend(candidate = candidate)
                }
            }
        }
    }
}

@Composable
fun AnimatedPieChart(
    candidates: List<CandidateWithPercentage>,
    modifier: Modifier = Modifier
) {
    val animatedPercentages = candidates.map { candidate ->
        animateFloatAsState(
            targetValue = candidate.percentage,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2.5f
        val center = Offset(size.width / 2, size.height / 2)
        val strokeWidth = 40.dp.toPx()

        var startAngle = -90f

        candidates.forEachIndexed { index, candidate ->
            val sweepAngle = animatedPercentages[index].value * 360f

            // Draw arc with gradient effect
            drawArc(
                color = candidate.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Draw separator lines
            if (index < candidates.size - 1) {
                val endAngle = startAngle + sweepAngle
                val angleRad = Math.toRadians(endAngle.toDouble())
                val lineStart = Offset(
                    (center.x + (radius - strokeWidth / 2) * cos(angleRad)).toFloat(),
                    (center.y + (radius - strokeWidth / 2) * sin(angleRad)).toFloat()
                )
                val lineEnd = Offset(
                    (center.x + (radius + strokeWidth / 2) * cos(angleRad)).toFloat(),
                    (center.y + (radius + strokeWidth / 2) * sin(angleRad)).toFloat()
                )

                drawLine(
                    color = NeutralColors.Neutral50,
                    start = lineStart,
                    end = lineEnd,
                    strokeWidth = 3.dp.toPx()
                )
            }

            startAngle += sweepAngle
        }
    }
}

@Composable
fun PieChartLegend(candidate: CandidateWithPercentage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(candidate.color)
            )
            Text(
                text = "${candidate.electionPair.election_no}. ${candidate.electionPair.president.full_name}",
                style = AppTypography.smallParagraphMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Text(
            text = "${(candidate.percentage * 100).toInt()}%",
            style = AppTypography.smallParagraphBold,
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
            .animateContentSize()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700) // Gold
                            2 -> Color(0xFFC0C0C0) // Silver
                            3 -> Color(0xFFCD7F32) // Bronze
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    style = AppTypography.heading6Bold,
                    color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Candidate info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Candidate ${candidate.electionPair.election_no}",
                    style = AppTypography.paragraphRegular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = candidate.electionPair.president.full_name,
                    style = AppTypography.paragraphMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.US).format(candidate.votes)} votes",
                    style = AppTypography.smallParagraphRegular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Percentage with animated progress
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${(candidate.percentage * 100).toInt()}%",
                    style = AppTypography.heading5Bold,
                    color = candidate.color
                )

                // Mini progress bar
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(candidate.percentage)
                            .background(candidate.color)
                            .animateContentSize()
                    )
                }
            }
        }
    }
}

@Composable
fun RegionalBreakdownCard(regions: List<RegionResult>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Regional Breakdown",
                style = AppTypography.heading6Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            regions.take(5).forEach { region ->
                RegionItem(region = region)
            }
        }
    }
}

@Composable
fun RegionItem(region: RegionResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = region.region,
            style = AppTypography.smallParagraphMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${region.votes} votes",
                style = AppTypography.paragraphRegular,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (region.percentage > 50) Color(0xFF10B981)
                        else if (region.percentage > 0) Color(0xFFF59E0B)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${region.percentage}%",
                    style = AppTypography.paragraphBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TopCitiesCard(cities: List<CityResult>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Top Participating Cities",
                style = AppTypography.heading6Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            cities.forEach { city ->
                CityItem(city = city)
            }
        }
    }
}

@Composable
fun CityItem(city: CityResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank medal
            when (city.rank) {
                1 -> Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Gold",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                2 -> Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Silver",
                    tint = Color(0xFFC0C0C0),
                    modifier = Modifier.size(20.dp)
                )
                3 -> Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Bronze",
                    tint = Color(0xFFCD7F32),
                    modifier = Modifier.size(20.dp)
                )
                else -> Text(
                    text = "#${city.rank}",
                    style = AppTypography.paragraphBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = city.city,
                style = AppTypography.paragraphMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${city.votes}/${city.voters}",
                style = AppTypography.paragraphRegular,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${city.percentage}%",
                style = AppTypography.smallParagraphBold,
                color = MainColors.Primary1
            )
        }
    }
}