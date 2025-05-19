package com.nocturna.votechain.ui.screens.votepage

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
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
    // State for 4-digit OTP input
    var otpDigits by remember { mutableStateOf(List(4) { "" }) }

    // State to track if all OTP fields are filled
    val isOtpComplete = remember(otpDigits) { otpDigits.all { it.isNotEmpty() } }

    // Focus management for OTP fields
    val focusRequesters = List(4) { remember { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    // Countdown timer state
    var remainingSeconds by remember { mutableStateOf(180) } // 3 minutes in seconds
    var isTimerRunning by remember { mutableStateOf(true) }

    // Format seconds to mm:ss
    val formattedTime = remember(remainingSeconds) {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    // Timer effect
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
        }
    }

    // State for showing verification success
    var showVerificationSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Resend OTP function
    val resendOTP = {
        remainingSeconds = 180 // Reset to 3 minutes
        isTimerRunning = true
        // Here you would add the actual API call to resend OTP
    }

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
            text = "Enter the OTP sent to your registered phone number to verify your identity before voting",
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
            for (i in 0 until 4) {
                val isFocused = remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = otpDigits[i],
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || (newValue.length == 1 && newValue.all { it.isDigit() })) {
                                val newDigits = otpDigits.toMutableList().apply {
                                    set(i, newValue)
                                }
                                otpDigits = newDigits

                                // Auto-move to next field if digit entered
                                if (newValue.isNotEmpty() && i < 3) {
                                    focusRequesters[i + 1].requestFocus()
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequesters[i])
                            .onFocusChanged { isFocused.value = it.isFocused },
                        textStyle = AppTypography.heading5Regular.copy(
                            color = PrimaryColors.Primary70,
                            textAlign = TextAlign.Center
                        ),
                        decorationBox = { innerTextField ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Text field
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    innerTextField()
                                }

                                // Underline
                                Divider(
                                    color = if (isFocused.value) MainColors.Primary1 else NeutralColors.Neutral30,
                                    thickness = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    )
                }
            }
        }

        // Resend OTP text and button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Didn't you receive the OTP? ",
                style = AppTypography.heading5Medium,
                color = NeutralColors.Neutral70
            )
            Text(
                text = "Resend OTP",
                style = AppTypography.heading5Medium,
                color = if (remainingSeconds > 0) NeutralColors.Neutral40 else MainColors.Primary1,
                modifier = Modifier.clickable(enabled = remainingSeconds == 0) {
                    if (remainingSeconds == 0) {
                        resendOTP()
                    }
                }
            )
        }

        // Verify Button
        Button(
            onClick = {
                showVerificationSuccess = true
                coroutineScope.launch {
                    // Simulate verification process
                    delay(1500)
                    // Navigate to candidate selection screen
                    onVerificationComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(horizontal = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MainColors.Primary1,
                contentColor = NeutralColors.Neutral10,
                disabledContainerColor = NeutralColors.Neutral30,
                disabledContentColor = NeutralColors.Neutral50
            ),
            shape = RoundedCornerShape(22.dp),
            enabled = isOtpComplete && !showVerificationSuccess
        ) {
            if (showVerificationSuccess) {
                CircularProgressIndicator(
                    color = NeutralColors.Neutral10,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Verify to Vote",
                    style = AppTypography.heading4SemiBold,
                    color = NeutralColors.Neutral10
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))
    }

    // Request focus to first digit field when screen loads
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }
}