package com.nocturna.votechain.ui.screens.profilepage

import com.nocturna.votechain.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.utils.ThemeManager
import com.nocturna.votechain.utils.getLocalizedStrings

@Composable
fun ProfileScreen(
    onNavigateToFAQ: () -> Unit = {},
    navController: NavController,
    onHomeClick: () -> Unit = {},
    onVotesClick: () -> Unit = {}
) {
    var language by remember { mutableStateOf("Indonesia") }
    // State to control dropdown visibility
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var showThemeDropdown by remember { mutableStateOf(false) }
    // Current route is set to "profile" since we're on the profile/settings screen
    val currentRoute = "profile"

    // State for password confirmation dialog
    var showPasswordDialog by remember { mutableStateOf(false) }

    // String resources based on selected language
    val strings = getLocalizedStrings(language)

    val context = LocalContext.current
    val currentTheme by ThemeManager.currentTheme.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header with gradient teal background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.background),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }


            // Profile Card - positioned to overlap with the header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-50).dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Luh Komang Devi Savitri",
                            style = AppTypography.heading4Bold,
                            color = PrimaryColors.Primary60
                        )

                        Button(
                            onClick = { showPasswordDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColors.Primary1
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 3.dp, vertical = 4.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("View", style = AppTypography.heading6Regular)
                            Icon(
                                painter = painterResource(id = R.drawable.right2),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(12.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        // Using tag_complete.xml drawable
                        Image(
                            painter = painterResource(id = R.drawable.tag_complete),
                            contentDescription = null,
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }

            // Password Confirmation Dialog
            PasswordConfirmationDialog(
                isOpen = showPasswordDialog,
                onCancel = { showPasswordDialog = false },
                onSubmit = { password ->
                    // Handle password verification here
                    // For demo purposes, always consider password correct
                    showPasswordDialog = false
                    // You could navigate to a detailed profile view or perform other actions here
                    navController.navigate("account_details")
                }
            )

            // Content area with padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Settings Section
                Text(
                    text = "Settings",
                    style = AppTypography.heading4Bold,
                    color = PrimaryColors.Primary80,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Accessibility Settings
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable {
//                            if (isAccessibilityEnabled) {
//                                accessibilityManager.speakAction("Pengaturan bantuan suara")
//                            }
//                        }
//                        .padding(vertical = 12.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Column {
//                        Text(
//                            text = "Bantuan Suara",
//                            style = AppTypography.heading5Medium,
//                            color = NeutralColors.Neutral40
//                        )
//                        Text(
//                            text = "Untuk pengguna tunanetra",
//                            style = AppTypography.paragraphRegular,
//                            color = NeutralColors.Neutral50
//                        )
//                    }
//
//                    Switch(
//                        checked = isAccessibilityEnabled,
//                        onCheckedChange = { enabled ->
//                            if (enabled) {
//                                accessibilityManager.initialize()
//                            } else {
//                                accessibilityManager.setEnabled(false)
//                            }
//                        },
//                        colors = SwitchDefaults.colors(
//                            checkedThumbColor = MainColors.Primary1,
//                            checkedTrackColor = MainColors.Primary1.copy(alpha = 0.5f)
//                        )
//                    )
//                }
//
//                Divider(color = NeutralColors.Neutral20, thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showThemeDropdown = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Theme",
                        style = AppTypography.heading5Medium,
                        color = NeutralColors.Neutral40
                    )

                    // Theme selection pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, NeutralColors.Neutral30, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentTheme,
                                style = AppTypography.paragraphRegular,
                                color = NeutralColors.Neutral40
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.down2),
                                contentDescription = null,
                                tint = NeutralColors.Neutral40,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(14.dp)
                            )
                        }
                    }

                    // Theme dropdown menu
                    DropdownMenu(
                        expanded = showThemeDropdown,
                        onDismissRequest = { showThemeDropdown = false },
                        modifier = Modifier
                            .background(Color.White)
                            .width(200.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = ThemeManager.THEME_LIGHT,
                                    style = AppTypography.paragraphRegular,
                                    color = if (currentTheme == ThemeManager.THEME_LIGHT)
                                        MainColors.Primary1 else NeutralColors.Neutral70
                                )
                            },
                            onClick = {
                                ThemeManager.setTheme(context, ThemeManager.THEME_LIGHT)
                                showThemeDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = ThemeManager.THEME_DARK,
                                    style = AppTypography.paragraphRegular,
                                    color = if (currentTheme == ThemeManager.THEME_DARK)
                                        MainColors.Primary1 else NeutralColors.Neutral70
                                )
                            },
                            onClick = {
                                ThemeManager.setTheme(context, ThemeManager.THEME_DARK)
                                showThemeDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = ThemeManager.THEME_SYSTEM,
                                    style = AppTypography.paragraphRegular,
                                    color = if (currentTheme == ThemeManager.THEME_SYSTEM)
                                        MainColors.Primary1 else NeutralColors.Neutral70
                                )
                            },
                            onClick = {
                                ThemeManager.setTheme(context, ThemeManager.THEME_SYSTEM)
                                showThemeDropdown = false
                            }
                        )
                    }
                }

                Divider(color = NeutralColors.Neutral20, thickness = 1.dp)

                // Language Selector - Dropdown style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDropdown = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.language,
                        style = AppTypography.heading5Medium,
                        color = NeutralColors.Neutral40
                    )

                    // Language selection pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, NeutralColors.Neutral30, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = language,
                                style = AppTypography.paragraphRegular,
                                color = NeutralColors.Neutral40
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.down2),
                                contentDescription = null,
                                tint = NeutralColors.Neutral40,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(14.dp)
                            )
                        }
                    }

                    // Language dropdown menu
                    DropdownMenu(
                        expanded = showLanguageDropdown,
                        onDismissRequest = { showLanguageDropdown = false },
                        modifier = Modifier
                            .background(Color.White)
                            .width(150.dp)
                    ) {
                        // English option
                        DropdownMenuItem(
                            text = { Text("English") },
                            onClick = {
                                language = "English"
                                showLanguageDropdown = false
                            }
                        )
                        // Indonesian option
                        DropdownMenuItem(
                            text = { Text("Indonesia") },
                            onClick = {
                                language = "Indonesia"
                                showLanguageDropdown = false
                            }
                        )
                    }
                }

                Divider(color = NeutralColors.Neutral20, thickness = 1.dp)

                // About Section
                Text(
                    text = "About",
                    style = AppTypography.heading4Bold,
                    color = PrimaryColors.Primary80,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )

                // FAQ Item - Updated to navigate to FAQ Screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToFAQ() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "FAQ",
                        style = AppTypography.heading5Medium,
                        color = NeutralColors.Neutral40,
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.right2),
                        contentDescription = "Navigate to FAQ",
                        tint = NeutralColors.Neutral30,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Divider(color = NeutralColors.Neutral20, thickness = 1.dp)
            }
        }

        // Bottom Navigation Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            BottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    when (route) {
                        "home" -> onHomeClick()
                        "votes" -> onVotesClick()
                        "profile" -> { /* Already on profile */
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        // Home Icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
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
            verticalArrangement = Arrangement.Top,
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
            verticalArrangement = Arrangement.Top,
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

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreen(
            navController = rememberNavController()
        )
    }
}