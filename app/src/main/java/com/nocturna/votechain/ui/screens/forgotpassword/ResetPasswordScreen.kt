package com.nocturna.votechain.ui.screens.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.viewmodel.forgotpassword.ForgotPasswordViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ResetPasswordScreen(
    email: String,
    otp: String,
    onBackClick: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var isConfirmPasswordFocused by remember { mutableStateOf(false) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var isConfirmPasswordValid by remember { mutableStateOf(true) }

    val passwordError = "Password must be at least 8 characters"
    val confirmPasswordError = "Passwords do not match"

    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Validator functions
    val validatePassword: (String) -> Boolean = { input ->
        input.isEmpty() || input.length >= 8
    }

    val validateConfirmPassword: (String) -> Boolean = { input ->
        input.isEmpty() || input == password
    }

    // Handle state changes
    LaunchedEffect(state) {
        when (state) {
            is ForgotPasswordViewModel.ForgotPasswordState.PasswordReset -> {
                onResetSuccess()
            }
            else -> {}
        }
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
                text = "Create New Password",
                style = AppTypography.heading1Bold,
                color = MainColors.Primary1
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = "Create a secure new password for your account",
                style = AppTypography.heading4Medium,
                color = NeutralColors.Neutral70,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
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

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    isPasswordValid = validatePassword(it)
                    if (confirmPassword.isNotEmpty()) {
                        isConfirmPasswordValid = confirmPassword == it
                    }
                },
                label = { Text("New Password") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester)
                    .onFocusChanged { isPasswordFocused = it.isFocused },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainColors.Primary1,
                    unfocusedBorderColor = NeutralColors.Neutral30,
                    focusedTextColor = NeutralColors.Neutral70,
                    unfocusedTextColor = NeutralColors.Neutral70,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { confirmPasswordFocusRequester.requestFocus() }
                ),
                isError = !isPasswordValid,
                supportingText = {
                    if (!isPasswordValid) {
                        Text(
                            text = passwordError,
                            color = MaterialTheme.colorScheme.error,
                            style = AppTypography.paragraphRegular
                        )
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (passwordVisible) R.drawable.show else R.drawable.hide
                            ),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = NeutralColors.Neutral30
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    isConfirmPasswordValid = validateConfirmPassword(it)
                },
                label = { Text("Confirm Password") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmPasswordFocusRequester)
                    .onFocusChanged { isConfirmPasswordFocused = it.isFocused },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainColors.Primary1,
                    unfocusedBorderColor = NeutralColors.Neutral30,
                    focusedTextColor = NeutralColors.Neutral70,
                    unfocusedTextColor = NeutralColors.Neutral70,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error
                ),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                isError = !isConfirmPasswordValid,
                supportingText = {
                    if (!isConfirmPasswordValid) {
                        Text(
                            text = confirmPasswordError,
                            color = MaterialTheme.colorScheme.error,
                            style = AppTypography.paragraphRegular
                        )
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (confirmPasswordVisible) R.drawable.show else R.drawable.hide
                            ),
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = NeutralColors.Neutral30
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Reset Password Button
            Button(
                onClick = {
                    if (validatePassword(password) && validateConfirmPassword(confirmPassword) &&
                        password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.resetPassword(email, otp, password)
                    } else {
                        isPasswordValid = validatePassword(password) && password.isNotEmpty()
                        isConfirmPasswordValid = validateConfirmPassword(confirmPassword) && confirmPassword.isNotEmpty()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColors.Primary1,
                    disabledContainerColor = MainColors.Primary1.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = password.isNotEmpty() && confirmPassword.isNotEmpty() &&
                         isPasswordValid && isConfirmPasswordValid && state !is ForgotPasswordViewModel.ForgotPasswordState.Loading
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
                            "Reset Password",
                            style = AppTypography.heading4SemiBold,
                            color = NeutralColors.Neutral10
                        )
                    }
                }
            }
        }
    }
}
