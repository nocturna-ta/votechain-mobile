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
import com.nocturna.votechain.data.repository.UserLoginRepository
import com.nocturna.votechain.viewmodel.login.LoginViewModel
import kotlinx.coroutines.delay

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

    // Check for login state when screen loads
    LaunchedEffect(Unit) {
        loginViewModel.checkLoginState()

        // Small delay to ensure smooth transition and let the view model check login state
        delay(1500)
        onSplashComplete()
    }

    // We skip the fade-in animation since the splash is already showing from the theme
    // Just handle timing for navigation
// Observe login state changes
    LaunchedEffect(loginState) {
        if (loginState is LoginViewModel.LoginUiState.AlreadyLoggedIn) {
            // User is already logged in, we can navigate immediately
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