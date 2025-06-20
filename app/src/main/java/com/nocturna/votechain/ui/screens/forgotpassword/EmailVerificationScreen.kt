//package com.nocturna.votechain.ui.screens.forgotpassword
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.ExperimentalComposeUiApi
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.nocturna.votechain.ui.theme.AppTypography
//import com.nocturna.votechain.ui.theme.MainColors
//import com.nocturna.votechain.ui.theme.NeutralColors
//import com.nocturna.votechain.utils.LanguageManager
//import com.nocturna.votechain.viewmodel.forgotpassword.ForgotPasswordViewModel
//
//@OptIn(ExperimentalComposeUiApi::class)
//@Composable
//fun EmailVerificationScreen(
//    onBackClick: () -> Unit,
//    onSendOTPClick: (String) -> Unit,
//    viewModel: ForgotPasswordViewModel = viewModel()
//) {
//    val strings = LanguageManager.getLocalizedStrings()
//    val uiState by viewModel.uiState.collectAsState()
//
//    var email by remember { mutableStateOf("") }
//    var isEmailFocused by remember { mutableStateOf(false) }
//    var isEmailValid by remember { mutableStateOf(true) }
//
//    val emailError = "Please enter a valid email address"
//    val emailFocusRequester = remember { FocusRequester() }
//    val focusManager = LocalFocusManager.current
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    // Email validator function
//    val validateEmail: (String) -> Boolean = { input ->
//        input.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
//    }
//
//    // Handle state changes
//    LaunchedEffect(uiState) {
//        when (uiState) {
//            is ForgotPasswordViewModel.ForgotPasswordUiState.OtpSent -> {
//                onSendOTPClick(email)
//            }
//            else -> {}
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(NeutralColors.Neutral10)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Top
//        ) {
//            // Back button and header
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Start
//            ) {
//                TextButton(onClick = onBackClick) {
//                    Text(
//                        text = "← Back",
//                        style = AppTypography.heading6Medium,
//                        color = MainColors.Primary1
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(40.dp))
//
//            // Header
//            Text(
//                text = "Forgot Password",
//                style = AppTypography.heading1Bold,
//                color = MainColors.Primary1
//            )
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Description
//            Text(
//                text = "Enter your email address to receive a verification code",
//                style = AppTypography.heading4Medium,
//                color = NeutralColors.Neutral70,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.padding(horizontal = 16.dp)
//            )
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            // Display error message if any
//            if (uiState is ForgotPasswordViewModel.ForgotPasswordUiState.Error) {
//                Text(
//                    text = (uiState as ForgotPasswordViewModel.ForgotPasswordUiState.Error).message,
//                    style = AppTypography.paragraphRegular,
//                    color = MaterialTheme.colorScheme.error,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp)
//                )
//            }
//
//            // Email field
//            OutlinedTextField(
//                value = email,
//                onValueChange = {
//                    email = it
//                    isEmailValid = validateEmail(it)
//                },
//                label = { Text("Email") },
//                singleLine = true,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .focusRequester(emailFocusRequester)
//                    .onFocusChanged { isEmailFocused = it.isFocused },
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = MainColors.Primary1,
//                    unfocusedBorderColor = NeutralColors.Neutral30,
//                    focusedTextColor = NeutralColors.Neutral70,
//                    unfocusedTextColor = NeutralColors.Neutral70,
//                    errorBorderColor = MaterialTheme.colorScheme.error,
//                    errorLabelColor = MaterialTheme.colorScheme.error
//                ),
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Email,
//                    imeAction = ImeAction.Done
//                ),
//                keyboardActions = KeyboardActions(
//                    onDone = {
//                        focusManager.clearFocus()
//                        keyboardController?.hide()
//                    }
//                ),
//                isError = !isEmailValid,
//                supportingText = {
//                    if (!isEmailValid) {
//                        Text(
//                            text = emailError,
//                            color = MaterialTheme.colorScheme.error,
//                            style = AppTypography.paragraphRegular
//                        )
//                    }
//                }
//            )
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            // Send OTP Button
//            Button(
//                onClick = {
//                    if (validateEmail(email) && email.isNotEmpty()) {
//                        focusManager.clearFocus()
//                        keyboardController?.hide()
//                        viewModel.sendOtpToEmail(email)
//                    } else {
//                        isEmailValid = validateEmail(email) && email.isNotEmpty()
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MainColors.Primary1,
//                    disabledContainerColor = MainColors.Primary1.copy(alpha = 0.6f)
//                ),
//                shape = RoundedCornerShape(12.dp),
//                enabled = email.isNotEmpty() && isEmailValid && uiState !is ForgotPasswordViewModel.ForgotPasswordUiState.Loading
//            ) {
//                Box(
//                    contentAlignment = Alignment.Center,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    if (uiState is ForgotPasswordViewModel.ForgotPasswordUiState.Loading) {
//                        CircularProgressIndicator(
//                            color = NeutralColors.Neutral10,
//                            strokeWidth = 2.dp,
//                            modifier = Modifier.size(24.dp)
//                        )
//                    } else {
//                        Text(
//                            "Send OTP",
//                            style = AppTypography.heading4SemiBold,
//                            color = NeutralColors.Neutral10
//                        )
//                    }
//                }
//            }
//        }
//    }
//}