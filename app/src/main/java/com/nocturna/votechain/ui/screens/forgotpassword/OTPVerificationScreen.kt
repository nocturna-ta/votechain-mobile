package com.nocturna.votechain.ui.screens.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.forgotpassword.ForgotPasswordViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OTPVerificationScreen(
    email: String,
    onBackClick: () -> Unit,
    onVerificationSuccess: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val strings = LanguageManager.getLocalizedStrings()
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var otp by remember { mutableStateOf("") }
    val maxChar = 6

    var secondsLeft by remember { mutableStateOf(60) }
    var isResendEnabled by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle state changes
    LaunchedEffect(state) {
        when (state) {
            is ForgotPasswordViewModel.ForgotPasswordState.OTPVerified -> {
                onVerificationSuccess()
            }
            else -> {}
        }
    }

    // Timer for OTP resend
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        isResendEnabled = true
    }

    // Request focus
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralColors.Neutral10)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Back button and header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(onClick = onBackClick) {
                    Text(
                        text = "← Back",
                        style = AppTypography.heading6Medium,
                        color = MainColors.Primary1
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Header
            Text(
                text = "Enter Verification Code",
                style = AppTypography.heading1Bold,
                color = MainColors.Primary1
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = "We've sent a verification code to",
                style = AppTypography.heading4Medium,
                color = NeutralColors.Neutral70,
                textAlign = TextAlign.Center
            )
            Text(
                text = email,
                style = AppTypography.heading4SemiBold,
                color = MainColors.Primary1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Display error message if any
            if (state is ForgotPasswordViewModel.ForgotPasswordState.Error) {
                Text(
                    text = (state as ForgotPasswordViewModel.ForgotPasswordState.Error).message,
                    style = AppTypography.paragraphRegular,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            // OTP TextField
            BasicTextField(
                value = otp,
                onValueChange = { value ->
                    if (value.length <= maxChar && value.all { it.isDigit() }) {
                        otp = value
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    fontSize = 0.sp,
                    color = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (otp.length == maxChar) {
                            viewModel.verifyOTP(email, otp)
                        }
                    }
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(maxChar) { index ->
                            val char = when {
                                index < otp.length -> otp[index].toString()
                                else -> ""
                            }

                            val isFocused = index == otp.length

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .border(
                                        width = 1.dp,
                                        color = when {
                                            char.isNotEmpty() -> MainColors.Primary1
                                            isFocused -> MainColors.Primary1
                                            else -> NeutralColors.Neutral30
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        color = NeutralColors.Neutral10,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 20.sp,
                                    color = NeutralColors.Neutral70,
                                    textAlign = TextAlign.Center
                                )
                                if (isFocused) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp)
                                            .width(16.dp)
                                            .height(2.dp)
                                            .background(MainColors.Primary1)
                                    )
                                }
                            }
                        }
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Resend code option
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Didn't receive code? ",
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral70
                )

                if (isResendEnabled) {
                    TextButton(
                        onClick = {
                            viewModel.sendVerificationEmail(email)
                            secondsLeft = 60
                            isResendEnabled = false
                            coroutineScope.launch {
                                while (secondsLeft > 0) {
                                    delay(1000)
                                    secondsLeft--
                                }
                                isResendEnabled = true
                            }
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Resend",
                            style = AppTypography.paragraphMedium,
                            color = MainColors.Primary1
                        )
                    }
                } else {
                    Text(
                        text = "Resend in ${secondsLeft}s",
                        style = AppTypography.paragraphMedium,
                        color = NeutralColors.Neutral50
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Verify Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    viewModel.verifyOTP(email, otp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColors.Primary1,
                    disabledContainerColor = MainColors.Primary1.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = otp.length == maxChar && state !is ForgotPasswordViewModel.ForgotPasswordState.Loading
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state is ForgotPasswordViewModel.ForgotPasswordState.Loading) {
                        CircularProgressIndicator(
                            color = NeutralColors.Neutral10,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Verify",
                            style = AppTypography.heading4SemiBold,
                            color = NeutralColors.Neutral10
                        )
                    }
                }
            }
        }
    }
}
