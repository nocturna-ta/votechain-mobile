package com.nocturna.votechain.ui.screens.login

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.test.services.storage.file.PropertyFile.Column
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.VotechainTheme
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.login.LoginViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen() {
    val strings = LanguageManager.getLocalizedStrings()

    var email by remember { mutableStateOf("") }
    var isEmailFocused by remember { mutableStateOf(false) }
    var isEmailValid by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val emailError = "Please enter a valid email address"

    val emailFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Validator functions
    val validateEmail: (String) -> Boolean = { input ->
        input.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status bar spacing
        Spacer(modifier = Modifier.height(100.dp))

        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Spacer(modifier = Modifier.height(180.dp))

            // Title
            Text(
                text = "Provide Email Address",
                style = AppTypography.heading2Bold,
                color = MainColors.Primary1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "To proceed, we'll send a verification code to your email.",
                style = AppTypography.heading5Regular,
                color = NeutralColors.Neutral70,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email input field
            // Form section with animations
            Box() {
                Column {
                    // Email field with enhanced feedback
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            isEmailValid = validateEmail(it)
                        },
                        label = { Text(strings.email) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(emailFocusRequester)
                            .onFocusChanged { isEmailFocused = it.isFocused },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MainColors.Primary1,
                            unfocusedBorderColor = NeutralColors.Neutral30,
                            focusedTextColor = NeutralColors.Neutral70,
                            unfocusedTextColor = NeutralColors.Neutral70,
                            focusedLabelColor = MainColors.Primary1,
                            unfocusedLabelColor = NeutralColors.Neutral30,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorLabelColor = MaterialTheme.colorScheme.error
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        isError = !isEmailValid,
                        supportingText = {
                            if (!isEmailValid) {
                                Text(
                                    text = emailError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = AppTypography.paragraphRegular
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Not Now button
                OutlinedButton(
                    onClick = { /* Handle not now */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF718096)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color(0xFFE2E8F0)
                    )
                ) {
                    Text(
                        text = "Not Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Send OTP button
                Button(
                    onClick = { /* Handle send OTP */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90A4),
                        contentColor = Color.White
                    ),
                    enabled = email.isNotEmpty()
                ) {
                    Text(
                        text = "Send OTP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmailVerificationScreenPreview() {
    VotechainTheme {
        EmailVerificationScreen()
    }
}