package com.nocturna.votechain.viewmodel.forgotpassword

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.repository.ForgotPasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class ForgotPasswordViewModel(private val context: Context) : ViewModel() {

    private val repository = ForgotPasswordRepository(context)

    // Store generated OTP locally for verification
    private var receivedOtp: String? = null

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Initial)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    /**
     * Send OTP to the provided email address
     */
    fun sendOtpToEmail(email: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ForgotPasswordUiState.Loading

                // Generate a 4-digit numeric OTP
                val generatedOtp = generateRandomOtp()
                receivedOtp = generatedOtp

                // Send email with the generated OTP
                val result = repository.sendVerificationEmail(email, generatedOtp)

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    (if (response != null) response::class.java.getDeclaredField("message").get(response) as? String else null) ?: "Failed to send verification code. Please try again."
                } else {
                    _uiState.value = ForgotPasswordUiState.Error(
                        result.exceptionOrNull()?.message ?: "Network error. Please check your connection."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ForgotPasswordUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Generates a random 4-digit OTP
     */
    private fun generateRandomOtp(): String {
        return String.format("%04d", Random.nextInt(10000))
    }

    /**
     * Resend OTP to the same email address
     */
    fun resendOtp(email: String) {
        sendOtpToEmail(email)
    }

    /**
     * Verify the entered OTP locally without API call
     */
    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ForgotPasswordUiState.Loading

                // Local verification - compare the entered OTP with what was generated
                if (otp == receivedOtp) {
                    _uiState.value = ForgotPasswordUiState.OtpVerified(email, otp)
                } else {
                    _uiState.value = ForgotPasswordUiState.Error(
                        "Invalid verification code. Please try again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ForgotPasswordUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Reset password with verified OTP using the v1/user/forgot-password endpoint
     */
    fun resetPassword(email: String, otp: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ForgotPasswordUiState.Loading

                // Call API endpoint v1/user/forgot-password
                val result = repository.resetPassword(email, otp, newPassword)

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null && response::class.java.getDeclaredField("success").get(response) as? Boolean == true) {
                        _uiState.value = ForgotPasswordUiState.PasswordResetSuccess
                    } else {
                        _uiState.value = ForgotPasswordUiState.Error(
                            response?.message ?: "Failed to reset password. Please try again."
                        )
                    }
                } else {
                    _uiState.value = ForgotPasswordUiState.Error(
                        result.exceptionOrNull()?.message ?: "Password reset failed. Please try again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ForgotPasswordUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        if (_uiState.value is ForgotPasswordUiState.Error) {
            _uiState.value = ForgotPasswordUiState.Initial
        }
    }

    // UI States
    sealed class ForgotPasswordUiState {
        object Initial : ForgotPasswordUiState()
        object Loading : ForgotPasswordUiState()
        data class OtpSent(val email: String) : ForgotPasswordUiState()
        data class OtpVerified(val email: String, val otp: String) : ForgotPasswordUiState()
        object PasswordResetSuccess : ForgotPasswordUiState()
        data class Error(val message: String) : ForgotPasswordUiState()
    }

    // Factory to provide context to ViewModel
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
                return ForgotPasswordViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}