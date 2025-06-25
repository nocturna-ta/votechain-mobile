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
import com.nocturna.votechain.ui.components.DataUpdateNotification
import com.nocturna.votechain.ui.components.ElectionComparisonCard
import com.nocturna.votechain.ui.components.ElectionStatsSummary
import com.nocturna.votechain.ui.components.ElectionUpdateTimer
import com.nocturna.votechain.ui.components.ParticipationTrendChart
import com.nocturna.votechain.ui.components.RealTimeStatsBar
import com.nocturna.votechain.ui.components.RefreshControlPanel
import com.nocturna.votechain.ui.components.RegionalPerformanceMap
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.candidate.ElectionViewModel
import com.nocturna.votechain.viewmodel.vote.LiveResultViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.collections.isNotEmpty
import kotlin.collections.sortedByDescending
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Data class untuk kandidat dengan persentase
data class ElectionPairWithVotes(
    val electionPair: ElectionPair,
    val electionId: String,
    val votes: Int,
    val percentage: Float,
    val color: Color
)

// Aggregate data untuk pie chart
data class AggregatedElectionData(
    val totalVotes: Int,
    val totalVoters: Int,
    val participationRate: Double,
    val electionPairs: List<ElectionPairWithVotes>
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
    Color(0xFF84CC16), // Lime
    Color(0xFFF97316), // Orange
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveResultScreen(
    electionId: String,
    navController: NavController,
    electionViewModel: ElectionViewModel = viewModel(),
    liveResultViewModel: LiveResultViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val electionPairs by electionViewModel.electionPairs.collectAsState()
    val connectionState by liveResultViewModel.connectionState.collectAsState()
    val rawLiveData by liveResultViewModel.rawLiveData.collectAsState()
    val allElectionsData by liveResultViewModel.allElectionsData.collectAsState()
    val aggregatedElectionStats by liveResultViewModel.aggregatedElectionData.collectAsState()
    val error by liveResultViewModel.error.collectAsState()
    val isLoading by liveResultViewModel.isLoading.collectAsState()

    val strings = LanguageManager.getLocalizedStrings()

    // State untuk menyimpan data agregat dari semua election_id
    var aggregatedData by remember { mutableStateOf<AggregatedElectionData?>(null) }
    var lastUpdateTime by remember { mutableStateOf<Long?>(null) }
    var showUpdateNotification by remember { mutableStateOf(false) }
    val electionStartTime = remember { System.currentTimeMillis() }

    // Simulasi data dari multiple election_id (dalam implementasi nyata, ini akan datang dari WebSocket)
    LaunchedEffect(rawLiveData, electionPairs) {
        if (rawLiveData != null && electionPairs.isNotEmpty()) {
            // Simulasi data untuk multiple election_id
            val simulatedElectionData = generateSimulatedElectionData(electionPairs, rawLiveData!!)
            liveResultViewModel.simulateMultipleElectionData(rawLiveData!!, electionPairs.map { it.id })

            // Agregasi data dari semua election_id
            aggregatedData = aggregateElectionData(electionPairs, simulatedElectionData)

            // Update timestamp dan show notification
            lastUpdateTime = System.currentTimeMillis()
            showUpdateNotification = true
        }
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
        electionViewModel.fetchElectionPairs()
        liveResultViewModel.startLiveResults(electionId)

        // Start multiple elections monitoring
        if (electionPairs.isNotEmpty()) {
            liveResultViewModel.startMultipleElections(electionPairs.map { it.id })
        }
    }

    // Show error if any
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Handle error display
        }
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
                text = strings.results,
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
        else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Data Update Notification
                    item {
                        DataUpdateNotification(
                            isVisible = showUpdateNotification,
                            message = "Data updated successfully!",
                            onDismiss = { showUpdateNotification = false }
                        )
                    }

                    // Refresh Control Panel
                    item {
                        RefreshControlPanel(
                            connectionState = connectionState,
                            onRefreshClick = {
                                liveResultViewModel.refreshAllElections()
                                showUpdateNotification = true
                            },
                            onSettingsClick = { /* Handle settings */ },
                            lastUpdateTime = lastUpdateTime,
                            autoRefreshEnabled = true
                        )
                    }

                    // Real-time Stats Bar
                    item {
                        RealTimeStatsBar(
                            votesPerSecond = rawLiveData?.stats?.votesPerSecond ?: 0.0,
                            activeConnections = 1, // This would come from WebSocket
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Election Timer
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ElectionUpdateTimer(
                                startTime = electionStartTime,
                                modifier = Modifier.weight(1f)
                            )

                            // Quick stats cards
                            if (aggregatedData != null) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Total Candidates",
                                            style = AppTypography.paragraphRegular,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "${aggregatedData!!.electionPairs.size}",
                                            style = AppTypography.paragraphBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Statistics Summary Cards
                    if (aggregatedElectionStats != null) {
                        item {
                            ElectionStatsSummary(aggregatedStats = aggregatedElectionStats!!)
                        }
                    }

                    // Aggregated Stats Card
                    item {
                        AggregatedStatsCard(
                            aggregatedData = aggregatedData,
                            pulseAnimation = pulseAnimation
                        )
                    }

                    // Main Pie Chart Card - Shows distribution across ALL election pairs
                    item {
                        if (aggregatedData != null && aggregatedData!!.electionPairs.isNotEmpty()) {
                            AggregatedPieChartCard(
                                aggregatedData = aggregatedData!!,
                                rotation = rotation
                            )
                        }
                    }

                    // Election Performance Comparison
                    if (allElectionsData.isNotEmpty() && electionPairs.isNotEmpty()) {
                        item {
                            val comparisons = liveResultViewModel.getElectionComparison()
                            if (comparisons.isNotEmpty()) {
                                ElectionComparisonCard(
                                    comparisons = comparisons,
                                    electionPairs = electionPairs
                                )
                            }
                        }
                    }

                    // Participation Trend Chart
                    if (allElectionsData.isNotEmpty() && electionPairs.isNotEmpty()) {
                        item {
                            ParticipationTrendChart(
                                electionsData = allElectionsData,
                                electionPairs = electionPairs
                            )
                        }
                    }

                    // Regional Performance Map
                    if (allElectionsData.isNotEmpty() && electionPairs.isNotEmpty()) {
                        item {
                            RegionalPerformanceMap(
                                electionsData = allElectionsData,
                                electionPairs = electionPairs
                            )
                        }
                    }

                    // Detailed Election Pair Results
                    if (aggregatedData?.electionPairs?.isNotEmpty() == true) {
                        items(aggregatedData!!.electionPairs.sortedByDescending { it.votes }) { electionPairData ->
                            ElectionPairResultCard(
                                electionPairData = electionPairData,
                                rank = aggregatedData!!.electionPairs.sortedByDescending { it.votes }
                                    .indexOf(electionPairData) + 1
                            )
                        }
                    }

                    // Regional Breakdown untuk current election
                    if (rawLiveData?.regions?.isNotEmpty() == true) {
                        item {
                            RegionalBreakdownCard(regions = rawLiveData!!.regions)
                        }
                    }

                    // Top Cities untuk current election
                    if (rawLiveData?.topCities?.isNotEmpty() == true) {
                        item {
                            TopCitiesCard(cities = rawLiveData!!.topCities)
                        }
                    }

                    // Individual Election Results
                    if (allElectionsData.isNotEmpty()) {
                        item {
                            IndividualElectionResultsCard(
                                allLiveData = allElectionsData,
                                electionPairs = electionPairs
                            )
                        }
                    }

                    // Footer with last update info
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🔄 Data refreshes automatically every 30 seconds",
                                    style = AppTypography.paragraphRegular,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (lastUpdateTime != null) {
                                    Text(
                                        text = "Last updated: ${
                                            java.text.SimpleDateFormat(
                                                "HH:mm:ss",
                                                java.util.Locale.getDefault()
                                            ).format(java.util.Date(lastUpdateTime!!))
                                        }",
                                        style = AppTypography.paragraphRegular,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Function untuk mensimulasikan data dari multiple election_id
fun generateSimulatedElectionData(
    electionPairs: List<ElectionPair>,
    currentLiveData: LiveElectionData
): Map<String, LiveElectionData> {
    val simulatedData = mutableMapOf<String, LiveElectionData>()

    electionPairs.forEach { pair ->
        // Simulasi data untuk setiap election pair
        val baseVotes = currentLiveData.totalVotes
        val randomMultiplier = Random.nextDouble(0.5, 1.5)
        val simulatedVotes = (baseVotes * randomMultiplier).toInt()

        val simulatedElectionData = LiveElectionData(
            electionId = pair.id,
            totalVotes = simulatedVotes,
            totalVoters = currentLiveData.totalVoters,
            lastUpdated = currentLiveData.lastUpdated,
            overallPercentage = currentLiveData.overallPercentage * randomMultiplier,
            regions = currentLiveData.regions.map { region ->
                region.copy(votes = (region.votes * randomMultiplier).toInt())
            },
            topCities = currentLiveData.topCities.map { city ->
                city.copy(votes = (city.votes * randomMultiplier).toInt())
            },
            stats = currentLiveData.stats
        )

        simulatedData[pair.id] = simulatedElectionData
    }

    return simulatedData
}

// Function untuk agregasi data dari semua election_id
fun aggregateElectionData(
    electionPairs: List<ElectionPair>,
    allLiveData: Map<String, LiveElectionData>
): AggregatedElectionData {
    val totalVotes = allLiveData.values.sumOf { it.totalVotes }
    val totalVoters = allLiveData.values.firstOrNull()?.totalVoters ?: 0
    val participationRate = if (totalVoters > 0) (totalVotes.toDouble() / totalVoters) * 100 else 0.0

    val electionPairsWithVotes = electionPairs.mapIndexed { index, pair ->
        val liveData = allLiveData[pair.id]
        val votes = liveData?.totalVotes ?: 0
        val percentage = if (totalVotes > 0) (votes.toFloat() / totalVotes) * 100f else 0f

        ElectionPairWithVotes(
            electionPair = pair,
            electionId = pair.id,
            votes = votes,
            percentage = percentage,
            color = PieChartColors[index % PieChartColors.size]
        )
    }

    return AggregatedElectionData(
        totalVotes = totalVotes,
        totalVoters = totalVoters,
        participationRate = participationRate,
        electionPairs = electionPairsWithVotes
    )
}

@Composable
fun AggregatedStatsCard(
    aggregatedData: AggregatedElectionData?,
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
                    text = "Overall Election Statistics",
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
                        .format(aggregatedData?.totalVotes ?: 0),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    title = "Total Candidates",
                    value = "${aggregatedData?.electionPairs?.size ?: 0}",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    title = "Participation",
                    value = "${String.format("%.1f", aggregatedData?.participationRate ?: 0.0)}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AggregatedPieChartCard(
    aggregatedData: AggregatedElectionData,
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
                text = "Vote Distribution - All Candidates",
                style = AppTypography.heading5Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pie Chart
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                AggregatedAnimatedPieChart(
                    electionPairs = aggregatedData.electionPairs,
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
                    aggregatedData.electionPairs.maxByOrNull { it.votes }?.let { leader ->
                        Text(
                            text = "${leader.percentage.toInt()}%",
                            style = AppTypography.heading3Bold,
                            color = leader.color
                        )
                        Text(
                            text = "#${leader.electionPair.election_no}",
                            style = AppTypography.paragraphMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Legend
            aggregatedData.electionPairs.sortedByDescending { it.votes }.forEach { electionPairData ->
                AggregatedLegendItem(
                    electionPairData = electionPairData,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AggregatedAnimatedPieChart(
    electionPairs: List<ElectionPairWithVotes>,
    modifier: Modifier = Modifier
) {
    val animatedValues = electionPairs.map { electionPairData ->
        animateFloatAsState(
            targetValue = electionPairData.percentage,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = minOf(canvasWidth, canvasHeight) / 2f * 0.85f
        val center = center

        var currentAngle = -90f
        val strokeWidth = 50.dp.toPx()

        electionPairs.forEachIndexed { index, electionPairData ->
            val animatedValue = animatedValues[index].value
            val sweepAngle = (animatedValue / 100f) * 360f

            if (sweepAngle > 0) {
                // Draw pie slice
                drawArc(
                    color = electionPairData.color,
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
}

@Composable
fun AggregatedLegendItem(
    electionPairData: ElectionPairWithVotes,
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
                    .background(electionPairData.color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "#${electionPairData.electionPair.election_no} - ${electionPairData.electionPair.president.full_name}",
                    style = AppTypography.paragraphMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.US).format(electionPairData.votes)} votes",
                    style = AppTypography.paragraphRegular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "${electionPairData.percentage.toInt()}%",
            style = AppTypography.paragraphBold,
            color = electionPairData.color
        )
    }
}

@Composable
fun ElectionPairResultCard(
    electionPairData: ElectionPairWithVotes,
    rank: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle election pair detail */ },
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(electionPairData.color),
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
                    text = "#${electionPairData.electionPair.election_no}",
                    style = AppTypography.paragraphBold,
                    color = electionPairData.color
                )
                Text(
                    text = "${electionPairData.electionPair.president.full_name} & ${electionPairData.electionPair.vice_president.full_name}",
                    style = AppTypography.paragraphBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.US).format(electionPairData.votes)} votes",
                    style = AppTypography.paragraphBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${electionPairData.percentage.toInt()}%",
                    style = AppTypography.heading6Bold,
                    color = electionPairData.color
                )
                Text(
                    text = "of total",
                    style = AppTypography.paragraphRegular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun IndividualElectionResultsCard(
    allLiveData: Map<String, LiveElectionData>,
    electionPairs: List<ElectionPair>
) {
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
                text = "Individual Election Results",
                style = AppTypography.heading5Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            allLiveData.forEach { (electionId, liveData) ->
                val electionPair = electionPairs.find { it.id == electionId }
                if (electionPair != null) {
                    IndividualElectionItem(
                        electionPair = electionPair,
                        liveData = liveData
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun IndividualElectionItem(
    electionPair: ElectionPair,
    liveData: LiveElectionData
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "#${electionPair.election_no} - ${electionPair.president.full_name}",
                style = AppTypography.paragraphMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Election ID: ${liveData.electionId}",
                style = AppTypography.paragraphRegular,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.US).format(liveData.totalVotes)} votes",
                style = AppTypography.paragraphBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${String.format("%.1f", liveData.overallPercentage * 100)}% participation",
                style = AppTypography.paragraphRegular,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Reuse existing components
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