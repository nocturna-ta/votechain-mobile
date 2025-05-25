package com.nocturna.votechain.viewmodel.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.UserLoginData
import com.nocturna.votechain.data.repository.RegistrationStateManager
import com.nocturna.votechain.data.repository.UserLoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Login Screen
 */
class LoginViewModel(
    private val userLoginRepository: UserLoginRepository,
    private val context: Context
) : ViewModel() {
    private val TAG = "LoginViewModel"
    private val registrationStateManager = RegistrationStateManager(context)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Check initial login state
    init {
        checkLoginState()
    }

    /**
     * Check if the user is already logged in
     */
    fun checkLoginState() {
        if (userLoginRepository.isUserLoggedIn()) {
            // If user is already logged in, clear any registration state
            clearRegistrationStateIfLoggedIn()
            _uiState.value = LoginUiState.AlreadyLoggedIn
        }
    }

    /**
     * Login user with email and password
     */
    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Email and password cannot be empty")
            return
        }

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val result = userLoginRepository.loginUser(email, password)

                result.fold(
                    onSuccess = { response ->
                        if (response.code == 200 && response.data?.is_active == true) {
                            Log.d(TAG, "Login successful and user is active")
                            // Save token for persistent login
                            response.data.token?.let { token ->
                                userLoginRepository.saveUserToken(token)
                            }

                            // Clear any existing registration state since user has successfully logged in
                            clearRegistrationStateAfterLogin()

                            _uiState.value = LoginUiState.Success(response)
                        } else if (response.code == 400 || response.code == 401) {
                            Log.e(TAG, "Login failed: Invalid credentials")
                            _uiState.value = LoginUiState.Error("Invalid email or password. Please try again.")
                        } else if (response.data?.is_active == false) {
                            Log.e(TAG, "Login failed: User account is not active")
                            _uiState.value = LoginUiState.Error("Your account is not active. Please contact support.")
                        } else {
                            Log.e(TAG, "Login failed: ${response.message}")
                            _uiState.value = LoginUiState.Error(response.message)
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Login failed: ${exception.message}", exception)
                        _uiState.value = LoginUiState.Error(exception.message ?: "Failed to connect to server. Please check your internet connection.")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during login: ${e.message}", e)
                _uiState.value = LoginUiState.Error(e.message ?: "An unexpected error occurred. Please try again.")
            }
        }
    }

    /**
     * Clear registration state when user successfully logs in
     * This ensures that if they logout and try to register again, they won't be stuck in old states
     */
    private fun clearRegistrationStateAfterLogin() {
        val currentState = registrationStateManager.getRegistrationState()
        if (currentState != RegistrationStateManager.STATE_NONE) {
            Log.d(TAG, "Clearing registration state after successful login. Previous state: $currentState")
            registrationStateManager.clearRegistrationState()
        }
    }

    /**
     * Clear registration state if user is already logged in
     * Called during initialization
     */
    private fun clearRegistrationStateIfLoggedIn() {
        val currentState = registrationStateManager.getRegistrationState()
        if (currentState != RegistrationStateManager.STATE_NONE) {
            Log.d(TAG, "User is already logged in, clearing existing registration state: $currentState")
            registrationStateManager.clearRegistrationState()
        }
    }

    /**
     * Reset the UI state to initial
     */
    fun resetState() {
        _uiState.value = LoginUiState.Initial
    }

    /**
     * Check if user is already logged in
     */
    fun isUserLoggedIn(): Boolean {
        return userLoginRepository.isUserLoggedIn()
    }

    /**
     * Log out the current user
     */
    fun logoutUser() {
        userLoginRepository.logoutUser()
        _uiState.value = LoginUiState.Initial
    }

    /**
     * UI State for Login Screen
     */
    sealed class LoginUiState {
        data object Initial : LoginUiState()
        data object Loading : LoginUiState()
        data object AlreadyLoggedIn : LoginUiState()
        data class Success(val data: ApiResponse<UserLoginData>) : LoginUiState()
        data class Error(val message: String) : LoginUiState()
    }

    /**
     * Factory for creating LoginViewModel
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                val userLoginRepository = UserLoginRepository(context)
                return LoginViewModel(userLoginRepository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}