//package com.nocturna.votechain.ui.screens
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.ExperimentalComposeUiApi
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.nocturna.votechain.ui.theme.AppTypography
//import com.nocturna.votechain.ui.theme.MainColors
//import com.nocturna.votechain.ui.theme.NeutralColors
//import com.nocturna.votechain.ui.theme.PrimaryColors
//import kotlinx.coroutines.delay
//import com.nocturna.votechain.R
//import com.nocturna.votechain.ui.screens.login.EmailVerificationScreen
//import com.nocturna.votechain.ui.theme.VotechainTheme
//import com.nocturna.votechain.utils.LanguageManager
//import com.nocturna.votechain.viewmodel.forgotpassword.ForgotPasswordViewModel
//
//@OptIn(ExperimentalComposeUiApi::class)
//@Composable
//fun OTPVerificationScreen(
//    email: String,
//    onBackClick: () -> Unit,
//    onOtpVerified: () -> Unit
//) {
//    val strings = LanguageManager.getLocalizedStrings()
//    val context = LocalContext.current
//    val viewModel: ForgotPasswordViewModel = viewModel(factory = ForgotPasswordViewModel.Factory(context))
//    val uiState by viewModel.uiState.collectAsState()
//
//    var otpDigits by remember { mutableStateOf(listOf("", "", "", "")) }
//    var showElements by remember { mutableStateOf(false) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    val focusManager = LocalFocusManager.current
//    val keyboardController = LocalSoftwareKeyboardController.current
//    val focusRequesters = remember { List(4) { FocusRequester() } }
//
//    // Animation values
//    val titleAlpha = animateFloatAsState(
//        targetValue = if (showElements) 1f else 0f,
//        animationSpec = tween(700)
//    )
//    val formAlpha = animateFloatAsState(
//        targetValue = if (showElements) 1f else 0f,
//        animationSpec = tween(1000)
//    )
//    val buttonScale = animateFloatAsState(
//        targetValue = if (showElements) 1f else 0.8f,
//        animationSpec = tween(800)
//    )
//
//    // Handle UI state changes
//    LaunchedEffect(uiState) {
//        when (uiState) {
//            is ForgotPasswordViewModel.ForgotPasswordUiState.OtpVerified -> {
//                // Navigate to reset password screen
//                onOtpVerified()
//            }
//            is ForgotPasswordViewModel.ForgotPasswordUiState.Error -> {
//                val message = (uiState as ForgotPasswordViewModel.ForgotPasswordUiState.Error).message
//                errorMessage = message
//            }
//            else -> {
//                errorMessage = null
//            }
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        delay(100)
//        showElements = true
//        // Request focus on first digit
//        focusRequesters[0].requestFocus()
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(NeutralColors.Neutral10)
//    ) {
//        // Background gradient overlay
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    brush = Brush.verticalGradient(
//                        colors = listOf(
//                            NeutralColors.Neutral10,
//                            NeutralColors.Neutral10.copy(alpha = 0.95f),
//                            NeutralColors.Neutral10
//                        )
//                    )
//                )
//        )
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            if (uiState is ForgotPasswordViewModel.ForgotPasswordUiState.Loading) {
//                CircularProgressIndicator(
//                    color = MainColors.Primary1,
//                    modifier = Modifier.size(48.dp)
//                )
//            } else {
//                // Header section with animations
//                Box(
//                    modifier = Modifier
//                        .alpha(titleAlpha.value)
//                        .padding(bottom = 44.dp)
//                ) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Text(
//                            text = "OTP Verification",
//                            style = AppTypography.heading1Bold,
//                            color = MainColors.Primary1
//                        )
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        Text(
//                            text = "Enter the 4-digit code sent to\n$email",
//                            style = AppTypography.heading4Medium,
//                            color = NeutralColors.Neutral70,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    }
//                }
//
//                // OTP input fields with animations
//                Box(modifier = Modifier.alpha(formAlpha.value)) {
//                    Column {
//                        // OTP input row
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceEvenly
//                        ) {
//                            otpDigits.forEachIndexed { index, digit ->
//                                OutlinedTextField(
//                                    value = digit,
//                                    onValueChange = { newValue ->
//                                        if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
//                                            val newDigits = otpDigits.toMutableList().apply {
//                                                this[index] = newValue
//                                            }
//                                            otpDigits = newDigits
//
//                                            if (newValue.isNotEmpty() && index < 3) {
//                                                focusRequesters[index + 1].requestFocus()
//                                            }
//                                        }
//                                    },
//                                    singleLine = true,
//                                    modifier = Modifier
//                                        .width(64.dp)
//                                        .focusRequester(focusRequesters[index]),
//                                    textStyle = AppTypography.heading1Bold.copy(
//                                        textAlign = TextAlign.Center
//                                    ),
//                                    colors = OutlinedTextFieldDefaults.colors(
//                                        focusedBorderColor = MainColors.Primary1,
//                                        unfocusedBorderColor = NeutralColors.Neutral30,
//                                        focusedTextColor = NeutralColors.Neutral70,
//                                        unfocusedTextColor = NeutralColors.Neutral70
//                                    ),
//                                    keyboardOptions = KeyboardOptions(
//                                        keyboardType = KeyboardType.Number,
//                                        imeAction = if (index == 3) ImeAction.Done else ImeAction.Next
//                                    ),
//                                    keyboardActions = KeyboardActions(
//                                        onNext = {
//                                            if (index < 3) focusRequesters[index + 1].requestFocus()
//                                        },
//                                        onDone = {
//                                            focusManager.clearFocus()
//                                            keyboardController?.hide()
//                                        }
//                                    )
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(44.dp))
//
//                        // Verify OTP button
//                        Button(
//                            onClick = {
//                                val otpCode = otpDigits.joinToString("")
//                                if (otpCode.length == 4) {
//                                    focusManager.clearFocus()
//                                    keyboardController?.hide()
//                                    viewModel.verifyOtp(email, otpCode)
//                                } else {
//                                    errorMessage = "Please enter the complete 4-digit code"
//                                }
//                            },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(56.dp)
//                                .scale(buttonScale.value),
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = MainColors.Primary1,
//                                disabledContainerColor = MainColors.Primary1.copy(alpha = 0.6f)
//                            ),
//                            shape = RoundedCornerShape(12.dp),
//                            enabled = uiState !is ForgotPasswordViewModel.ForgotPasswordUiState.Loading
//                        ) {
//                            Box(
//                                contentAlignment = Alignment.Center,
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                Text(
//                                    "Verify Code",
//                                    style = AppTypography.heading4SemiBold,
//                                    color = NeutralColors.Neutral10
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Resend OTP button
//                        TextButton(
//                            onClick = { viewModel.sendOtpToEmail(email) },
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Text(
//                                "Resend Code",
//                                style = AppTypography.heading5Medium,
//                                color = MainColors.Primary1
//                            )
//                        }
//
//                        // Back button
//                        TextButton(
//                            onClick = onBackClick,
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Text(
//                                "Back",
//                                style = AppTypography.heading5Medium,
//                                color = NeutralColors.Neutral70
//                            )
//                        }
//
//                        // Display error message
//                        Spacer(modifier = Modifier.height(32.dp))
//
//                        AnimatedVisibility(
//                            visible = errorMessage != null,
//                            enter = fadeIn(tween(300)),
//                            exit = fadeOut(tween(300))
//                        ) {
//                            errorMessage?.let {
//                                Text(
//                                    text = it,
//                                    style = AppTypography.paragraphRegular,
//                                    color = MaterialTheme.colorScheme.error,
//                                    textAlign = TextAlign.Center,
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 8.dp)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}