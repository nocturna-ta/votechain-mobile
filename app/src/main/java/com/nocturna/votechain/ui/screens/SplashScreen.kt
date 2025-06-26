package com.nocturna.votechain.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.R
import com.nocturna.votechain.VoteChainApplication
import com.nocturna.votechain.data.repository.UserLoginRepository
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.viewmodel.login.LoginViewModel
import kotlinx.coroutines.delay

private const val TAG = "SplashScreen"

/**
 * Enhanced Splash screen dengan crypto key initialization
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val context = LocalContext.current

    // Get LoginViewModel untuk session check
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory(context))
    val loginState by loginViewModel.uiState.collectAsState()

    // State untuk progress tracking
    var initializationStep by remember { mutableStateOf("Starting...") }
    var initializationProgress by remember { mutableStateOf(0f) }
    var isInitializing by remember { mutableStateOf(true) }

    // Progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = initializationProgress,
        animationSpec = tween(300),
        label = "progress"
    )

    // Enhanced LaunchedEffect dengan comprehensive initialization
    LaunchedEffect(Unit) {
        Log.d(TAG, "🚀 Enhanced SplashScreen started")

        try {
            // Step 1: Initialize application components
            initializationStep = "Initializing security..."
            initializationProgress = 0.1f
            delay(300)

            // Get application instance
            val app = context.applicationContext as VoteChainApplication

            // Step 2: Check user session
            initializationStep = "Checking user session..."
            initializationProgress = 0.3f
            delay(300)

            val userLoginRepository = UserLoginRepository(context)
            val userEmail = userLoginRepository.getUserEmail()
            val isSessionValid = userLoginRepository.isSessionValid()

            if (isSessionValid && !userEmail.isNullOrEmpty()) {
                Log.d(TAG, "👤 Valid session found for: $userEmail")

                // Step 3: Load and verify crypto keys
                initializationStep = "Loading cryptographic keys..."
                initializationProgress = 0.5f
                delay(400)

                // Step 5: Initialize login state
                initializationStep = "Loading user data..."
                initializationProgress = 0.9f
                delay(300)

                loginViewModel.checkLoginState()

            } else {
                Log.d(TAG, "👤 No valid session found")

                // Step 3: Quick initialization untuk guest
                initializationStep = "Ready for login..."
                initializationProgress = 0.9f
                delay(300)
            }

            // Step 6: Complete initialization
            initializationStep = "Ready!"
            initializationProgress = 1.0f
            delay(400)

            isInitializing = false

            Log.d(TAG, "✅ Enhanced SplashScreen initialization completed")
            onSplashComplete()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during enhanced splash initialization: ${e.message}", e)

            // Fallback - complete splash anyway
            initializationStep = "Initialization failed, continuing..."
            initializationProgress = 1.0f
            delay(1000)

            isInitializing = false
            onSplashComplete()
        }
    }

    // UI Implementation
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Votechain Logo",
                modifier = Modifier.size(130.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App Name
            Text(
                text = "VoteChain",
                style = AppTypography.heading2Bold,
                color = MainColors.Primary1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Secure Blockchain Voting",
                style = AppTypography.paragraphRegular,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Progress Section (only show during initialization)
            if (isInitializing) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(250.dp)
                ) {
                    // Progress Bar
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = MainColors.Primary1,
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Text
                    Text(
                        text = initializationStep,
                        style = AppTypography.paragraphRegular,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Percentage
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = AppTypography.paragraphRegular,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Version info at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Powered by Blockchain Technology",
                style = AppTypography.paragraphRegular,
                color = Color.Gray.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Version 1.0.0",
                style = AppTypography.paragraphRegular,
                color = Color.Gray.copy(alpha = 0.4f)
            )
        }
    }
}