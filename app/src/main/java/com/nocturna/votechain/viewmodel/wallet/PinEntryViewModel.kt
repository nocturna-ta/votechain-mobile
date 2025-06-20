package com.nocturna.votechain.viewmodel.wallet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.storage.WalletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PinEntryUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val hasWallets: Boolean = false
)

class PinEntryViewModel(
    private val context: Context
) : ViewModel() {

    private val walletManager = WalletManager.getInstance(context)

    private val _uiState = MutableStateFlow(
        PinEntryUiState(hasWallets = walletManager.hasWallets())
    )
    val uiState: StateFlow<PinEntryUiState> = _uiState.asStateFlow()

    fun verifyPin(pin: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                if (walletManager.hasWallets()) {
                    // Try to load existing wallet
                    val wallet = walletManager.loadSelectedWallet(pin)
                    if (wallet != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Incorrect PIN"
                        )
                    }
                } else {
                    // Create new wallet
                    try {
                        walletManager.createNewWallet(pin, "My Wallet")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            hasWallets = true
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to create wallet: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Incorrect PIN"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PinEntryViewModel::class.java)) {
                return PinEntryViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}