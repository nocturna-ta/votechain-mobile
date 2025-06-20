package com.nocturna.votechain.ui.screens.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.wallet.WalletAccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletAccountScreen(
    onBackPressed: () -> Unit,
    onSwitchWallet: () -> Unit,
    onShowMnemonic: (String, String, String) -> Unit // address, privateKey, walletName
) {
    val context = LocalContext.current
    val viewModel: WalletAccountViewModel = viewModel(
        factory = WalletAccountViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    val strings = LanguageManager.getLocalizedStrings()

    var showPrivateKey by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCopiedMessage by remember { mutableStateOf(false) }

    // Handle side effects
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onBackPressed()
        }
    }

    LaunchedEffect(uiState.showMnemonic) {
        uiState.showMnemonic?.let { (address, privateKey, walletName) ->
            onShowMnemonic(address, privateKey, walletName)
            viewModel.clearMnemonicDisplay()
        }
    }

    LaunchedEffect(showCopiedMessage) {
        if (showCopiedMessage) {
            kotlinx.coroutines.delay(2000)
            showCopiedMessage = false
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
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MainColors.Primary1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = uiState.walletData?.name ?: "Wallet",
                    style = AppTypography.paragraphRegular.copy(
                        color = MainColors.Primary1,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Box {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MainColors.Primary1
                        )
                    }

                    WalletOptionsMenu(
                        expanded = showOptionsMenu,
                        onDismiss = { showOptionsMenu = false },
                        onSwitchWallet = {
                            showOptionsMenu = false
                            onSwitchWallet()
                        },
                        onRenameWallet = {
                            showOptionsMenu = false
                            showRenameDialog = true
                        },
                        onShowBackupPhrase = {
                            showOptionsMenu = false
                            viewModel.showBackupPhrase()
                        },
                        onDeleteWallet = {
                            showOptionsMenu = false
                            showDeleteDialog = true
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MainColors.Primary1)
                }
            } else {
                uiState.walletData?.let { wallet ->
                    // Balance Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MainColors.Primary1
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Balance",
                                style = AppTypography.paragraphRegular.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.formattedBalance,
                                style = AppTypography.paragraphRegular.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Wallet Details
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Public Key
                        WalletDetailItem(
                            label = "Public Key",
                            value = wallet.publicKey,
                            onCopy = {
                                copyToClipboard(context, wallet.publicKey, "Public Key")
                                showCopiedMessage = true
                            }
                        )

                        // Private Key
                        WalletDetailItem(
                            label = "Private Key",
                            value = if (showPrivateKey) wallet.privateKey else "•".repeat(64),
                            onCopy = {
                                if (showPrivateKey) {
                                    copyToClipboard(context, wallet.privateKey, "Private Key")
                                    showCopiedMessage = true
                                }
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (!showPrivateKey) {
                                            // Show warning before revealing private key
                                            viewModel.showPrivateKeyWarning {
                                                showPrivateKey = true
                                            }
                                        } else {
                                            showPrivateKey = false
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = if (showPrivateKey) {
                                            painterResource(id = R.drawable.hide)
                                        } else {
                                            painterResource(id = R.drawable.show)
                                        },
                                        contentDescription = if (showPrivateKey) "Hide" else "Show",
                                        tint = MainColors.Primary1
                                    )
                                }
                            }
                        )

                        // Address
                        WalletDetailItem(
                            label = "Address",
                            value = wallet.address,
                            onCopy = {
                                copyToClipboard(context, wallet.address, "Address")
                                showCopiedMessage = true
                            }
                        )
                    }
                }

                // Error display
                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
        }

        // Copied Message
        if (showCopiedMessage) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NeutralColors.Neutral70
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = AppTypography.paragraphRegular.copy(color = Color.White),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }

    // Dialogs
    if (showRenameDialog) {
        RenameWalletDialog(
            currentName = uiState.walletData?.name ?: "",
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                showRenameDialog = false
                viewModel.renameWallet(newName)
            }
        )
    }

    if (showDeleteDialog) {
        DeleteWalletDialog(
            walletName = uiState.walletData?.name ?: "",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteWallet()
            }
        )
    }

    // Private Key Warning Dialog
    if (uiState.showPrivateKeyWarning) {
        PrivateKeyWarningDialog(
            onDismiss = { viewModel.hidePrivateKeyWarning() },
            onConfirm = {
                viewModel.hidePrivateKeyWarning()
                uiState.onPrivateKeyWarningConfirm?.invoke()
            }
        )
    }
}

@Composable
fun WalletDetailItem(
    label: String,
    value: String,
    onCopy: () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = NeutralColors.Neutral50
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = AppTypography.paragraphRegular.copy(
                        color = NeutralColors.Neutral60,
                        fontWeight = FontWeight.Medium
                    )
                )

                Row {
                    trailingIcon?.invoke()
                    IconButton(onClick = onCopy) {
                        Icon(
                            painter = painterResource(id = R.drawable.copy),
                            contentDescription = "Copy",
                            tint = MainColors.Primary1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = AppTypography.paragraphRegular.copy(
                    color = NeutralColors.Neutral70
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun WalletOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSwitchWallet: () -> Unit,
    onRenameWallet: () -> Unit,
    onShowBackupPhrase: () -> Unit,
    onDeleteWallet: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Switch Wallet") },
            onClick = onSwitchWallet
        )
        DropdownMenuItem(
            text = { Text("Rename Wallet") },
            onClick = onRenameWallet
        )
        DropdownMenuItem(
            text = { Text("Show Backup Phrase") },
            onClick = onShowBackupPhrase
        )
        DropdownMenuItem(
            text = { Text("Delete Wallet") },
            onClick = onDeleteWallet
        )
    }
}

@Composable
fun RenameWalletDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Wallet") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Wallet Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName.trim()) },
                enabled = newName.trim().isNotEmpty()
            ) {
                Text("Save", color = MainColors.Primary1)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NeutralColors.Neutral60)
            }
        }
    )
}

@Composable
fun DeleteWalletDialog(
    walletName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Wallet") },
        text = {
            Text("Are you sure you want to delete \"$walletName\"? Make sure you have your backup phrase saved. This action cannot be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NeutralColors.Neutral60)
            }
        }
    )
}

@Composable
fun PrivateKeyWarningDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Warning") },
        text = {
            Text("Displaying your private key is a security risk. Ensure no one else can see your screen.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Show", color = MainColors.Primary1)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NeutralColors.Neutral60)
            }
        }
    )
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}