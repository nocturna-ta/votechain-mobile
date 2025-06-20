package com.nocturna.votechain.viewmodel.wallet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.WalletData
import com.nocturna.votechain.data.storage.WalletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

data class WalletAccountUiState(
    val walletData: WalletData? = null,
    val formattedBalance: String = "0.0000 ETH",
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val showMnemonic: Triple<String, String, String>? = null, // address, privateKey, walletName
    val showPrivateKeyWarning: Boolean = false,
    val onPrivateKeyWarningConfirm: (() -> Unit)? = null
)

class WalletAccountViewModel(
    private val context: Context
) : ViewModel() {

    private val walletManager = WalletManager.getInstance(context)

    private val _uiState = MutableStateFlow(WalletAccountUiState())
    val uiState: StateFlow<WalletAccountUiState> = _uiState.asStateFlow()

    init {
        loadCurrentWallet()
    }

    private fun loadCurrentWallet() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Get the selected wallet address
                val selectedAddress = walletManager.getSelectedWalletAddress()
                    ?: walletManager.getDefaultWalletAddress()

                if (selectedAddress != null) {
                    // For demo purposes, we'll use a default PIN. In production, you'd need to handle PIN authentication
                    // or store the wallet data securely after initial authentication
                    val walletData = loadWalletWithStoredPin(selectedAddress)

                    if (walletData != null) {
                        val formattedBalance = formatBalance(walletData.balance)

                        _uiState.value = _uiState.value.copy(
                            walletData = walletData,
                            formattedBalance = formattedBalance,
                            isLoading = false
                        )

                        // Refresh balance periodically
                        refreshBalance()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load wallet data"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No wallet selected"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load wallet: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadWalletWithStoredPin(address: String): WalletData? {
        // In a real implementation, you'd need to handle PIN authentication properly
        // For now, this is a simplified version that assumes the wallet is already authenticated
        return try {
            // Try to get wallet info without decryption for display purposes
            val wallets = walletManager.getAllWallets()
            val walletInfo = wallets.find { it.address.equals(address, ignoreCase = true) }

            if (walletInfo != null) {
                val balance = walletManager.getWalletBalance(address)

                WalletData(
                    address = walletInfo.address,
                    privateKey = "*** PIN Required ***", // Don't store decrypted private key
                    publicKey = walletInfo.address,
                    balance = balance,
                    name = walletInfo.name,
                    mnemonic = walletInfo.mnemonic
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun refreshBalance() {
        viewModelScope.launch {
            try {
                val currentWallet = _uiState.value.walletData
                if (currentWallet != null) {
                    val newBalance = walletManager.getWalletBalance(currentWallet.address)
                    val formattedBalance = formatBalance(newBalance)

                    _uiState.value = _uiState.value.copy(
                        walletData = currentWallet.copy(balance = newBalance),
                        formattedBalance = formattedBalance
                    )
                }
            } catch (e: Exception) {
                // Handle balance refresh error silently
            }
        }
    }

    private fun formatBalance(balance: BigInteger): String {
        return try {
            val balanceEth = Convert.fromWei(balance.toString(), Convert.Unit.ETHER)
            "${balanceEth.setScale(4, BigDecimal.ROUND_DOWN)} ETH"
        } catch (e: Exception) {
            "0.0000 ETH"
        }
    }

    fun renameWallet(newName: String) {
        viewModelScope.launch {
            try {
                val currentWallet = _uiState.value.walletData
                if (currentWallet != null) {
                    val success = walletManager.updateWalletName(currentWallet.address, newName)
                    if (success) {
                        _uiState.value = _uiState.value.copy(
                            walletData = currentWallet.copy(name = newName)
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to rename wallet"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to rename wallet: ${e.message}"
                )
            }
        }
    }

    fun deleteWallet() {
        viewModelScope.launch {
            try {
                val currentWallet = _uiState.value.walletData
                if (currentWallet != null) {
                    val success = walletManager.deleteWallet(currentWallet.address)
                    if (success) {
                        _uiState.value = _uiState.value.copy(isDeleted = true)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to delete wallet"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete wallet: ${e.message}"
                )
            }
        }
    }

    fun showBackupPhrase() {
        val currentWallet = _uiState.value.walletData
        if (currentWallet != null) {
            // In production, you'd need to verify PIN again before showing mnemonic
            _uiState.value = _uiState.value.copy(
                showMnemonic = Triple(
                    currentWallet.address,
                    currentWallet.privateKey,
                    currentWallet.name
                )
            )
        }
    }

    fun showPrivateKeyWarning(onConfirm: () -> Unit) {
        _uiState.value = _uiState.value.copy(
            showPrivateKeyWarning = true,
            onPrivateKeyWarningConfirm = onConfirm
        )
    }

    fun hidePrivateKeyWarning() {
        _uiState.value = _uiState.value.copy(
            showPrivateKeyWarning = false,
            onPrivateKeyWarningConfirm = null
        )
    }

    fun clearMnemonicDisplay() {
        _uiState.value = _uiState.value.copy(showMnemonic = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WalletAccountViewModel::class.java)) {
                return WalletAccountViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}