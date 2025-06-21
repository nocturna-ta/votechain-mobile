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
import androidx.compose.ui.platform.LocalContext
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
import androidx.navigation.NavController
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.viewmodel.forgotpassword.ForgotPasswordViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ResetPasswordScreen(
    navController: NavController,
    email: String,
    otp: String,
    onBackClick: () -> Unit = { navController.popBackStack() },
    onResetSuccess: () -> Unit = {
        navController.navigate("login") {
            popUpTo("email_verification") { inclusive = true }
        }
    }
) {
    val context = LocalContext.current
    val viewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModel.Factory(context)
    )

    val uiState by viewModel.uiState.collectAsState()

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

    // Password validation functions
    fun validatePassword(password: String): Boolean {
        return password.length >= 8
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword && confirmPassword.isNotEmpty()
    }

    // Handle state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is ForgotPasswordViewModel.ForgotPasswordUiState.PasswordResetSuccess -> {
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
                text = "Reset Password",
                style = AppTypography.heading1Bold,
                color = MainColors.Primary1
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = "Create a new password for your account",
                style = AppTypography.heading4Medium,
                color = NeutralColors.Neutral70,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
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

            // New Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (!isPasswordValid) {
                        isPasswordValid = validatePassword(it)
                    }
                    // Re-validate confirm password if it's already filled
                    if (confirmPassword.isNotEmpty()) {
                        isConfirmPasswordValid = validateConfirmPassword(it, confirmPassword)
                    }
                },
                label = {
                    Text(
                        "New Password",
                        style = AppTypography.paragraphRegular,
                        color = if (isPasswordFocused) MainColors.Primary1 else NeutralColors.Neutral50
                    )
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (passwordVisible) R.drawable.hide else R.drawable.show
                            ),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = NeutralColors.Neutral50
                        )
                    }
                },
                isError = !isPasswordValid && password.isNotEmpty(),
                supportingText = {
                    if (!isPasswordValid && password.isNotEmpty()) {
                        Text(
                            passwordError,
                            style = AppTypography.paragraphRegular,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { confirmPasswordFocusRequester.requestFocus() }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester)
                    .onFocusChanged { focusState ->
                        isPasswordFocused = focusState.isFocused
                        if (!focusState.isFocused && password.isNotEmpty()) {
                            isPasswordValid = validatePassword(password)
                        }
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainColors.Primary1,
                    unfocusedBorderColor = NeutralColors.Neutral30,
                    focusedLabelColor = MainColors.Primary1,
                    unfocusedLabelColor = NeutralColors.Neutral50,
                    cursorColor = MainColors.Primary1
                ),
                textStyle = AppTypography.paragraphRegular
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (!isConfirmPasswordValid) {
                        isConfirmPasswordValid = validateConfirmPassword(password, it)
                    }
                },
                label = {
                    Text(
                        "Confirm New Password",
                        style = AppTypography.paragraphRegular,
                        color = if (isConfirmPasswordFocused) MainColors.Primary1 else NeutralColors.Neutral50
                    )
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (confirmPasswordVisible) R.drawable.hide else R.drawable.show
                            ),
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = NeutralColors.Neutral50
                        )
                    }
                },
                isError = !isConfirmPasswordValid && confirmPassword.isNotEmpty(),
                supportingText = {
                    if (!isConfirmPasswordValid && confirmPassword.isNotEmpty()) {
                        Text(
                            confirmPasswordError,
                            style = AppTypography.paragraphRegular,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (validatePassword(password) && validateConfirmPassword(password, confirmPassword)) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            viewModel.resetPassword(email, otp, password)
                        } else {
                            isPasswordValid = validatePassword(password)
                            isConfirmPasswordValid = validateConfirmPassword(password, confirmPassword)
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmPasswordFocusRequester)
                    .onFocusChanged { focusState ->
                        isConfirmPasswordFocused = focusState.isFocused
                        if (!focusState.isFocused && confirmPassword.isNotEmpty()) {
                            isConfirmPasswordValid = validateConfirmPassword(password, confirmPassword)
                        }
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainColors.Primary1,
                    unfocusedBorderColor = NeutralColors.Neutral30,
                    focusedLabelColor = MainColors.Primary1,
                    unfocusedLabelColor = NeutralColors.Neutral50,
                    cursorColor = MainColors.Primary1
                ),
                textStyle = AppTypography.paragraphRegular
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Reset Password Button
            Button(
                onClick = {
                    if (validatePassword(password) && validateConfirmPassword(password, confirmPassword)) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.resetPassword(email, otp, password)
                    } else {
                        isPasswordValid = validatePassword(password)
                        isConfirmPasswordValid = validateConfirmPassword(password, confirmPassword)
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
                enabled = password.isNotEmpty() &&
                        confirmPassword.isNotEmpty() &&
                        isPasswordValid &&
                        isConfirmPasswordValid &&
                        uiState !is ForgotPasswordViewModel.ForgotPasswordUiState.Loading
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
                            "Reset Password",
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