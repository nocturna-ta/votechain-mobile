package com.nocturna.votechain.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nocturna.votechain.ui.screens.wallet.*

// Wallet Navigation Routes
object WalletRoutes {
    const val PIN_ENTRY = "pin_entry"
    const val WALLET_SELECTION = "wallet_selection/{pin}"
    const val WALLET_ACCOUNT = "wallet_account"
    const val MNEMONIC = "mnemonic/{address}/{privateKey}/{walletName}"

    fun walletSelection(pin: String) = "wallet_selection/$pin"
    fun mnemonic(address: String, privateKey: String, walletName: String) =
        "mnemonic/$address/$privateKey/$walletName"
}

@Composable
fun WalletNavigation(
    navController: NavHostController = rememberNavController(),
    onWalletSetupComplete: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = WalletRoutes.PIN_ENTRY
    ) {
        // PIN Entry Screen
        composable(WalletRoutes.PIN_ENTRY) {
            PinEntryScreen(
                onPinVerified = {
                    // Navigate to wallet account screen
                    navController.navigate(WalletRoutes.WALLET_ACCOUNT) {
                        popUpTo(WalletRoutes.PIN_ENTRY) { inclusive = true }
                    }
                    onWalletSetupComplete()
                },
                onWalletCreated = {
                    // Navigate to wallet account screen after creation
                    navController.navigate(WalletRoutes.WALLET_ACCOUNT) {
                        popUpTo(WalletRoutes.PIN_ENTRY) { inclusive = true }
                    }
                    onWalletSetupComplete()
                }
            )
        }

        // Wallet Selection Screen
        composable(WalletRoutes.WALLET_SELECTION) { backStackEntry ->
            val pin = backStackEntry.arguments?.getString("pin") ?: ""

            WalletSelectionScreen(
                pin = pin,
                onWalletSelected = {
                    navController.navigate(WalletRoutes.WALLET_ACCOUNT) {
                        popUpTo(WalletRoutes.WALLET_SELECTION) { inclusive = true }
                    }
                    onWalletSetupComplete()
                },
                onBackPressed = {
                    navController.popBackStack()
                },
                onShowMnemonic = { address, privateKey, walletName ->
                    navController.navigate(
                        WalletRoutes.mnemonic(address, privateKey, walletName)
                    )
                }
            )
        }

        // Wallet Account Screen
        composable(WalletRoutes.WALLET_ACCOUNT) {
            WalletAccountScreen(
                onBackPressed = {
                    navController.popBackStack()
                },
                onSwitchWallet = {
                    // Navigate back to PIN entry to switch wallet
                    navController.navigate(WalletRoutes.PIN_ENTRY) {
                        popUpTo(WalletRoutes.WALLET_ACCOUNT) { inclusive = true }
                    }
                },
                onShowMnemonic = { address, privateKey, walletName ->
                    navController.navigate(
                        WalletRoutes.mnemonic(address, privateKey, walletName)
                    )
                }
            )
        }

        // Mnemonic Screen
        composable(WalletRoutes.MNEMONIC) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: ""
            val privateKey = backStackEntry.arguments?.getString("privateKey") ?: ""
            val walletName = backStackEntry.arguments?.getString("walletName") ?: ""

            MnemonicScreen(
                walletAddress = address,
                privateKey = privateKey,
                walletName = walletName,
                onBackPressed = {
                    navController.popBackStack()
                },
                onContinue = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// Integration with existing VoteChain navigation
@Composable
fun WalletIntegrationScreen(
    onWalletSetupComplete: () -> Unit,
    onBackPressed: () -> Unit
) {
    val navController = rememberNavController()

    WalletNavigation(
        navController = navController,
        onWalletSetupComplete = onWalletSetupComplete
    )
}