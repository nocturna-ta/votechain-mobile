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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.screens.BottomNavigation
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.utils.openUrlInBrowser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onVoteItemClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onVotesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onNewsClick: (String) -> Unit = {}
) {
    val currentRoute = "home" // Default route is home
    val context = LocalContext.current

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
                .verticalScroll(rememberScrollState())
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

            // Carousel Section - Now using our updated HomeCarousel with KPU news
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
//                Text(
//                    text = "Latest News",
//                    style = AppTypography.heading4SemiBold,
//                    color = PrimaryColors.Primary80,
//                    modifier = Modifier.padding(bottom = 12.dp)
//                )

                HomeCarousel(
                    onNewsClick = { postSlug ->
                        // Open the news in the browser
                        val newsUrl = "https://www.kpu.go.id/page/detail/blog/$postSlug"
                        openUrlInBrowser(context, newsUrl)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}