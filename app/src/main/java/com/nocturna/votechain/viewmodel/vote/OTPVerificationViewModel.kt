package com.nocturna.votechain.viewmodel.vote

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.OTPData
import com.nocturna.votechain.data.model.VoterData
import com.nocturna.votechain.data.repository.OTPRepository
import com.nocturna.votechain.data.repository.VoterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * UI State for OTP Verification
 */
data class OTPVerificationUiState(
    val isLoading: Boolean = false,
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    val otpData: OTPData? = null,
    val voterData: VoterData? = null,
    val error: String? = null,
    val isVerificationSuccess: Boolean = false,
    val remainingAttempts: Int = 0,
    val timeRemainingSeconds: Int = 180
)

/**
 * ViewModel for handling OTP verification logic
 */
class OTPVerificationViewModel(
    private val context: Context,
    private val categoryId: String
) : ViewModel() {

    private val otpRepository = OTPRepository(context)
    private val voterRepository = VoterRepository(context)

    private val _uiState = MutableStateFlow(OTPVerificationUiState())
    val uiState: StateFlow<OTPVerificationUiState> = _uiState.asStateFlow()

    init {
        loadVoterDataAndGenerateOTP()
    }

    /**
     * Load voter data and automatically generate OTP
     */
    private fun loadVoterDataAndGenerateOTP() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Get stored voter data
                val voterData = getStoredVoterData()
                if (voterData != null) {
                    _uiState.value = _uiState.value.copy(voterData = voterData)
                    generateOTP()
                } else {
                    // If no stored data, try to fetch from API
                    fetchVoterDataAndGenerateOTP()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load voter data: ${e.message}"
                )
            }
        }
    }

    /**
     * Get stored voter data from SharedPreferences
     * Fix: Add this method since it doesn't exist in VoterRepository
     */
    private fun getStoredVoterData(): VoterData? {
        val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)

        val fullName = sharedPreferences.getString("voter_full_name", null)
        val nik = sharedPreferences.getString("voter_nik", null)
        val voterAddress = sharedPreferences.getString("voter_address", null)
        val userId = sharedPreferences.getString("user_id", null)

        return if (fullName != null && nik != null) {
            VoterData(
                id = userId ?: "",
                user_id = userId.toString(),
                full_name = fullName,
                nik = nik,
                voter_address = voterAddress ?: "",
                telephone = "085722663467", // Default phone number
                gender = sharedPreferences.getString("voter_gender", null) ?: "",
                birth_place = sharedPreferences.getString("voter_birth_place", null) ?: "",
                birth_date = sharedPreferences.getString("voter_birth_date", null) ?: "",
                residential_address = sharedPreferences.getString("voter_residential_address", null) ?: "",
                is_registered = sharedPreferences.getBoolean("voter_is_registered", false),
                has_voted = sharedPreferences.getBoolean("voter_has_voted", false),
                region = sharedPreferences.getString("voter_region", null) ?: ""
            )
        } else {
            null
        }
    }

    /**
     * Fetch voter data from API and generate OTP
     */
    private fun fetchVoterDataAndGenerateOTP() {
        viewModelScope.launch {
            val token = getStoredToken()
            if (token.isNullOrEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Authentication required. Please login again."
                )
                return@launch
            }

//            voterRepository.fetchVoterData(token)
//                .onEach { result: Result<VoterData> ->
//                result.fold(
//                    onSuccess = { voterData: VoterData ->
//                        _uiState.value = _uiState.value.copy(voterData = voterData)
//                        generateOTP()
//                    },
//                    onFailure = { e: Throwable ->
//                        _uiState.value = _uiState.value.copy(
//                            isLoading = false,
//                            error = "Failed to fetch voter data: ${e.message}"
//                        )
//                    }
//                )
//            }
//                .collect()
        }
    }

    /**
     * Generate OTP for voting verification
     */
    fun generateOTP() {
        val voterData = _uiState.value.voterData ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            otpRepository.generateVotingOTP(voterData, categoryId).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        response.data?.let { otpData ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                otpData = otpData,
                                remainingAttempts = otpData.remaining_attempts,
                                timeRemainingSeconds = otpData.time_remaining_seconds.toIntOrNull() ?: 180
                            )
                        } ?: run {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Failed to generate OTP: No data received"
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to generate OTP: ${e.message}"
                        )
                    }
                )
            }
        }
    }

    /**
     * Resend OTP
     */
    fun resendOTP() {
        val voterData = _uiState.value.voterData ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResending = true, error = null)

            otpRepository.resendVotingOTP(voterData, categoryId).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        response.data?.let { otpData ->
                            _uiState.value = _uiState.value.copy(
                                isResending = false,
                                otpData = otpData,
                                remainingAttempts = otpData.remaining_attempts,
                                timeRemainingSeconds = otpData.time_remaining_seconds.toIntOrNull() ?: 180
                            )
                        } ?: run {
                            _uiState.value = _uiState.value.copy(
                                isResending = false,
                                error = "Failed to resend OTP: No data received"
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isResending = false,
                            error = "Failed to resend OTP: ${e.message}"
                        )
                    }
                )
            }
        }
    }

    /**
     * Verify OTP code
     */
    fun verifyOTP(otpCode: String) {
        val voterData = _uiState.value.voterData ?: return

        if (otpCode.length != 4) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid 4-digit OTP")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true, error = null)

            otpRepository.verifyVotingOTP(voterData, otpCode).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        if (response.data?.is_valid == true) {
                            _uiState.value = _uiState.value.copy(
                                isVerifying = false,
                                isVerificationSuccess = true
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isVerifying = false,
                                error = response.data?.message ?: "Invalid OTP code",
                                remainingAttempts = _uiState.value.remainingAttempts - 1
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isVerifying = false,
                            error = "Verification failed: ${e.message}",
                            remainingAttempts = _uiState.value.remainingAttempts - 1
                        )
                    }
                )
            }
        }
    }

    /**
     * Update timer countdown
     */
    fun updateTimer(seconds: Int) {
        _uiState.value = _uiState.value.copy(timeRemainingSeconds = seconds)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Get stored authentication token
     */
    private fun getStoredToken(): String? {
        val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    /**
     * Factory for creating OTPVerificationViewModel with dependencies
     */
    class Factory(
        private val context: Context,
        private val categoryId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OTPVerificationViewModel::class.java)) {
                return OTPVerificationViewModel(context, categoryId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}