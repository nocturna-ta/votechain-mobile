package com.nocturna.votechain.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nocturna.votechain.ui.screens.register.RegisterScreen
import com.nocturna.votechain.viewmodel.register.RegisterViewModel

/**
 * A wrapper component that handles the registration flow based on the persisted state
 * This component determines which screen to show based on the current registration state
 * Updated to handle persistent registration states across app restarts
 */
@Composable
fun RegistrationFlowController(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory(androidx.compose.ui.platform.LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    val TAG = "RegistrationFlowController"

    // Handle navigation based on registration state
    LaunchedEffect(uiState) {
        Log.d(TAG, "UI State changed to: $uiState")

        when (uiState) {
            RegisterViewModel.RegisterUiState.Waiting -> {
                Log.d(TAG, "Navigating to waiting screen")
                navController.navigate("waiting") {
                    popUpTo("register") { inclusive = true }
                }
            }
            RegisterViewModel.RegisterUiState.Approved -> {
                Log.d(TAG, "Navigating to accepted screen")
                navController.navigate("accepted") {
                    popUpTo("register") { inclusive = true }
                }
            }
            RegisterViewModel.RegisterUiState.Rejected -> {
                Log.d(TAG, "Navigating to rejected screen")
                navController.navigate("rejected") {
                    popUpTo("register") { inclusive = true }
                }
            }
            is RegisterViewModel.RegisterUiState.Success -> {
                Log.d(TAG, "Registration success, handling based on verification status")
                // Let the ViewModel handle the state transition based on verification status
                // The ViewModel will update the UI state accordingly
            }
            is RegisterViewModel.RegisterUiState.Error -> {
                Log.e(TAG, "Registration error: ${(uiState as RegisterViewModel.RegisterUiState.Error).message}")
                // Stay on register screen to show error
            }
            RegisterViewModel.RegisterUiState.Loading -> {
                Log.d(TAG, "Registration in progress")
                // Stay on register screen to show loading
            }
            RegisterViewModel.RegisterUiState.Initial -> {
                Log.d(TAG, "Initial state - showing register screen")
                // Show register screen
            }
        }
    }

    // Check if user should be redirected immediately (when coming from register navigation)
    LaunchedEffect(Unit) {
        Log.d(TAG, "Checking initial registration state")
        val currentState = viewModel.getCurrentRegistrationState()
        Log.d(TAG, "Current registration state: $currentState")

        // The ViewModel's init already handles this, but we can add additional logic here if needed
        when (currentState) {
            com.nocturna.votechain.data.repository.RegistrationStateManager.STATE_WAITING -> {
                Log.d(TAG, "User has waiting registration, should redirect to waiting screen")
            }
            com.nocturna.votechain.data.repository.RegistrationStateManager.STATE_APPROVED -> {
                Log.d(TAG, "User has approved registration, should redirect to accepted screen")
            }
            com.nocturna.votechain.data.repository.RegistrationStateManager.STATE_REJECTED -> {
                Log.d(TAG, "User has rejected registration, allowing new registration")
            }
            else -> {
                Log.d(TAG, "No active registration state, showing register form")
            }
        }
    }

    // Render the appropriate screen based on current state
    when (uiState) {
        RegisterViewModel.RegisterUiState.Waiting -> {
            // Don't render anything here, navigation will handle it
            Log.d(TAG, "Waiting state - navigation should handle this")
        }
        RegisterViewModel.RegisterUiState.Approved -> {
            // Don't render anything here, navigation will handle it
            Log.d(TAG, "Approved state - navigation should handle this")
        }
        RegisterViewModel.RegisterUiState.Rejected -> {
            // Don't render anything here, navigation will handle it
            Log.d(TAG, "Rejected state - navigation should handle this")
        }
        else -> {
            // Render the RegisterScreen for all other states
            Log.d(TAG, "Rendering RegisterScreen for state: $uiState")
            RegisterScreen(
                onRegisterClick = { /* Handled by the viewModel */ },
                onLoginClick = {
                    // When login text is clicked, navigate back to login screen
                    Log.d(TAG, "Login clicked, navigating to login")
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onWaitingScreen = {
                    Log.d(TAG, "Navigating to waiting screen from register")
                    navController.navigate("waiting")
                },
                navigateToAccepted = {
                    Log.d(TAG, "Navigating to accepted screen from register")
                    navController.navigate("accepted") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                navigateToRejected = {
                    Log.d(TAG, "Navigating to rejected screen from register")
                    navController.navigate("rejected") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }
    }
}