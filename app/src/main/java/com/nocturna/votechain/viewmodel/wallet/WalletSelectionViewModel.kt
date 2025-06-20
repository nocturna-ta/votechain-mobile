package com.nocturna.votechain.viewmodel.wallet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.storage.WalletManager
import com.nocturna.votechain.ui.screens.wallet.WalletDisplayInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.web3j.utils.Convert

data class WalletSelectionUiState(
    val wallets: List<WalletDisplayInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isWalletSelected: Boolean = false,
    val showMnemonic: Triple<String, String, String>? = null // address, privateKey, walletName
)

class WalletSelectionViewModel(
    private val context: Context,
    private val pin: String
) : ViewModel() {

    private val walletManager = WalletManager.getInstance(context)

    private val _uiState = MutableStateFlow(WalletSelectionUiState())
    val uiState: StateFlow<WalletSelectionUiState> = _uiState.asStateFlow()

    init {
        loadWallets()
    }

    private fun loadWallets() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val wallets = walletManager.getAllWallets()
                val walletsWithBalance = wallets.map { wallet ->
                    WalletDisplayInfo(
                        address = wallet.address,
                        name = wallet.name,
                        balance = "Loading...",
                        isDefault = wallet.isDefault
                    )
                }

                _uiState.value = _uiState.value.copy(
                    wallets = walletsWithBalance,
                    isLoading = false
                )

                // Load balances asynchronously
                wallets.forEach { wallet ->
                    launch {
                        try {
                            val balance = walletManager.getWalletBalance(wallet.address)
                            val balanceEth = Convert.fromWei(balance.toString(), Convert.Unit.ETHER)

                            updateWalletBalance(
                                wallet.address,
                                "${balanceEth.setScale(6)} ETH"
                            )
                        } catch (e: Exception) {
                            updateWalletBalance(wallet.address, "Error")
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load wallets: ${e.message}"
                )
            }
        }
    }

    private fun updateWalletBalance(address: String, balance: String) {
        val updatedWallets = _uiState.value.wallets.map { wallet ->
            if (wallet.address.equals(address, ignoreCase = true)) {
                wallet.copy(balance = balance)
            } else {
                wallet
            }
        }
        _uiState.value = _uiState.value.copy(wallets = updatedWallets)
    }

    fun selectWallet(address: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val walletData = walletManager.loadWallet(address, pin)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isWalletSelected = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load wallet: ${e.message}"
                )
            }
        }
    }

    fun createNewWallet(walletName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val walletData = walletManager.createNewWallet(pin, walletName)

                // Show mnemonic for new wallet
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showMnemonic = Triple(
                        walletData.address,
                        walletData.privateKey,
                        walletData.name
                    )
                )

                // Refresh wallet list
                loadWallets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create wallet: ${e.message}"
                )
            }
        }
    }

    fun importWallet(privateKey: String, walletName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val walletData = walletManager.importWallet(privateKey, pin, walletName)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )

                // Refresh wallet list
                loadWallets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to import wallet: ${e.message}"
                )
            }
        }
    }

    fun importWalletFromMnemonic(mnemonic: String, walletName: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val walletData = walletManager.importWalletFromMnemonic(mnemonic, pin, walletName)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )

                // Refresh wallet list
                loadWallets()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to restore wallet: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearMnemonicDisplay() {
        _uiState.value = _uiState.value.copy(showMnemonic = null)
    }

    class Factory(
        private val context: Context,
        private val pin: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WalletSelectionViewModel::class.java)) {
                return WalletSelectionViewModel(context, pin) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}