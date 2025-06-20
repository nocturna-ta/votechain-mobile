package com.nocturna.votechain.viewmodel.register

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.UserRegistrationData
import com.nocturna.votechain.data.repository.EnhancedUserRepository
import com.nocturna.votechain.data.storage.WalletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.SecureRandom
import org.web3j.crypto.Keys
import org.web3j.crypto.ECKeyPair
import org.web3j.utils.Numeric

data class RegisterUiState(
    val isLoading: Boolean = false,
    val registrationResponse: ApiResponse<UserRegistrationData>? = null,
    val error: String? = null,
    val walletAddress: String? = null,
    val isWalletCreated: Boolean = false
)

class RegisterViewModel(
    private val context: Context,
    private val userRepository: EnhancedUserRepository,
    private val walletManager: WalletManager
) : ViewModel() {

    private val TAG = "RegisterViewModel"

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        email: String,
        password: String,
        nik: String,
        fullName: String,
        gender: String,
        birthPlace: String,
        birthDate: String,
        residentialAddress: String,
        region: String,
        ktpFile: Uri?
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                Log.d(TAG, "Starting registration process for email: $email")

                // Step 1: Generate wallet keypair
                val keyPair = generateWalletKeyPair()
                val walletAddress = "0x" + Keys.getAddress(keyPair)

                Log.d(TAG, "Generated wallet address: $walletAddress")

                // Step 2: Register user with enhanced repository (includes wallet creation)
                val result = userRepository.registerWithVoterAddress(
                    email = email,
                    password = password,
                    nik = nik,
                    fullName = fullName,
                    gender = gender,
                    birthPlace = birthPlace,
                    birthDate = birthDate,
                    residentialAddress = residentialAddress,
                    region = region,
                    role = "voter",
                    ktpFileUri = ktpFile
                )

                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "Registration successful: ${response.message}")

                        // Update UI state with success
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            registrationResponse = response,
                            walletAddress = walletAddress,
                            isWalletCreated = true,
                            error = null
                        )

                        Log.d(TAG, "Registration and wallet creation completed successfully")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Registration failed", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Registration failed"
                        )
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "Registration error", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun generateWalletKeyPair(): ECKeyPair {
        val random = SecureRandom()
        val privateKey = ByteArray(32)
        random.nextBytes(privateKey)

        val privateKeyBigInt = Numeric.toBigInt(privateKey)
        return ECKeyPair.create(privateKeyBigInt)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearRegistrationResponse() {
        _uiState.value = _uiState.value.copy(registrationResponse = null)
    }

    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                val userRepository = EnhancedUserRepository(context)
                val walletManager = WalletManager.getInstance(context)
                return RegisterViewModel(context, userRepository, walletManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}