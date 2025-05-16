package com.nocturna.votechain.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.R
import kotlinx.coroutines.delay

/**
 * Splash screen that appears after app initialization.
 * The actual first screen the user sees is configured in the themes.xml
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // We skip the fade-in animation since the splash is already showing from the theme
    // Just handle timing for navigation
    LaunchedEffect(key1 = true) {
        // Just delay slightly to ensure smooth transition
        delay(1000)
        onSplashComplete()
    }

    // This composable now just displays the same content as the splash theme
    // to ensure consistent visuals during the transition
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Votechain Logo",
            modifier = Modifier.size(130.dp)
        )
    }
}