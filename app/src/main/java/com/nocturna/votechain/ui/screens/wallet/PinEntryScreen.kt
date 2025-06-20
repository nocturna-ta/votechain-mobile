package com.nocturna.votechain.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.wallet.PinEntryViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryScreen(
    onPinVerified: () -> Unit,
    onWalletCreated: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PinEntryViewModel = viewModel(factory = PinEntryViewModel.Factory(context))
    val uiState by viewModel.uiState.collectAsState()

    val strings = LanguageManager.getLocalizedStrings()

    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Handle side effects
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            isError = true
            errorMessage = uiState.error ?: ""
            pin = ""
            delay(2000)
            isError = false
            errorMessage = ""
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            if (uiState.hasWallets) {
                onPinVerified()
            } else {
                onWalletCreated()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralColors.Neutral10)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Text(
                    text = "VoteChain Wallet",
                    style = AppTypography.paragraphRegular.copy(
                        color = MainColors.Primary1,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (uiState.hasWallets) {
                        "Enter your 4-digit PIN"
                    } else {
                        "Create your wallet with a 4-digit PIN"
                    },
                    style = AppTypography.paragraphRegular.copy(
                        color = NeutralColors.Neutral60
                    ),
                    textAlign = TextAlign.Center
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = AppTypography.paragraphRegular.copy(
                            color = MaterialTheme.colorScheme.error
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // PIN Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 32.dp)
                ) {
                    repeat(4) { index ->
                        PinDot(
                            isFilled = pin.length > index,
                            isError = isError
                        )
                    }
                }

                // Number Pad
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Numbers 1-3
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        NumberButton("1") { if (pin.length < 4) pin += "1" }
                        NumberButton("2") { if (pin.length < 4) pin += "2" }
                        NumberButton("3") { if (pin.length < 4) pin += "3" }
                    }

                    // Numbers 4-6
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        NumberButton("4") { if (pin.length < 4) pin += "4" }
                        NumberButton("5") { if (pin.length < 4) pin += "5" }
                        NumberButton("6") { if (pin.length < 4) pin += "6" }
                    }

                    // Numbers 7-9
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        NumberButton("7") { if (pin.length < 4) pin += "7" }
                        NumberButton("8") { if (pin.length < 4) pin += "8" }
                        NumberButton("9") { if (pin.length < 4) pin += "9" }
                    }

                    // Bottom row: Empty, 0, Delete
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Empty space
                        Spacer(modifier = Modifier.size(64.dp))

                        NumberButton("0") { if (pin.length < 4) pin += "0" }

                        // Delete button
                        IconButton(
                            onClick = {
                                if (pin.isNotEmpty()) {
                                    pin = pin.dropLast(1)
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = NeutralColors.Neutral70,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = "Delete",
                                tint = NeutralColors.Neutral60
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Done Button
                Button(
                    onClick = {
                        if (pin.length == 4) {
                            viewModel.verifyPin(pin)
                        }
                    },
                    enabled = pin.length == 4 && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainColors.Primary1,
                        disabledContainerColor = NeutralColors.Neutral60
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Text(
                                text = "Continue",
                                style = AppTypography.paragraphRegular.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PinDot(
    isFilled: Boolean,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    isFilled -> MainColors.Primary1
                    else -> NeutralColors.Neutral70
                },
                shape = CircleShape
            )
            .border(
                width = if (isFilled) 0.dp else 2.dp,
                color = if (isError) MaterialTheme.colorScheme.error else NeutralColors.Neutral60,
                shape = CircleShape
            )
    )
}

@Composable
fun NumberButton(
    number: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(
                color = NeutralColors.Neutral50,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            style = AppTypography.paragraphRegular.copy(
                color = NeutralColors.Neutral80,
                fontWeight = FontWeight.Medium
            )
        )
    }
}