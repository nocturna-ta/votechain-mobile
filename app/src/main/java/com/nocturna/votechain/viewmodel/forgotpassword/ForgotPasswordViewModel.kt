package com.nocturna.votechain.viewmodel.forgotpassword

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.repository.ForgotPasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Forgot Password flow
 */
class ForgotPasswordViewModel(
    private val forgotPasswordRepository: ForgotPasswordRepository
) : ViewModel() {
    private val TAG = "ForgotPasswordViewModel"

    private val _state = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Initial)
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    /**
     * Send verification email with OTP
     */
    fun sendVerificationEmail(email: String) {
        if (email.isBlank()) {
            _state.value = ForgotPasswordState.Error("Email cannot be empty")
            return
        }

        _state.value = ForgotPasswordState.Loading

        viewModelScope.launch {
            try {
                val result = forgotPasswordRepository.sendVerificationEmail(email)

                result.fold(
                    onSuccess = { response ->
                        if (response.code == 200) {
                            Log.d(TAG, "Verification email sent successfully")
                            _state.value = ForgotPasswordState.EmailSent
                        } else {
                            Log.e(TAG, "Failed to send verification email: ${response.message}")
                            _state.value = ForgotPasswordState.Error(response.message)
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Exception sending verification email: ${exception.message}", exception)
                        _state.value = ForgotPasswordState.Error(exception.message ?: "Failed to connect to server. Please check your internet connection.")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during verification email: ${e.message}", e)
                _state.value = ForgotPasswordState.Error(e.message ?: "An unexpected error occurred. Please try again.")
            }
        }
    }

    /**
     * Verify OTP code
     */
    fun verifyOTP(email: String, otp: String) {
        if (otp.isBlank()) {
            _state.value = ForgotPasswordState.Error("OTP cannot be empty")
            return
        }

        _state.value = ForgotPasswordState.Loading

        viewModelScope.launch {
            try {
                val result = forgotPasswordRepository.verifyOTP(email, otp)

                result.fold(
                    onSuccess = { response ->
                        if (response.code == 200) {
                            Log.d(TAG, "OTP verified successfully")
                            _state.value = ForgotPasswordState.OTPVerified
                        } else {
                            Log.e(TAG, "Failed to verify OTP: ${response.message}")
                            _state.value = ForgotPasswordState.Error(response.message)
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Exception verifying OTP: ${exception.message}", exception)
                        _state.value = ForgotPasswordState.Error(exception.message ?: "Failed to connect to server. Please check your internet connection.")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during OTP verification: ${e.message}", e)
                _state.value = ForgotPasswordState.Error(e.message ?: "An unexpected error occurred. Please try again.")
            }
        }
    }

    /**
     * Reset password with verified OTP
     */
    fun resetPassword(email: String, otp: String, newPassword: String) {
        if (newPassword.isBlank()) {
            _state.value = ForgotPasswordState.Error("Password cannot be empty")
            return
        }

        if (newPassword.length < 8) {
            _state.value = ForgotPasswordState.Error("Password must be at least 8 characters")
            return
        }

        _state.value = ForgotPasswordState.Loading

        viewModelScope.launch {
            try {
                val result = forgotPasswordRepository.resetPassword(email, otp, newPassword)

                result.fold(
                    onSuccess = { response ->
                        if (response.code == 200) {
                            Log.d(TAG, "Password reset successfully")
                            _state.value = ForgotPasswordState.PasswordReset
                        } else {
                            Log.e(TAG, "Failed to reset password: ${response.message}")
                            _state.value = ForgotPasswordState.Error(response.message)
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Exception resetting password: ${exception.message}", exception)
                        _state.value = ForgotPasswordState.Error(exception.message ?: "Failed to connect to server. Please check your internet connection.")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during password reset: ${e.message}", e)
                _state.value = ForgotPasswordState.Error(e.message ?: "An unexpected error occurred. Please try again.")
            }
        }
    }

    /**
     * Reset the state to Initial
     */
    fun resetState() {
        _state.value = ForgotPasswordState.Initial
    }

    /**
     * UI State for Forgot Password Flow
     */
    sealed class ForgotPasswordState {
        object Initial : ForgotPasswordState()
        object Loading : ForgotPasswordState()
        object EmailSent : ForgotPasswordState()
        object OTPVerified : ForgotPasswordState()
        object PasswordReset : ForgotPasswordState()
        data class Error(val message: String) : ForgotPasswordState()
    }

    /**
     * Factory for creating ForgotPasswordViewModel
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
                val repository = ForgotPasswordRepository(context)
                return ForgotPasswordViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
