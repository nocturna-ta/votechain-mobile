package com.nocturna.votechain.viewmodel.register

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
    private val userRepository: UserRepository
) : ViewModel() {
    private val TAG = "RegisterViewModel"
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Initial)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // BlockchainManager for Ethereum operations
    private val blockchainManager = BlockchainManager

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
    }

    /**
     * Check connection to Ethereum node
     */
    private fun checkNodeConnection() {
        viewModelScope.launch {
            try {
                val isConnected = blockchainManager.isConnected()
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
        role: String = "voter",
        voterAddress: String
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
                // Generate Ethereum address using BlockchainManager
                val voterAddress = blockchainManager.generateAddress()
                Log.d(TAG, "Generated voter address: $voterAddress")

                // If connected to node, try to fund the address with a small amount (0.01 ETH)
                if (_nodeConnected.value) {
                    try {
                        withContext(Dispatchers.IO) {
                            blockchainManager.fundVoterAddress(voterAddress)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to fund voter address: ${e.message}", e)
                        // Continue with registration even if funding fails
                    }
                }

                Log.d(TAG, "Calling repository registerUser method with voter_address")
                val result = userRepository.registerUser(
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
                    voterAddress = voterAddress, // Pass the generated address
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

    override fun onCleared() {
        super.onCleared()
        // No need to shut down BlockchainManager here since it's a singleton
        // The app should handle shutdown in a more appropriate lifecycle
    }
}