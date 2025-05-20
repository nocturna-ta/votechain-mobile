package com.nocturna.votechain.ui.screens.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nocturna.votechain.viewmodel.register.RegisterViewModel

/**
 * A wrapper component that handles the registration flow based on the persisted state
 * This component determines which screen to show based on the current registration state
 */
@Composable
fun RegistrationFlowController(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory(androidx.compose.ui.platform.LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()

    // Effect to handle navigation based on registration state
    LaunchedEffect(uiState) {
        when (uiState) {
            RegisterViewModel.RegisterUiState.Waiting -> {
                navController.navigate("waiting") {
                    popUpTo("register") { inclusive = true }
                }
            }
            RegisterViewModel.RegisterUiState.Approved -> {
                navController.navigate("accepted") {
                    popUpTo("register") { inclusive = true }
                }
            }
            RegisterViewModel.RegisterUiState.Rejected -> {
                navController.navigate("rejected") {
                    popUpTo("register") { inclusive = true }
                }
            }
            else -> {
                // Other states are handled by the RegisterScreen
            }
        }
    }

    // Render the RegisterScreen for normal registration flow
    RegisterScreen(
        onRegisterClick = { /* Handled by the viewModel */ },
        onLoginClick = {
            // When login text is clicked, navigate back to login screen
            navController.navigate("login") {
                popUpTo("register") { inclusive = true }
            }
        },
        onWaitingScreen = {
            navController.navigate("waiting")
        },
        navigateToAccepted = {
            navController.navigate("accepted") {
                popUpTo("register") { inclusive = true }
            }
        },
        navigateToRejected = {
            navController.navigate("rejected") {
                popUpTo("register") { inclusive = true }
            }
        },
        viewModel = viewModel
    )
}