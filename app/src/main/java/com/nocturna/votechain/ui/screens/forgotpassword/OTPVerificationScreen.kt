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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
    navController: NavController,
    email: String,
    onBackClick: () -> Unit = { navController.popBackStack() },
    onVerificationComplete: (String, String) -> Unit = { verifiedEmail, otp ->
        navController.navigate("reset_password?email=$verifiedEmail&otp=$otp")
    }
) {
    val context = LocalContext.current
    val viewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModel.Factory(context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // State for 4-digit OTP input
    var otpDigits by remember { mutableStateOf(List(4) { "" }) }
    val otp = otpDigits.joinToString("")

    // Focus management for OTP fields
    val focusRequesters = List(4) { remember { FocusRequester() } }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Timer state for resend functionality
    var secondsLeft by remember { mutableStateOf(60) }
    var isResendEnabled by remember { mutableStateOf(false) }

    // Format seconds to mm:ss
    val formattedTime = remember(secondsLeft) {
        val minutes = secondsLeft / 60
        val seconds = secondsLeft % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    // Handle state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is ForgotPasswordViewModel.ForgotPasswordUiState.OtpVerified -> {
                onVerificationComplete(email, otp)
            }
            is ForgotPasswordViewModel.ForgotPasswordUiState.OtpSent -> {
                // Reset timer when OTP is sent/resent
                secondsLeft = 60
                isResendEnabled = false
            }
            else -> {}
        }
    }

    // Timer effect for resend functionality
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        isResendEnabled = true
    }

    // Request focus to first digit field when screen loads
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
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
            val currentState = uiState
            if (currentState is ForgotPasswordViewModel.ForgotPasswordUiState.Error) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = currentState.message,
                        style = AppTypography.paragraphRegular,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 4-digit OTP input fields
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(
                                width = 2.dp,
                                color = if (otpDigits[index].isNotEmpty()) MainColors.Primary1 else NeutralColors.Neutral30,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(
                                color = NeutralColors.Neutral10,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = TextFieldValue(
                                text = otpDigits[index],
                                selection = TextRange(otpDigits[index].length)
                            ),
                            onValueChange = { newValue ->
                                val newText = newValue.text.filter { it.isDigit() }
                                if (newText.length <= 1) {
                                    val newOtpDigits = otpDigits.toMutableList()
                                    newOtpDigits[index] = newText
                                    otpDigits = newOtpDigits

                                    // Auto-focus next field
                                    if (newText.isNotEmpty() && index < 3) {
                                        focusRequesters[index + 1].requestFocus()
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = if (index == 3) ImeAction.Done else ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    if (index < 3) {
                                        focusRequesters[index + 1].requestFocus()
                                    }
                                },
                                onDone = {
                                    if (otpDigits.all { it.isNotEmpty() }) {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        viewModel.verifyOtp(email, otp)
                                    }
                                }
                            ),
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                                color = MainColors.Primary1,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(focusRequesters[index])
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && otpDigits[index].isNotEmpty()) {
                                        // Clear the current digit when focused
                                        val newOtpDigits = otpDigits.toMutableList()
                                        newOtpDigits[index] = ""
                                        otpDigits = newOtpDigits
                                    }
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Resend OTP section
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Didn't receive the code? ",
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral70
                )

                if (isResendEnabled) {
                    TextButton(
                        onClick = {
                            viewModel.resendOtp(email)
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
                    viewModel.verifyOtp(email, otp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColors.Primary1,
                    disabledContainerColor = MainColors.Primary1.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = otpDigits.all { it.isNotEmpty() } && uiState !is ForgotPasswordViewModel.ForgotPasswordUiState.Loading
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState is ForgotPasswordViewModel.ForgotPasswordUiState.Loading) {
                        CircularProgressIndicator(
                            color = NeutralColors.Neutral10,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Verify Code",
                            style = AppTypography.heading4SemiBold,
                            color = NeutralColors.Neutral10
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}