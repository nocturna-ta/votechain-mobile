package com.nocturna.votechain.viewmodel.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.network.ApiResponse
import com.nocturna.votechain.data.network.UserLoginData
import com.nocturna.votechain.data.repository.UserLoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Login Screen
 */
class LoginViewModel(
    private val userLoginRepository: UserLoginRepository
) : ViewModel() {
    private val TAG = "LoginViewModel"

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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
                        if (response.data?.is_active == true) {
                            Log.d(TAG, "Login successful and user is active")
                            _uiState.value = LoginUiState.Success(response)
                        } else {
                            Log.e(TAG, "Login failed: User account is not active")
                            _uiState.value = LoginUiState.Error("Your account is not active. Please contact support.")
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Login failed: ${exception.message}", exception)
                        _uiState.value = LoginUiState.Error(exception.message ?: "Unknown error occurred")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during login: ${e.message}", e)
                _uiState.value = LoginUiState.Error(e.message ?: "Unknown error occurred")
            }
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
     * UI State for Login Screen
     */
    sealed class LoginUiState {
        data object Initial : LoginUiState()
        data object Loading : LoginUiState()
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
                return LoginViewModel(userLoginRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}