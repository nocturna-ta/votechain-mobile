package com.nocturna.votechain.ui.screens.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.wallet.MnemonicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MnemonicScreen(
    walletAddress: String,
    privateKey: String,
    walletName: String,
    onBackPressed: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: MnemonicViewModel = viewModel(
        factory = MnemonicViewModel.Factory(context, walletAddress)
    )
    val uiState by viewModel.uiState.collectAsState()

    val strings = LanguageManager.getLocalizedStrings()
    var showCopiedMessage by remember { mutableStateOf(false) }

    // Show copied message and hide it after delay
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
                IconButton(
                    onClick = onBackPressed
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryColors.Primary50
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Backup Phrase",
                    style = AppTypography.paragraphRegular.copy(
                        color = MainColors.Primary1,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
            }

            // Warning Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Warning Title
            Text(
                text = "Save your backup phrase!",
                style = AppTypography.paragraphRegular.copy(
                    color = NeutralColors.Neutral60,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Warning Description
            Text(
                text = "Write down this 12-word phrase and store it safely. This is the only way to recover your wallet if you lose access.",
                style = AppTypography.paragraphRegular.copy(
                    color = NeutralColors.Neutral60
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Wallet Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = NeutralColors.Neutral70
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = walletName,
                        style = AppTypography.paragraphRegular.copy(
                            fontWeight = FontWeight.Medium,
                            color = NeutralColors.Neutral70
                        )
                    )
                    Text(
                        text = "${walletAddress.take(6)}...${walletAddress.takeLast(4)}",
                        style = AppTypography.paragraphRegular.copy(
                            color = NeutralColors.Neutral70
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mnemonic Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = NeutralColors.Neutral70
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Backup Phrase",
                            style = AppTypography.paragraphRegular.copy(
                                fontWeight = FontWeight.Medium,
                                color = NeutralColors.Neutral60
                            )
                        )

                        IconButton(
                            onClick = {
                                uiState.mnemonic?.let { mnemonic ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Mnemonic Phrase", mnemonic)
                                    clipboard.setPrimaryClip(clip)
                                    showCopiedMessage = true
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = com.nocturna.votechain.R.drawable.copy),
                                contentDescription = "Copy",
                                tint = MainColors.Primary1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MainColors.Primary1
                            )
                        }
                    } else {
                        Text(
                            text = uiState.mnemonic ?: "Failed to load mnemonic",
                            style = AppTypography.paragraphRegular.copy(
                                color = NeutralColors.Neutral60,
                                lineHeight = AppTypography.paragraphRegular.lineHeight * 1.5
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Security Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "⚠️ Never share your backup phrase with anyone. Store it offline in a secure location.",
                    style = AppTypography.paragraphRegular.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Continue Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColors.Primary1
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "I've Saved It Safely",
                    style = AppTypography.paragraphRegular.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Copied Message Snackbar
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
                    text = "Mnemonic phrase copied to clipboard",
                    style = AppTypography.paragraphRegular.copy(
                        color = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}