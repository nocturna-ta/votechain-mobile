package com.nocturna.votechain.ui.screens.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.screens.BottomNavigation
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.ui.theme.VotechainTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onVoteItemClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onVotesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
//    var selectedTabIndex by remember { mutableStateOf(0) }
//    val tabs = listOf("Today", "Weekly", "Monthly")
    // Define currentRoute to pass to BottomNavigation
    val currentRoute = "home" // Default route is home

    Scaffold(
        bottomBar = {
            BottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    when (route) {
                        "home" -> onHomeClick()
                        "votes" -> onVotesClick()
                        "profile" -> onProfileClick()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .padding(horizontal = 24.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Empowering Democracy",
                        style = AppTypography.heading4Regular,
                        color = PrimaryColors.Primary70
                    )
                    Text(
                        text = "One Vote at a Time",
                        style = AppTypography.heading1Bold,
                        color = PrimaryColors.Primary80
                    )
                }

                IconButton(onClick = { onNotificationClick() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.notification),
                        contentDescription = "Notifications",
                        tint = MainColors.Primary1
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Row
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .wrapContentWidth(Alignment.Start)
//                    .padding(vertical = 8.dp)
//            ) {
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(24.dp)
//                ) {
//                    tabs.forEachIndexed { index, title ->
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Text(
//                                text = title,
//                                style = AppTypography.heading3Medium,
//                                color = if (selectedTabIndex == index)
//                                    MainColors.Primary1
//                                else
//                                    NeutralColors.Neutral50,
//                                modifier = Modifier
//                                    .clickable { selectedTabIndex = index }
//                                    .padding(4.dp)
//                            )
//                            if (selectedTabIndex == index) {
//                                val textMeasurer = rememberTextMeasurer()
//                                val textLayoutResult = remember(title) {
//                                    textMeasurer.measure(
//                                        text = title,
//                                        style = AppTypography.heading3Medium
//                                    )
//                                }
//                                val textWidth = textLayoutResult.size.width.toFloat()
//
//                                Box(
//                                    modifier = Modifier
//                                        .padding(top = 4.dp)
//                                        .width(with(LocalDensity.current) { textWidth.toDp() })
//                                        .height(2.dp)
//                                        .clip(RoundedCornerShape(1.dp))
//                                        .background(MainColors.Primary1)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
            Spacer(modifier = Modifier.height(14.dp))

            // Carousel Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                HomeCarousel()
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Votes List Section
            Text(
                text = "Active Votes List",
                style = AppTypography.heading4SemiBold,
                color = PrimaryColors.Primary80
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Vote Item
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                onClick = { onVoteItemClick("presidential_election_2024") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "2024 Presidential Election - Indonesia",
                            style = AppTypography.heading5Bold,
                            color = PrimaryColors.Primary60
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Choose the leaders you trust to guide Indonesia forward",
                            style = AppTypography.heading6Medium,
                            color = NeutralColors.Neutral40,
                            maxLines = 1,
                            modifier = Modifier.width(270.dp),
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.right2),
                        contentDescription = "View Details",
                        tint = MainColors.Primary1
                    )
                }
            }
        }
    }
}

// HomeCarousel implementation would go here
// @Composable
// fun HomeCarousel() {
//     // Implementation of carousel functionality
// }

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    VotechainTheme {
        HomeScreen()
    }
}