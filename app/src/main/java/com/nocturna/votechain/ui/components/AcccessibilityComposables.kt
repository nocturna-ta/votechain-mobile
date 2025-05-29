package com.nocturna.votechain.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.utils.AccessibilityManager

/**
 * Accessible Card Component with TTS support
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccessibleCard(
    modifier: Modifier = Modifier,
    speechText: String,
    contentDescription: String,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager.getInstance(context) }
    val isAccessibilityEnabled by accessibilityManager.isEnabled.collectAsState()

    Card(
        modifier = modifier
            .combinedClickable(
                onClick = {
                    if (isAccessibilityEnabled) {
                        accessibilityManager.speakText(speechText)
                    }
                    onClick()
                },
                onLongClick = onLongClick,
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            )
            .semantics {
                this.contentDescription = contentDescription
                if (enabled) {
                    this.onClick {
                        if (isAccessibilityEnabled) {
                            accessibilityManager.speakText(speechText)
                        }
                        onClick()
                        true
                    }
                }
            },
        content = { content() }
    )
}

/**
 * Accessible Button with TTS support
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    speechText: String,
    contentDescription: String? = null,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    content: @Composable RowScope.() -> Unit
) {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager.getInstance(context) }
    val isAccessibilityEnabled by accessibilityManager.isEnabled.collectAsState()

    Button(
        onClick = {
            if (isAccessibilityEnabled) {
                accessibilityManager.speakText(speechText)
            }
            onClick()
        },
        modifier = modifier.semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
        },
        enabled = enabled,
        colors = colors,
        shape = shape,
        content = content
    )
}

/**
 * Accessible Text Field with TTS support for labels and errors
 */
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    speechText: String = label,
    errorText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager.getInstance(context) }
    val isAccessibilityEnabled by accessibilityManager.isEnabled.collectAsState()

    // Speak error when it appears
    LaunchedEffect(errorText) {
        if (isAccessibilityEnabled && errorText != null && errorText.isNotEmpty()) {
            accessibilityManager.speakError(errorText)
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .semantics {
                contentDescription = speechText
            },
        label = { Text(label) },
        enabled = enabled,
        singleLine = singleLine,
        isError = isError,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation
    )
}

/**
 * Accessible Candidate Card specifically for voting
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccessibleCandidateCard(
    candidateNumber: Int,
    presidentName: String,
    vicePresidentName: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager.getInstance(context) }
    val isAccessibilityEnabled by accessibilityManager.isEnabled.collectAsState()

    val speechText = "Kandidat nomor $candidateNumber. Calon Presiden: $presidentName. Calon Wakil Presiden: $vicePresidentName."
    val selectionState = if (isSelected) "Dipilih" else "Tidak dipilih"
    val fullSpeechText = "$speechText $selectionState. Ketuk untuk ${if (isSelected) "membatalkan pilihan" else "memilih"}"

    Card(
        modifier = modifier
            .combinedClickable(
                onClick = {
                    if (isAccessibilityEnabled) {
                        if (isSelected) {
                            accessibilityManager.speakText("Membatalkan pilihan kandidat nomor $candidateNumber")
                        } else {
                            accessibilityManager.speakCandidateSelection(candidateNumber, presidentName)
                        }
                    }
                    onClick()
                },
                onLongClick = {
                    if (isAccessibilityEnabled) {
                        accessibilityManager.speakCandidateInfo(candidateNumber, presidentName, vicePresidentName)
                    }
                }
            )
            .semantics {
                contentDescription = fullSpeechText
                onClick {
                    if (isAccessibilityEnabled) {
                        if (isSelected) {
                            accessibilityManager.speakText("Membatalkan pilihan kandidat nomor $candidateNumber")
                        } else {
                            accessibilityManager.speakCandidateSelection(candidateNumber, presidentName)
                        }
                    }
                    onClick()
                    true
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            else
                androidx.compose.material3.MaterialTheme.colorScheme.surface
        )
    ) {
        content()
    }
}

/**
 * Accessibility Toggle Switch
 */
@Composable
fun AccessibilityToggle(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager.getInstance(context) }
    val isEnabled by accessibilityManager.isEnabled.collectAsState()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Bantuan Suara",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )

        Switch(
            checked = isEnabled,
            onCheckedChange = { enabled ->
                if (enabled) {
                    accessibilityManager.initialize()
                } else {
                    accessibilityManager.setEnabled(false)
                }
            }
        )
    }
}

/**
 * Screen announcement helper
 */
@Composable
fun ScreenAnnouncement(
    screenName: String,
    announcement: String? = null
) {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager.getInstance(context) }
    val isAccessibilityEnabled by accessibilityManager.isEnabled.collectAsState()

    LaunchedEffect(screenName) {
        if (isAccessibilityEnabled) {
            val text = announcement ?: "Halaman $screenName"
            accessibilityManager.speakNavigation(text)
        }
    }
}