package com.nocturna.votechain.viewmodel.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.CompleteUserData
import com.nocturna.votechain.data.model.UserLoginData
import com.nocturna.votechain.data.network.NetworkClient.apiService
import com.nocturna.votechain.data.repository.IntegratedEnhancedUserRepository
import com.nocturna.votechain.data.repository.RegistrationStateManager
import com.nocturna.votechain.data.repository.UserLoginRepository
import com.nocturna.votechain.data.repository.VoterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Login Screen
 */
class LoginViewModel(
    private val userLoginRepository: UserLoginRepository,
    private val integratedUserRepository: IntegratedEnhancedUserRepository,
    private val voterRepository: VoterRepository,
    private val context: Context
) : ViewModel() {
    private val TAG = "LoginViewModel"

    // UI State
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Complete User Data State
    private val _completeUserData = MutableStateFlow<CompleteUserData?>(null)
    val completeUserData: StateFlow<CompleteUserData?> = _completeUserData.asStateFlow()

    // Session State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Check initial login state
    init {
        checkLoginState()
    }

    /**
     * UI State for Login Screen
     */
    sealed class LoginUiState {
        data object Initial : LoginUiState()
        data object Loading : LoginUiState()
        data class Success(val userData: CompleteUserData) : LoginUiState()
        data class Error(val message: String) : LoginUiState()
        data object NavigateToHome : LoginUiState()

        // User data states
        data class PartialSuccess(val userData: CompleteUserData, val message: String) : LoginUiState()
        data class RefreshError(val userData: CompleteUserData, val message: String) : LoginUiState()
        data object LoggedOut : LoginUiState()

        // New states for registration status
        data object NavigateToWaiting : LoginUiState()
        data object NavigateToAccepted : LoginUiState()
        data object NavigateToRejected : LoginUiState()
    }

    // Add registration state manager
    private val registrationStateManager = RegistrationStateManager(context)

    /**
     * Login user with email and password
     */
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = LoginUiState.Loading

                Log.d(TAG, "Starting enhanced login for: $email")

                // Step 1: Authenticate user
                val loginResult = userLoginRepository.loginUser(email, password)

                loginResult.fold(
                    onSuccess = { loginResponse ->
                        Log.d(TAG, "Login successful, checking registration status...")

                        // Step 2: Check registration status after successful login
                        checkRegistrationStatusAfterLogin(email, loginResponse.data)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Login failed: ${error.message}")
                        _uiState.value = LoginUiState.Error(error.message ?: "Login failed")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Login exception: ${e.message}", e)
                _uiState.value = LoginUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Check registration status after successful login
     */
    private suspend fun checkRegistrationStatusAfterLogin(email: String, loginData: UserLoginData?) {
        try {
            // Always check with API first to get the most up-to-date status
            val response = withContext(Dispatchers.IO) {
                apiService.getVerificationStatus(email)
            }

            if (response.isSuccessful && response.body() != null) {
                val verificationData = response.body()!!.data
                if (verificationData != null) {
                    val status = verificationData.verification_status.lowercase()
                    Log.d(TAG, "API verification status for $email: $status")

                    when (status) {
                        "pending", "waiting" -> {
                            registrationStateManager.saveRegistrationState(
                                RegistrationStateManager.STATE_WAITING, email, ""
                            )
                            _uiState.value = LoginUiState.NavigateToWaiting
                        }
                        "approved", "accepted" -> {
                            // Clear waiting state since they're approved now
                            registrationStateManager.saveRegistrationState(
                                RegistrationStateManager.STATE_APPROVED, email, ""
                            )
                            // Proceed to home instead of showing the accepted screen again
                            proceedToHome(loginData)
                        }
                        "denied", "rejected" -> {
                            registrationStateManager.saveRegistrationState(
                                RegistrationStateManager.STATE_REJECTED, email, ""
                            )
                            _uiState.value = LoginUiState.NavigateToRejected
                        }
                        else -> {
                            // User is approved/no pending registration - proceed to home
                            // Clear any previous registration state
                            registrationStateManager.saveRegistrationState(
                                RegistrationStateManager.STATE_NONE, email, ""
                            )
                            proceedToHome(loginData)
                        }
                    }
                } else {
                    // No verification data found - proceed to home
                    proceedToHome(loginData)
                }
            } else {
                // API call failed - fall back to local state
                fallbackToLocalState(email, loginData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking registration status: ${e.message}")
            // On error, fall back to local state
            fallbackToLocalState(email, loginData)
        }
    }

    /**
     * Fall back to local registration state when API call fails
     */
    private suspend fun fallbackToLocalState(email: String, loginData: UserLoginData?) {
        val localState = registrationStateManager.getRegistrationState()
        val savedEmail = registrationStateManager.getSavedEmail()

        Log.d(TAG, "Falling back to local state: $localState for email: $savedEmail")

        if (localState != RegistrationStateManager.STATE_NONE && savedEmail == email) {
            handleRegistrationState(localState, email)
        } else {
            // No local state or mismatch email - proceed to home (fallback)
            proceedToHome(loginData)
        }
    }

    /**
     * Handle registration state after login
     */
    private suspend fun handleRegistrationState(state: Int, email: String) {
        when (state) {
            RegistrationStateManager.STATE_WAITING -> {
                Log.d(TAG, "User has waiting registration - navigating to waiting screen")
                _uiState.value = LoginUiState.NavigateToWaiting
            }
            RegistrationStateManager.STATE_APPROVED -> {
                Log.d(TAG, "User has approved registration - navigating to accepted screen")
                _uiState.value = LoginUiState.NavigateToAccepted
            }
            RegistrationStateManager.STATE_REJECTED -> {
                Log.d(TAG, "User has rejected registration - navigating to rejected screen")
                _uiState.value = LoginUiState.NavigateToRejected
            }
            else -> {
                // No pending registration - proceed to home
                proceedToHome(null)
            }
        }
    }

    /**
     * Proceed to home screen after successful login
     */
    private suspend fun proceedToHome(loginData: UserLoginData?) {
        try {
            // Load complete user data
            loadCompleteUserData()

            // Set navigation state
            _uiState.value = LoginUiState.NavigateToHome
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user data: ${e.message}")
            _uiState.value = LoginUiState.Error("Failed to load user data")
        }
    }

    /**
     * Load complete user data after successful authentication
     */
    private suspend fun loadCompleteUserData() {
        try {
            Log.d(TAG, "Loading complete user data...")

            // Load complete user data with wallet info
            val completeData = integratedUserRepository.getCompleteUserData()

            _completeUserData.value = completeData
            _isLoggedIn.value = true

            // Check if we have all required data
            val hasRequiredData = completeData.voterData != null &&
                    completeData.walletInfo.voterAddress.isNotEmpty()

            if (hasRequiredData) {
                _uiState.value = LoginUiState.Success(completeData)
                Log.d(TAG, "✅ Login completed with complete user data")

                // Log success details (without sensitive info)
                Log.d(TAG, "User data loaded:")
                Log.d(TAG, "- Voter: ${completeData.voterData?.full_name}")
                Log.d(TAG, "- Balance: ${completeData.walletInfo.balance} ETH")
                Log.d(TAG, "- Address: ${completeData.walletInfo.voterAddress.take(10)}...")
            } else {
                Log.w(TAG, "Login successful but missing some user data")
                _uiState.value = LoginUiState.PartialSuccess(completeData, "Some account data may be incomplete")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading complete user data: ${e.message}", e)
            _uiState.value = LoginUiState.Error("Login successful but failed to load account data: ${e.message}")
        }
    }

    /**
     * Check current login state and load data if already logged in
     */
    fun checkLoginState() {
        viewModelScope.launch {
            try {
                val isSessionValid = userLoginRepository.isSessionValid()

                if (isSessionValid) {
                    Log.d(TAG, "Valid session found, loading user data...")
                    _uiState.value = LoginUiState.Loading
                    loadCompleteUserData()
                } else {
                    Log.d(TAG, "No valid session found")
                    _uiState.value = LoginUiState.Initial
                    _isLoggedIn.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking login state: ${e.message}", e)
                _uiState.value = LoginUiState.Initial
                _isLoggedIn.value = false
            }
        }
    }

    /**
     * Refresh user data (for pull-to-refresh functionality)
     */
    fun refreshUserData() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Refreshing user data...")

                val refreshedData = integratedUserRepository.refreshCompleteUserData()

                refreshedData.fold(
                    onSuccess = { data ->
                        _completeUserData.value = data
                        _uiState.value = LoginUiState.Success(data)
                        Log.d(TAG, "✅ User data refreshed successfully")
                    },
                    onFailure = { error ->
                        Log.w(TAG, "Failed to refresh user data: ${error.message}")
                        // Keep existing data but show warning
                        _uiState.value = LoginUiState.RefreshError(
                            _completeUserData.value ?: CompleteUserData(),
                            "Failed to refresh: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing user data: ${e.message}", e)
            }
        }
    }

    /**
     * Refresh only wallet balance
     */
    fun refreshBalance() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Refreshing balance...")

                val newBalance = voterRepository.refreshBalance()

                // Update the wallet info in complete user data
                _completeUserData.value?.let { currentData ->
                    val updatedWalletInfo = currentData.walletInfo.copy(
                        balance = newBalance,
                        lastUpdated = System.currentTimeMillis(),
                        hasError = false,
                        errorMessage = ""
                    )
                    val updatedData = currentData.copy(walletInfo = updatedWalletInfo)
                    _completeUserData.value = updatedData

                    Log.d(TAG, "✅ Balance refreshed: $newBalance ETH")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing balance: ${e.message}", e)

                // Update wallet info with error
                _completeUserData.value?.let { currentData ->
                    val errorWalletInfo = currentData.walletInfo.copy(
                        hasError = true,
                        errorMessage = e.message ?: "Failed to refresh balance"
                    )
                    val updatedData = currentData.copy(walletInfo = errorWalletInfo)
                    _completeUserData.value = updatedData
                }
            }
        }
    }

    /**
     * Get current user session info
     */
    fun getUserSession() = userLoginRepository.getCompleteUserSession()

    /**
     * Check if blockchain is connected
     */
    fun checkBlockchainConnection() {
        viewModelScope.launch {
            try {
                val isConnected = integratedUserRepository.checkBlockchainConnection()
                Log.d(TAG, "Blockchain connection status: $isConnected")

                // Update wallet info with connection status
                _completeUserData.value?.let { currentData ->
                    val updatedWalletInfo = currentData.walletInfo.copy(
                        hasError = !isConnected,
                        errorMessage = if (!isConnected) "Blockchain not connected" else ""
                    )
                    val updatedData = currentData.copy(walletInfo = updatedWalletInfo)
                    _completeUserData.value = updatedData
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking blockchain connection: ${e.message}", e)
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Initial
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
     * Reset login state (used when navigating away from login result screens)
     */
    fun resetLoginState() {
        _uiState.value = LoginUiState.Initial
    }

    /**
     * Log out the current user
     */
    fun logoutUser() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting enhanced logout...")

                // Clear all user data
                integratedUserRepository.clearAllUserData()

                // Reset state
                _completeUserData.value = null
                _isLoggedIn.value = false
                _uiState.value = LoginUiState.LoggedOut

                Log.d(TAG, "✅ Enhanced logout completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout: ${e.message}", e)
                // Force logout even if there's an error
                _completeUserData.value = null
                _isLoggedIn.value = false
                _uiState.value = LoginUiState.LoggedOut
            }
        }
    }

    /**
     * Factory for creating LoginViewModel
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                val userLoginRepository = UserLoginRepository(context)
                val integratedUserRepository = IntegratedEnhancedUserRepository(context)
                val voterRepository = VoterRepository(context)

                return LoginViewModel(
                    userLoginRepository,
                    integratedUserRepository,
                    voterRepository,
                    context
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}