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

data class MnemonicUiState(
    val mnemonic: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class MnemonicViewModel(
    private val context: Context,
    private val walletAddress: String
) : ViewModel() {

    private val walletManager = WalletManager.getInstance(context)

    private val _uiState = MutableStateFlow(MnemonicUiState())
    val uiState: StateFlow<MnemonicUiState> = _uiState.asStateFlow()

    init {
        loadMnemonic()
    }

    private fun loadMnemonic() {
        viewModelScope.launch {
            try {
                // Get the wallet info to retrieve the mnemonic
                val wallets = walletManager.getAllWallets()
                val wallet = wallets.find { it.address.equals(walletAddress, ignoreCase = true) }

                if (wallet?.mnemonic != null) {
                    _uiState.value = _uiState.value.copy(
                        mnemonic = wallet.mnemonic,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        mnemonic = "Mnemonic not available for this wallet",
                        isLoading = false,
                        error = "This wallet was imported and doesn't have a mnemonic phrase"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load mnemonic: ${e.message}"
                )
            }
        }
    }

    class Factory(
        private val context: Context,
        private val walletAddress: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MnemonicViewModel::class.java)) {
                return MnemonicViewModel(context, walletAddress) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}