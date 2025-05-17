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
import com.nocturna.votechain.navigation.VotechainNavGraph
import com.nocturna.votechain.ui.theme.VotechainTheme
import com.nocturna.votechain.utils.openUrlInBrowser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                        onNewsClick = { postSlug ->
                            // Open the news in the browser
                            val newsUrl = "https://www.kpu.go.id/page/detail/blog/$postSlug"
                            openUrlInBrowser(context, newsUrl)
                        }
                    )
                }
            }
        }
    }
}