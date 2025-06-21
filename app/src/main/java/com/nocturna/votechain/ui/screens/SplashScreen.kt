package com.nocturna.votechain.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.R
import com.nocturna.votechain.viewmodel.login.LoginViewModel
import kotlinx.coroutines.delay

private const val TAG = "SplashScreen"

/**
 * Splash screen that appears after app initialization.
 * The actual first screen the user sees is configured in the themes.xml
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // Get context for repository
    val context = LocalContext.current

    // Get LoginViewModel to check login status
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory(context))
    val loginState by loginViewModel.uiState.collectAsState()

    // Single LaunchedEffect to handle all splash screen logic
    LaunchedEffect(Unit) {
        Log.d(TAG, "SplashScreen started")

        try {
            // Check login state
            loginViewModel.checkLoginState()

            // Small delay to ensure smooth transition
            delay(1000)

            Log.d(TAG, "SplashScreen delay complete, login state: $loginState")

            // Navigate to next screen
            Log.d(TAG, "SplashScreen calling onSplashComplete()")
            onSplashComplete()
        } catch (e: Exception) {
            Log.e(TAG, "Error in SplashScreen", e)
            // Still navigate even if there's an error to prevent app from getting stuck
            onSplashComplete()
        }
    }

    // This composable just displays the same content as the splash theme
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