package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.viewmodel.vote.OTPVerificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OTPVotingVerificationScreen(
    navController: NavController,
    categoryId: String,
    onBackClick: () -> Unit = { navController.popBackStack() },
    onVerificationComplete: () -> Unit = {
        navController.navigate("candidate_selection/$categoryId") {
            popUpTo("otp_verification/$categoryId") { inclusive = true }
        }
    }
) {
    val context = LocalContext.current
    val viewModel: OTPVerificationViewModel = viewModel(
        factory = OTPVerificationViewModel.Factory(context, categoryId)
    )

    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // OTP input state
    var otpDigits by remember { mutableStateOf(listOf("", "", "", "")) }
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val focusManager = LocalFocusManager.current
    val isOtpComplete = otpDigits.all { it.isNotEmpty() }

    // Timer state - synced with ViewModel
    var remainingSeconds by remember { mutableStateOf(uiState.timeRemainingSeconds) }
    var isTimerRunning by remember { mutableStateOf(true) }

    // Update timer from ViewModel state
    LaunchedEffect(uiState.timeRemainingSeconds) {
        remainingSeconds = uiState.timeRemainingSeconds
        if (uiState.timeRemainingSeconds > 0) {
            isTimerRunning = true
        }
    }

    // Timer countdown effect
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning && remainingSeconds > 0) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
                viewModel.updateTimer(remainingSeconds)
            }
            isTimerRunning = false
        }
    }

    // Handle verification success
    LaunchedEffect(uiState.isVerificationSuccess) {
        if (uiState.isVerificationSuccess) {
            // Show success message briefly before navigating
            delay(1500)
            onVerificationComplete()
        }
    }

    // Format timer display
    val formattedTime = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)

    // Main screen content
    Column(modifier = Modifier.fillMaxSize()) {
        // Custom top bar with shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    tint = MainColors.Primary1,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterStart)
                        .clickable(onClick = onBackClick)
                )
            }

            // Centered title
            Text(
                text = "Voting Verification",
                style = AppTypography.heading4Regular,
                color = PrimaryColors.Primary80,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MainColors.Primary1,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Generating OTP...",
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral60
                    )
                }
            }
        } else {

        // Center content with vertical spacing
        Spacer(modifier = Modifier.weight(0.3f))

        // OTP Verification title
        Text(
            text = "OTP Verification",
            style = AppTypography.heading2Bold,
            color = MainColors.Primary1,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Instructions - modified for voting context
        Text(
            text = "Please enter the 4-digit verification code, sent to your registered phone number",
            style = AppTypography.heading5Regular,
            color = NeutralColors.Neutral70,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // Countdown timer
        Text(
            text = formattedTime,
            style = AppTypography.heading6SemiBold,
            color = MainColors.Primary1,
            modifier = Modifier
                .padding(vertical = 24.dp)
                .align(Alignment.CenterHorizontally)
        )

        // OTP Input Fields
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            otpDigits.forEachIndexed { index, digit ->
                BasicTextField(
                    value = digit,
                    onValueChange = { newValue ->
                        if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                            val newDigits = otpDigits.toMutableList()
                            newDigits[index] = newValue
                            otpDigits = newDigits

                            // Auto-focus next field
                            if (newValue.isNotEmpty() && index < 3) {
                                focusRequesters[index + 1].requestFocus()
                            }

                            // Auto-verify when all fields are filled
                            if (newDigits.all { it.isNotEmpty() }) {
                                val otpCode = newDigits.joinToString("")
                                focusManager.clearFocus()
                                viewModel.verifyOTP(otpCode)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(
                            width = if (digit.isNotEmpty()) 2.dp else 1.dp,
                            color = if (digit.isNotEmpty()) MainColors.Primary1 else NeutralColors.Neutral30,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .focusRequester(focusRequesters[index]),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (digit.isEmpty()) {
                                Text(
                                    text = "0",
                                    style = AppTypography.heading3Regular,
                                    color = NeutralColors.Neutral30
                                )
                            }
                            innerTextField()
                        }
                    },
                    textStyle = AppTypography.heading3Regular.copy(
                        color = MainColors.Primary1,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            if (!uiState.error.isNullOrEmpty()) {
                Text(
                    text = "error: ${uiState.error}",
                    style = AppTypography.smallParagraphRegular,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Success message
            if (uiState.isVerificationSuccess) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verification successful! Redirecting...",
                        style = AppTypography.paragraphRegular,
                        color = Color.Green
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Timer and resend section
            if (remainingSeconds > 0) {
                Text(
                    text = "Resend OTP in $formattedTime",
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral60,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Text(
                    text = "Didn't receive the code?",
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral60,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (uiState.isResending) "Resending..." else "Resend OTP",
                    style = AppTypography.paragraphMedium,
                    color = if (uiState.isResending) NeutralColors.Neutral40 else MainColors.Primary1,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(enabled = !uiState.isResending) {
                            // Clear OTP inputs
                            otpDigits = listOf("", "", "", "")
                            viewModel.clearError()
                            viewModel.resendOTP()
                        }
                )
            }

            // Remaining attempts
            if (uiState.remainingAttempts < 3) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Remaining attempts: ${uiState.remainingAttempts}",
                    style = AppTypography.smallParagraphRegular,
                    color = if (uiState.remainingAttempts > 0) NeutralColors.Neutral60 else Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.weight(0.7f))

            // Manual verify button (hidden when auto-verify works)
            if (isOtpComplete && !uiState.isVerifying && !uiState.isVerificationSuccess) {
                Button(
                    onClick = {
                        val otpCode = otpDigits.joinToString("")
                        viewModel.verifyOTP(otpCode)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainColors.Primary1,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isVerifying
                ) {
                    if (uiState.isVerifying) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Verify",
                            style = AppTypography.paragraphMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}