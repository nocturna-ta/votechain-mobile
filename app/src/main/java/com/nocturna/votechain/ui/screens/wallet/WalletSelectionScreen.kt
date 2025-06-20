package com.nocturna.votechain.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.wallet.WalletSelectionViewModel
import org.web3j.utils.Convert
import java.math.BigInteger

data class WalletDisplayInfo(
    val address: String,
    val name: String,
    val balance: String = "Loading...",
    val isDefault: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSelectionScreen(
    pin: String,
    onWalletSelected: () -> Unit,
    onBackPressed: () -> Unit,
    onShowMnemonic: (String, String, String) -> Unit // address, privateKey, walletName
) {
    val context = LocalContext.current
    val viewModel: WalletSelectionViewModel = viewModel(
        factory = WalletSelectionViewModel.Factory(context, pin)
    )
    val uiState by viewModel.uiState.collectAsState()

    val strings = LanguageManager.getLocalizedStrings()

    var showAddWalletDialog by remember { mutableStateOf(false) }

    // Handle wallet selection success
    LaunchedEffect(uiState.isWalletSelected) {
        if (uiState.isWalletSelected) {
            onWalletSelected()
        }
    }

    // Handle mnemonic display
    LaunchedEffect(uiState.showMnemonic) {
        uiState.showMnemonic?.let { (address, privateKey, walletName) ->
            onShowMnemonic(address, privateKey, walletName)
            viewModel.clearMnemonicDisplay()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralColors.Neutral10)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MainColors.Primary1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Select Wallet",
                    style = AppTypography.paragraphRegular.copy(
                        color = MainColors.Primary1,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
            }

            // Wallets List
            if (uiState.isLoading && uiState.wallets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MainColors.Primary1
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.wallets) { wallet ->
                        WalletItem(
                            wallet = wallet,
                            onClick = { viewModel.selectWallet(wallet.address) }
                        )
                    }
                }
            }

            // Error display
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        style = AppTypography.paragraphRegular.copy(
                            color = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddWalletDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MainColors.Primary1,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Wallet"
            )
        }
    }

    // Add Wallet Dialog
    if (showAddWalletDialog) {
        AddWalletDialog(
            onDismiss = { showAddWalletDialog = false },
            onCreateWallet = { walletName ->
                showAddWalletDialog = false
                viewModel.createNewWallet(walletName.ifBlank { "New Wallet" })
            },
            onImportFromPrivateKey = { privateKey, walletName ->
                showAddWalletDialog = false
                viewModel.importWallet(privateKey, walletName.ifBlank { "Imported Wallet" })
            },
            onImportFromMnemonic = { mnemonic, walletName ->
                showAddWalletDialog = false
                viewModel.importWalletFromMnemonic(mnemonic, walletName.ifBlank { "Restored Wallet" })
            }
        )
    }
}

@Composable
fun WalletItem(
    wallet: WalletDisplayInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = NeutralColors.Neutral70
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wallet Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MainColors.Primary1,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
//                Icon(
//                    imageVector = Icons.Default.AccountBalanceWallet,
//                    contentDescription = null,
//                    tint = MainColors.Primary500,
//                    modifier = Modifier.size(24.dp)
//                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Wallet Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = wallet.name,
                        style = AppTypography.paragraphRegular.copy(
                            fontWeight = FontWeight.Medium,
                            color = NeutralColors.Neutral70
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (wallet.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MainColors.Primary1,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Default",
                                style = AppTypography.paragraphRegular.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${wallet.address.take(6)}...${wallet.address.takeLast(4)}",
                    style = AppTypography.paragraphRegular.copy(
                        color = NeutralColors.Neutral70
                    )
                )
            }

            // Balance
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = wallet.balance,
                    style = AppTypography.paragraphRegular.copy(
                        fontWeight = FontWeight.Medium,
                        color = MainColors.Primary1
                    )
                )
            }
        }
    }
}

@Composable
fun AddWalletDialog(
    onDismiss: () -> Unit,
    onCreateWallet: (String) -> Unit,
    onImportFromPrivateKey: (String, String) -> Unit,
    onImportFromMnemonic: (String, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var walletName by remember { mutableStateOf("") }
    var privateKey by remember { mutableStateOf("") }
    var mnemonic by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Wallet",
                style = AppTypography.paragraphRegular.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MainColors.Primary1
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Create") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Import") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Restore") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content based on selected tab
                when (selectedTab) {
                    0 -> {
                        // Create new wallet
                        OutlinedTextField(
                            value = walletName,
                            onValueChange = { walletName = it },
                            label = { Text("Wallet Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    1 -> {
                        // Import from private key
                        Column {
                            OutlinedTextField(
                                value = walletName,
                                onValueChange = { walletName = it },
                                label = { Text("Wallet Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = privateKey,
                                onValueChange = { privateKey = it },
                                label = { Text("Private Key") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                    2 -> {
                        // Restore from mnemonic
                        Column {
                            OutlinedTextField(
                                value = walletName,
                                onValueChange = { walletName = it },
                                label = { Text("Wallet Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = mnemonic,
                                onValueChange = { mnemonic = it },
                                label = { Text("Mnemonic Phrase") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 4
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (selectedTab) {
                        0 -> onCreateWallet(walletName)
                        1 -> {
                            if (privateKey.isNotBlank()) {
                                onImportFromPrivateKey(privateKey, walletName)
                            }
                        }
                        2 -> {
                            if (mnemonic.isNotBlank()) {
                                onImportFromMnemonic(mnemonic, walletName)
                            }
                        }
                    }
                },
                enabled = when (selectedTab) {
                    0 -> true
                    1 -> privateKey.isNotBlank()
                    2 -> mnemonic.isNotBlank()
                    else -> false
                }
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> "Create"
                        1 -> "Import"
                        2 -> "Restore"
                        else -> "OK"
                    },
                    color = MainColors.Primary1
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = NeutralColors.Neutral70
                )
            }
        }
    )
}