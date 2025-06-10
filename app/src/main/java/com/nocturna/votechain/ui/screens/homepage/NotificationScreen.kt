package com.nocturna.votechain.ui.screens.homepage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.ui.theme.VotechainTheme
import com.nocturna.votechain.utils.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {}
) {
    val strings = LanguageManager.getLocalizedStrings()

    Column(modifier = Modifier.fillMaxSize()) {
        // Custom top bar with centered title
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
                    modifier = Modifier.size(20.dp) // Smaller icon size
                )
            }

            // Centered title
            Text(
                text = strings.notification,
                style = AppTypography.heading4Regular,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Content
        NotificationContent()
    }
}

@Composable
fun NotificationContent(modifier: Modifier = Modifier) {
    // Sample notification data
    val notifications = listOf(
        NotificationItem(
            id = "1",
            title = "Login Successful",
            message = "Welcome back! You have successfully logged in.",
            timestamp = "Today 12:18"
        ),
        NotificationItem(
            id = "2",
            title = "Registration Successful",
            message = "Your account has been successfully created. Welcome aboard!",
            timestamp = "Yesterday 08:10"
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        items(notifications) { notification ->
            NotificationItemView(notification)
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        }
    }
}

@Composable
fun NotificationItemView(notification: NotificationItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = notification.title,
                style = AppTypography.heading6Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = notification.timestamp,
                style = AppTypography.smallParagraphRegular,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = notification.message,
            style = AppTypography.paragraphRegular,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String
)

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    VotechainTheme {
        NotificationScreen()
    }
}