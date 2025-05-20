package com.nocturna.votechain.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.nocturna.votechain.data.repository.UserLoginRepository
import com.nocturna.votechain.navigation.VotechainNavGraph
import com.nocturna.votechain.ui.theme.VotechainTheme
import com.nocturna.votechain.utils.RegistrationStatusChecker
import com.nocturna.votechain.utils.openUrlInBrowser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create an instance of RegistrationStatusChecker
        val registrationStatusChecker = RegistrationStatusChecker(this)

        // Create an instance of UserLoginRepository to check login status
        val userLoginRepository = UserLoginRepository(this)

        // Determine the appropriate start destination
        val startDestination = when {
            userLoginRepository.isUserLoggedIn() -> "home" // User is logged in, go directly to home
            registrationStatusChecker.hasRegistrationState() -> registrationStatusChecker.getStartDestination()
            else -> "splash" // Default start with splash screen
        }

        setContent {
            VotechainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val context = LocalContext.current

                    VotechainNavGraph(
                        navController = navController,
                        startDestination = startDestination, // Use the determined start destination
                        onNewsClick = { newsItem ->
                            // Open the news in the browser with the correct URL format:
                            // https://www.kpu.go.id/berita/baca/id/post_slug
                            val newsUrl = "https://www.kpu.go.id/berita/baca/${newsItem.id}/${newsItem.post_slug}"
                            openUrlInBrowser(context, newsUrl)
                        }
                    )
                }
            }
        }
    }
}