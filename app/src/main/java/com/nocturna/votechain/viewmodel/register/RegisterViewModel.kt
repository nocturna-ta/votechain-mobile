package com.nocturna.votechain.viewmodel.register

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.network.ApiResponse
import com.nocturna.votechain.data.network.UserRegistrationData
import com.nocturna.votechain.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel for the Register Screen
 */
class RegisterViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val TAG = "RegisterViewModel"
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Initial)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * Register a new user with KTP file
     */
    fun registerUser(
        nationalId: String,
        fullName: String,
        email: String,
        password: String,
        birthPlace: String,
        birthDate: String, // Format: dd/MM/yyyy
        address: String,
        region: String,
        gender: String,
        ktpFileUri: Uri,
        role: String = "voter"
    ) {
        _uiState.value = RegisterUiState.Loading
        Log.d(TAG, "Starting registration process")

        if (nationalId.isBlank() || fullName.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = RegisterUiState.Error("Required fields cannot be empty")
            return
        }

        if (ktpFileUri == Uri.EMPTY) {
            _uiState.value = RegisterUiState.Error("KTP file is required")
            return
        }

        val formattedBirthDate = try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(birthDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting birth date: ${e.message}", e)
            birthDate
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Calling repository registerUser method")
                val result = userRepository.registerUser(
                    nik = nationalId,
                    fullName = fullName,
                    email = email,
                    password = password,
                    gender = gender,
                    birthPlace = birthPlace,
                    birthDate = formattedBirthDate,
                    residentialAddress = address,
                    region = region,
                    role = role,
                    ktpFileUri = ktpFileUri
                )

                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "Registration successful: ${response.message}")
                        _uiState.value = RegisterUiState.Success(response)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Registration failed: ${exception.message}", exception)
                        _uiState.value = RegisterUiState.Error(exception.message ?: "Unknown error occurred")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during registration: ${e.message}", e)
                _uiState.value = RegisterUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Reset the UI state to initial
     */
    fun resetState() {
        _uiState.value = RegisterUiState.Initial
    }

    /**
     * UI State for Register Screen
     */
    sealed class RegisterUiState {
        data object Initial : RegisterUiState()
        data object Loading : RegisterUiState()
        data class Success(val data: ApiResponse<UserRegistrationData>) : RegisterUiState()
        data class Error(val message: String) : RegisterUiState()
    }

    /**
     * Factory for creating RegisterViewModel
     */
    class Factory(
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}