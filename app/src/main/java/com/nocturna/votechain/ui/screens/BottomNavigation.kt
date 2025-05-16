package com.nocturna.votechain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors

@Composable
fun BottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = NeutralColors.Neutral10,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            // Home Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top,
                modifier = Modifier
                    .padding(top = 12.dp) // Uniform top padding for all items
                    .clickable { onNavigate("home") }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Home",
                    tint = if (currentRoute == "home") MainColors.Primary1 else NeutralColors.Neutral50,
                    modifier = Modifier.size(24.dp)
                )
                if (currentRoute == "home") {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .width(28.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MainColors.Primary1)
                    )
                }
            }

            // Votes Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top,
                modifier = Modifier
                    .padding(top = 12.dp) // Uniform top padding for all items
                    .clickable { onNavigate("votes") }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.voting),
                    contentDescription = "Votes",
                    tint = if (currentRoute == "votes") MainColors.Primary1 else NeutralColors.Neutral50,
                    modifier = Modifier.size(24.dp)
                )
                if (currentRoute == "votes") {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .width(28.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MainColors.Primary1)
                    )
                }
            }

            // Profile Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top,
                modifier = Modifier
                    .padding(top = 12.dp) // Uniform top padding for all items
                    .clickable { onNavigate("profile") }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile",
                    tint = if (currentRoute == "profile") MainColors.Primary1 else NeutralColors.Neutral50,
                    modifier = Modifier.size(24.dp)
                )
                if (currentRoute == "profile") {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .width(28.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MainColors.Primary1)
                    )
                }
            }
        }
    }
}