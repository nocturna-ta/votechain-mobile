package com.nocturna.votechain.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.DangerColors
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.WarningColors
import com.nocturna.votechain.utils.VotingErrorHandler

/**
 * Enhanced error dialog for voting operations
 */
@Composable
fun VotingErrorDialog(
    error: VotingErrorHandler.VotingError,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onGetHelp: () -> Unit = {},
    recoverySuggestions: List<String> = emptyList(),
    estimatedRecoveryTime: String = "Unknown"
) {
    var showDetails by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error Icon and Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (error.isRetryable) Icons.Default.Warning else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (error.isRetryable) WarningColors.Warning50 else DangerColors.Danger50,
                        modifier = Modifier.size(32.dp)
                    )

                    Text(
                        text = error.title,
                        style = AppTypography.smallParagraphMedium,
                        color = NeutralColors.Neutral90,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Error Message
                Text(
                    text = error.message,
                    style = AppTypography.paragraphMedium,
                    color = NeutralColors.Neutral70,
                    textAlign = TextAlign.Start
                )

                // Recovery Action (if available)
                error.recoveryAction?.let { action ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MainColors.Primary1.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "💡 $action",
                            style = AppTypography.paragraphRegular,
                            color = MainColors.Primary1,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Estimated Recovery Time (for retryable errors)
                if (error.isRetryable && estimatedRecoveryTime != "Unknown") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⏱️ Estimated recovery time:",
                            style = AppTypography.paragraphRegular,
                            color = NeutralColors.Neutral60
                        )
                        Text(
                            text = estimatedRecoveryTime,
                            style = AppTypography.paragraphRegular,
                            color = MainColors.Primary1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Recovery Suggestions (collapsible)
                if (recoverySuggestions.isNotEmpty()) {
                    Column {
                        TextButton(
                            onClick = { showDetails = !showDetails },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (showDetails) "Hide Details" else "Show Recovery Steps",
                                style = AppTypography.paragraphMedium,
                                color = MainColors.Primary1
                            )
                        }

                        if (showDetails) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = NeutralColors.Neutral10
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(recoverySuggestions) { suggestion ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "•",
                                                style = AppTypography.paragraphRegular,
                                                color = MainColors.Primary1
                                            )
                                            Text(
                                                text = suggestion,
                                                style = AppTypography.paragraphRegular,
                                                color = NeutralColors.Neutral70,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dismiss Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NeutralColors.Neutral70
                        )
                    ) {
                        Text(
                            text = "Close",
                            style = AppTypography.paragraphMedium
                        )
                    }

                    // Retry Button (if error is retryable)
                    if (error.isRetryable) {
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColors.Primary1
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Retry",
                                style = AppTypography.paragraphMedium
                            )
                        }
                    }
                }

                // Get Help Button (secondary action)
                TextButton(
                    onClick = onGetHelp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Contact Support",
                        style = AppTypography.paragraphMedium,
                        color = MainColors.Primary1
                    )
                }
            }
        }
    }
}

/**
 * Simplified error dialog for quick errors
 */
@Composable
fun SimpleVotingErrorDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onAction: (() -> Unit)? = null,
    actionText: String = "OK"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = AppTypography.paragraphMedium,
                color = NeutralColors.Neutral90
            )
        },
        text = {
            Text(
                text = message,
                style = AppTypography.paragraphMedium,
                color = NeutralColors.Neutral70
            )
        },
        confirmButton = {
            Button(
                onClick = onAction ?: onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColors.Primary1
                )
            ) {
                Text(
                    text = actionText,
                    style = AppTypography.paragraphMedium
                )
            }
        },
        dismissButton = if (onAction != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        style = AppTypography.paragraphMedium,
                        color = NeutralColors.Neutral70
                    )
                }
            }
        } else null,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Loading dialog for voting operations
 */
@Composable
fun VotingLoadingDialog(
    message: String = "Processing your vote...",
    onDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MainColors.Primary1,
                    strokeWidth = 4.dp
                )

                Text(
                    text = message,
                    style = AppTypography.paragraphMedium,
                    color = NeutralColors.Neutral70,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Success dialog for voting operations
 */
@Composable
fun VotingSuccessDialog(
    title: String = "Vote Cast Successfully!",
    message: String = "Your vote has been recorded and submitted to the blockchain.",
    txHash: String? = null,
    onDismiss: () -> Unit,
    onViewResults: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✅",
                        style = AppTypography.paragraphMedium
                    )
                }

                // Title
                Text(
                    text = title,
                    style = AppTypography.paragraphMedium,
                    color = NeutralColors.Neutral90,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Message
                Text(
                    text = message,
                    style = AppTypography.paragraphMedium,
                    color = NeutralColors.Neutral70,
                    textAlign = TextAlign.Center
                )

                // Transaction Hash (if available)
                txHash?.let { hash ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MainColors.Primary1.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Transaction Hash:",
                                style = AppTypography.paragraphRegular,
                                color = NeutralColors.Neutral60
                            )
                            Text(
                                text = hash,
                                style = AppTypography.paragraphRegular,
                                color = MainColors.Primary1,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainColors.Primary1
                        )
                    ) {
                        Text(
                            text = "Done",
                            style = AppTypography.paragraphMedium
                        )
                    }

                    OutlinedButton(
                        onClick = onViewResults,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MainColors.Primary1
                        )
                    ) {
                        Text(
                            text = "View Results",
                            style = AppTypography.paragraphMedium
                        )
                    }
                }
            }
        }
    }
}