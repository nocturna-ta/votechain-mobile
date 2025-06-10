package com.nocturna.votechain.viewmodel.register

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.blockchain.BlockchainManager
import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.Province
import com.nocturna.votechain.data.model.Regency
import com.nocturna.votechain.data.model.UserRegistrationData
import com.nocturna.votechain.data.model.VerificationStatusData
import com.nocturna.votechain.data.network.NetworkClient
import com.nocturna.votechain.data.network.WilayahApiClient
import com.nocturna.votechain.data.repository.EnhancedUserRepository
import com.nocturna.votechain.data.repository.RegistrationStateManager
import com.nocturna.votechain.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel for the Register Screen with verification status checking
 */
class RegisterViewModel(
    private val userRepository: UserRepository,
    private val context: Context
) : ViewModel() {
    private val TAG = "RegisterViewModel"
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Initial)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    private val enhancedUserRepository = EnhancedUserRepository(context)

    // API service for verification status checking
    private val apiService = NetworkClient.apiService

    // Registration state manager
    private val registrationStateManager = RegistrationStateManager(context)

    // Node connection state
    private val _nodeConnected = MutableStateFlow(false)
    val nodeConnected: StateFlow<Boolean> = _nodeConnected.asStateFlow()

    // States for province and regency data
    private val _provinces = MutableStateFlow<List<Province>>(emptyList())
    val provinces: StateFlow<List<Province>> = _provinces.asStateFlow()

    private val _regencies = MutableStateFlow<List<Regency>>(emptyList())
    val regencies: StateFlow<List<Regency>> = _regencies.asStateFlow()

    private val _isProvincesLoading = MutableStateFlow(false)
    val isProvincesLoading: StateFlow<Boolean> = _isProvincesLoading.asStateFlow()

    private val _isRegenciesLoading = MutableStateFlow(false)
    val isRegenciesLoading: StateFlow<Boolean> = _isRegenciesLoading.asStateFlow()

    init {
        // Check blockchain connection on init
        checkNodeConnection()
        // Initialize by fetching provinces
        fetchProvinces()
        // Check initial registration state
        checkInitialRegistrationState()
    }

    /**
     * Check for any stored registration state when ViewModel is created
     * This ensures persistence across app restarts
     */
    private fun checkInitialRegistrationState() {
        val state = registrationStateManager.getRegistrationState()
        Log.d(TAG, "Checking initial registration state: $state")

        when (state) {
            RegistrationStateManager.STATE_WAITING -> {
                Log.d(TAG, "Found waiting registration state")
                _uiState.value = RegisterUiState.Waiting
                // Auto-check verification status for waiting state
                val email = registrationStateManager.getSavedEmail()
                if (email.isNotEmpty()) {
                    checkVerificationStatus(email)
                }
            }
            RegistrationStateManager.STATE_APPROVED -> {
                Log.d(TAG, "Found approved registration state")
                _uiState.value = RegisterUiState.Approved
            }
            RegistrationStateManager.STATE_REJECTED -> {
                Log.d(TAG, "Found rejected registration state - allowing new registration")
                // Clear rejected state to allow new registration
                registrationStateManager.clearRegistrationState()
                _uiState.value = RegisterUiState.Initial
            }
            else -> {
                Log.d(TAG, "No stored registration state found")
                _uiState.value = RegisterUiState.Initial
            }
        }
    }

    /**
     * Check if user already has a registration and redirect accordingly
     * Called when user clicks register button or tries to access register screen
     */
    fun checkExistingRegistration(email: String) {
        val currentState = registrationStateManager.getRegistrationState()
        val savedEmail = registrationStateManager.getSavedEmail()

        if (currentState != RegistrationStateManager.STATE_NONE && savedEmail == email) {
            Log.d(TAG, "User already has registration with state: $currentState")
            when (currentState) {
                RegistrationStateManager.STATE_WAITING -> {
                    _uiState.value = RegisterUiState.Waiting
                    checkVerificationStatus(email)
                }
                RegistrationStateManager.STATE_APPROVED -> {
                    _uiState.value = RegisterUiState.Approved
                }
                RegistrationStateManager.STATE_REJECTED -> {
                    // Allow new registration for rejected state
                    registrationStateManager.clearRegistrationState()
                    _uiState.value = RegisterUiState.Initial
                }
            }
        } else {
            // Check verification status from API for new email
            checkVerificationStatus(email)
        }
    }

    /**
     * Check verification status from API
     */
    fun checkVerificationStatus(email: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Checking verification status for email: $email")

                val response = withContext(Dispatchers.IO) {
                    apiService.getVerificationStatus(email)
                }

                if (response.isSuccessful && response.body() != null) {
                    val verificationData = response.body()!!.data
                    if (verificationData != null) {
                        handleVerificationStatusResponse(verificationData, email)
                    } else {
                        Log.d(TAG, "No verification data found for email: $email")
                        // Allow new registration if no existing data
                        if (_uiState.value == RegisterUiState.Waiting) {
                            // Keep current state if we're checking from waiting screen
                        } else {
                            _uiState.value = RegisterUiState.Initial
                        }
                    }
                } else {
                    Log.d(TAG, "No verification status found for email: $email")
                    // Allow new registration if no existing data
                    if (_uiState.value == RegisterUiState.Waiting) {
                        // Keep current state if we're checking from waiting screen
                    } else {
                        _uiState.value = RegisterUiState.Initial
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error checking verification status: ${e.message}")
                // Allow new registration on error
                if (_uiState.value == RegisterUiState.Waiting) {
                    // Keep current state if we're checking from waiting screen
                } else {
                    _uiState.value = RegisterUiState.Initial
                }
            }
        }
    }

    /**
     * Handle verification status response and update states accordingly
     */
    private fun handleVerificationStatusResponse(verificationData: VerificationStatusData, email: String) {
        val status = verificationData.verification_status.lowercase()
        Log.d(TAG, "Verification status for $email: $status")

        when (status) {
            "pending", "waiting" -> {
                registrationStateManager.saveRegistrationState(
                    RegistrationStateManager.STATE_WAITING,
                    email,
                    "" // NIK not available from this response
                )
                _uiState.value = RegisterUiState.Waiting
            }
            "approved", "accepted" -> {
                registrationStateManager.saveRegistrationState(
                    RegistrationStateManager.STATE_APPROVED,
                    email,
                    ""
                )
                _uiState.value = RegisterUiState.Approved
            }
            "denied", "rejected" -> {
                registrationStateManager.saveRegistrationState(
                    RegistrationStateManager.STATE_REJECTED,
                    email,
                    ""
                )
                _uiState.value = RegisterUiState.Rejected
            }
            else -> {
                Log.d(TAG, "Unknown verification status: $status")
                _uiState.value = RegisterUiState.Initial
            }
        }
    }

    /**
     * Handle close action from waiting screen
     */
    fun onWaitingScreenClose() {
        Log.d(TAG, "Waiting screen closed, maintaining registration state")
        // Don't clear the registration state, just set flag for navigation
        _uiState.value = RegisterUiState.NavigateToLogin
    }

    /**
     * Check if user has any pending registration state
     */
    fun getCurrentRegistrationState(): Int {
        return registrationStateManager.getRegistrationState()
    }

    /**
     * Check blockchain node connection
     */
    private fun checkNodeConnection() {
        viewModelScope.launch {
            try {
                val isConnected = withContext(Dispatchers.IO) {
                    BlockchainManager.isConnected()
                }
                _nodeConnected.value = isConnected
                Log.d(TAG, "Ethereum node connection: ${if (isConnected) "CONNECTED" else "DISCONNECTED"}")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking node connection: ${e.message}", e)
                _nodeConnected.value = false
            }
        }
    }

    /**
     * Fetch provinces data
     */
    fun fetchProvinces() {
        viewModelScope.launch {
            _isProvincesLoading.value = true
            try {
                val response = WilayahApiClient.apiService.getProvinces()
                _provinces.value = response.data
                Log.d(TAG, "Fetched ${response.data.size} provinces")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching provinces: ${e.message}", e)
            } finally {
                _isProvincesLoading.value = false
            }
        }
    }

    /**
     * Fetch regencies for selected province
     */
    fun fetchRegencies(provinceCode: String) {
        viewModelScope.launch {
            _isRegenciesLoading.value = true
            try {
                val response = WilayahApiClient.apiService.getRegencies(provinceCode)
                _regencies.value = response.data
                Log.d(TAG, "Fetched ${response.data.size} regencies for province $provinceCode")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching regencies: ${e.message}", e)
            } finally {
                _isRegenciesLoading.value = false
            }
        }
    }

    /**
     * Register a new user
     */
    fun registerUser(
        email: String,
        password: String,
        nationalId: String,
        fullName: String,
        gender: String,
        birthPlace: String,
        birthDate: String,
        address: String,
        region: String,
        role: String = "voter",
        ktpFileUri: Uri?
    ) {
        _uiState.value = RegisterUiState.Loading

        // Format birth date to YYYY-MM-DD
        val formattedBirthDate = try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(birthDate)
            outputFormat.format(date ?: throw Exception("Invalid date"))
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting birth date: ${e.message}", e)
            birthDate // Fallback to original format if parsing fails
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Using EnhancedUserRepository with new wallet generation")

                // Save registration as waiting state immediately
                registrationStateManager.saveRegistrationState(
                    RegistrationStateManager.STATE_WAITING,
                    email,
                    nationalId
                )

                val result = enhancedUserRepository.registerWithVoterAddress(
                    email = email,
                    password = password,
                    nik = nationalId,
                    fullName = fullName,
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
                        Log.d(TAG, "Registration API call successful: ${response.message}")

                        // Parse the verification status and save appropriate state
                        val verificationStatus = response.data?.verification_status?.lowercase()
                        Log.d(TAG, "Verification status received: $verificationStatus")

                        when (verificationStatus) {
                            "pending", "waiting" -> {
                                registrationStateManager.saveRegistrationState(
                                    RegistrationStateManager.STATE_WAITING,
                                    email,
                                    nationalId
                                )
                                _uiState.value = RegisterUiState.Waiting
                            }
                            "accepted", "approved" -> {
                                registrationStateManager.saveRegistrationState(
                                    RegistrationStateManager.STATE_APPROVED,
                                    email,
                                    nationalId
                                )
                                _uiState.value = RegisterUiState.Approved
                            }
                            "rejected", "denied" -> {
                                registrationStateManager.saveRegistrationState(
                                    RegistrationStateManager.STATE_REJECTED,
                                    email,
                                    nationalId
                                )
                                _uiState.value = RegisterUiState.Rejected
                            }
                            else -> {
                                // Default to waiting if status is unclear
                                registrationStateManager.saveRegistrationState(
                                    RegistrationStateManager.STATE_WAITING,
                                    email,
                                    nationalId
                                )
                                _uiState.value = RegisterUiState.Waiting
                            }
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Registration failed: ${exception.message}", exception)
                        // Clear the waiting state since registration failed
                        registrationStateManager.clearRegistrationState()
                        _uiState.value = RegisterUiState.Error(exception.message ?: "Unknown error occurred")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during registration: ${e.message}", e)
                // Clear the waiting state since registration failed
                registrationStateManager.clearRegistrationState()
                _uiState.value = RegisterUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Clear registration state when user successfully completes the process
     * This should be called when user successfully logs in after approval
     */
    fun clearRegistrationState() {
        Log.d(TAG, "Clearing registration state")
        registrationStateManager.clearRegistrationState()
        _uiState.value = RegisterUiState.Initial
    }

    /**
     * Handle retry registration - only allowed for rejected registrations
     */
    fun retryRegistration() {
        val currentState = registrationStateManager.getRegistrationState()
        if (currentState == RegistrationStateManager.STATE_REJECTED) {
            Log.d(TAG, "Retrying registration after rejection")
            registrationStateManager.clearRegistrationState()
            _uiState.value = RegisterUiState.Initial
        } else {
            Log.w(TAG, "Retry registration not allowed for current state: $currentState")
        }
    }

    /**
     * Reset the UI state to initial
     */
    fun resetState() {
        _uiState.value = RegisterUiState.Initial
    }

    /**
     * For testing/admin purposes - force clear registration state
     */
    fun forceClearRegistrationState() {
        Log.d(TAG, "Force clearing registration state")
        registrationStateManager.clearRegistrationState()
        _uiState.value = RegisterUiState.Initial
    }

    /**
     * UI State for Register Screen with additional states
     */
    sealed class RegisterUiState {
        data object Initial : RegisterUiState()
        data object Loading : RegisterUiState()
        data class Success(val data: ApiResponse<UserRegistrationData>) : RegisterUiState()
        data class Error(val message: String) : RegisterUiState()
        data object Waiting : RegisterUiState()
        data object Approved : RegisterUiState()
        data object Rejected : RegisterUiState()
        data object NavigateToLogin : RegisterUiState()
    }

    /**
     * Factory for creating RegisterViewModel
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                val userRepository = UserRepository(context)
                return RegisterViewModel(userRepository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    /**
     * Enhanced registerUserWithVoterAddress method that checks existing registration first
     * This method will be called by RegisterScreen
     */
    fun registerUserWithVoterAddress(
        nationalId: String,
        fullName: String,
        email: String,
        password: String,
        birthPlace: String,
        birthDate: String,
        address: String,
        region: String,
        gender: String,
        ktpFileUri: Uri?,
        role: String = "voter"
    ) {
        // First check if user already has a registration
        Log.d(TAG, "Starting registration process for email: $email")

        // Check existing registration status first
        checkExistingRegistrationBeforeRegister(email) { shouldProceed ->
            if (shouldProceed) {
                // Proceed with new registration
                proceedWithNewRegistration(
                    nationalId = nationalId,
                    fullName = fullName,
                    email = email,
                    password = password,
                    birthPlace = birthPlace,
                    birthDate = birthDate,
                    address = address,
                    region = region,
                    gender = gender,
                    ktpFileUri = ktpFileUri,
                    role = role
                )
            }
            // If shouldProceed is false, the user will be redirected to appropriate status screen
            // by the existing registration checking logic
        }
    }

    /**
     * Check existing registration before proceeding with new registration
     */
    private fun checkExistingRegistrationBeforeRegister(email: String, onResult: (Boolean) -> Unit) {
        val currentState = registrationStateManager.getRegistrationState()
        val savedEmail = registrationStateManager.getSavedEmail()

        if (currentState != RegistrationStateManager.STATE_NONE && savedEmail == email) {
            Log.d(TAG, "User already has registration with state: $currentState")
            when (currentState) {
                RegistrationStateManager.STATE_WAITING -> {
                    _uiState.value = RegisterUiState.Waiting
                    checkVerificationStatus(email)
                    onResult(false) // Don't proceed with new registration
                }
                RegistrationStateManager.STATE_APPROVED -> {
                    _uiState.value = RegisterUiState.Approved
                    onResult(false) // Don't proceed with new registration
                }
                RegistrationStateManager.STATE_REJECTED -> {
                    // Allow new registration for rejected state
                    registrationStateManager.clearRegistrationState()
                    onResult(true) // Proceed with new registration
                }
                else -> {
                    onResult(true) // Proceed with new registration
                }
            }
        } else {
            // Check verification status from API for potentially different email
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Checking verification status for email: $email")

                    val response = withContext(Dispatchers.IO) {
                        apiService.getVerificationStatus(email)
                    }

                    if (response.isSuccessful && response.body() != null) {
                        val verificationData = response.body()!!.data
                        if (verificationData != null) {
                            handleVerificationStatusResponse(verificationData, email)
                            onResult(false) // Don't proceed with new registration
                        } else {
                            Log.d(TAG, "No verification data found for email: $email")
                            onResult(true) // Proceed with new registration
                        }
                    } else {
                        Log.d(TAG, "No verification status found for email: $email")
                        onResult(true) // Proceed with new registration
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Error checking verification status: ${e.message}")
                    onResult(true) // Proceed with new registration on error
                }
            }
        }
    }

    /**
     * Proceed with new registration (existing logic)
     */
    private fun proceedWithNewRegistration(
        nationalId: String,
        fullName: String,
        email: String,
        password: String,
        birthPlace: String,
        birthDate: String,
        address: String,
        region: String,
        gender: String,
        ktpFileUri: Uri?,
        role: String
    ) {
        // This is the existing registration logic
        // Copy your existing registerUser implementation here

        _uiState.value = RegisterUiState.Loading

        // Format birth date to YYYY-MM-DD
        val formattedBirthDate = try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(birthDate)
            outputFormat.format(date ?: throw Exception("Invalid date"))
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting birth date: ${e.message}", e)
            birthDate // Fallback to original format if parsing fails
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Using EnhancedUserRepository with new wallet generation")

                // Save registration as waiting state immediately
                registrationStateManager.saveRegistrationState(
                    RegistrationStateManager.STATE_WAITING,
                    email,
                    nationalId
                )

                val result = enhancedUserRepository.registerWithVoterAddress(
                    email = email,
                    password = password,
                    nik = nationalId,
                    fullName = fullName,
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
                        Log.d(TAG, "Registration API call successful: ${response.message}")

                        // Parse the verification status and save appropriate state
                        val verificationStatus = response.data?.verification_status?.lowercase()
                        Log.d(TAG, "Verification status received: $verificationStatus")

                        when (verificationStatus) {
                            "pending", "waiting" -> {
                                registrationStateManager.saveRegistrationState(
                                    RegistrationStateManager.STATE_WAITING,
                                    email,
                                    nationalId
                                )
                                _uiState.value = RegisterUiState.Waiting
                            }
                            "accepted", "approved" -> {
                                registrationStateManager.saveRegistrationState(
                                    RegistrationStateManager.STATE_APPROVED,
                                    email,
                                    nationalId
                                )
                                _uiState.value = RegisterUiState.Approved
                            }
                            "rejected", "denied" -> {
                                registrationStateManager.saveRegistrationState(
                                    RegistrationStateManager.STATE_REJECTED,
                                    email,
                                    nationalId
                                )
                                _uiState.value = RegisterUiState.Rejected
                            }
                            else -> {
                                // Default to waiting if status is unclear
                                registrationStateManager.saveRegistrationState(
                                    RegistrationStateManager.STATE_WAITING,
                                    email,
                                    nationalId
                                )
                                _uiState.value = RegisterUiState.Waiting
                            }
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Registration failed: ${exception.message}", exception)
                        // Clear the waiting state since registration failed
                        registrationStateManager.clearRegistrationState()
                        _uiState.value = RegisterUiState.Error(exception.message ?: "Unknown error occurred")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during registration: ${e.message}", e)
                // Clear the waiting state since registration failed
                registrationStateManager.clearRegistrationState()
                _uiState.value = RegisterUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}