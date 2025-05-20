package com.nocturna.votechain.viewmodel.register

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.blockchain.BlockchainManager
import com.nocturna.votechain.data.network.ApiResponse
import com.nocturna.votechain.data.network.Province
import com.nocturna.votechain.data.network.Regency
import com.nocturna.votechain.data.network.UserRegistrationData
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
 * ViewModel for the Register Screen
 */
class RegisterViewModel(
    private val userRepository: UserRepository,
    private val context: Context
) : ViewModel() {
    private val TAG = "RegisterViewModel"
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Initial)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    private val enhancedUserRepository = EnhancedUserRepository(context)

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
        checkRegistrationState()
    }

    /**
     * Check for any stored registration state
     */
    private fun checkRegistrationState() {
        val state = registrationStateManager.getRegistrationState()
        when (state) {
            RegistrationStateManager.STATE_WAITING -> {
                _uiState.value = RegisterUiState.Waiting
            }
            RegistrationStateManager.STATE_APPROVED -> {
                _uiState.value = RegisterUiState.Approved
            }
            RegistrationStateManager.STATE_REJECTED -> {
                _uiState.value = RegisterUiState.Rejected
            }
            else -> {
                // Initial state, do nothing
            }
        }
    }

    /**
     * Check connection to Ethereum node
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
     * Fetch provinces from wilayah.id API
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
     * Fetch regencies for a specific province from wilayah.id API
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
     * Register a new user with automatic voter address generation using BlockchainManager
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
        ktpFileUri: Uri,
        role: String = "voter"
    ) {
        _uiState.value = RegisterUiState.Loading
        Log.d(TAG, "Starting registration process with blockchain voter address generation")

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
            birthDate // Fallback to original format if parsing fails
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Using EnhancedUserRepository with new wallet generation")
                // Use EnhancedUserRepository with the test flag
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
     * Clear registration state when user successfully logs in
     */
    fun clearRegistrationState() {
        registrationStateManager.clearRegistrationState()
        _uiState.value = RegisterUiState.Initial
    }

    /**
     * Reset the UI state to initial
     */
    fun resetState() {
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
}