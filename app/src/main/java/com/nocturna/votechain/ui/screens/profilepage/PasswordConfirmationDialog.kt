package com.nocturna.votechain.ui.screens.profilepage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nocturna.votechain.R
import androidx.compose.ui.res.painterResource
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.DangerColors
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.utils.LanguageManager
import androidx.compose.ui.platform.LocalContext
import com.nocturna.votechain.data.repository.UserLoginRepository

@Composable
fun PasswordConfirmationDialog(
    isOpen: Boolean,
    onCancel: () -> Unit,
    onSubmit: (String) -> Unit,
    userLoginRepository: UserLoginRepository
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    if (isOpen) {
        Dialog(onDismissRequest = onCancel) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "Enter Password",
                        style = AppTypography.heading4Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Subtitle
                    Text(
                        text = "Please enter your password to view account details",
                        style = AppTypography.paragraphRegular,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            isError = false
                        },
                        label = { Text("Password") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (showPassword) R.drawable.show else R.drawable.hide
                                    ),
                                    contentDescription = if (showPassword) "Hide password" else "Show password",
                                    tint = NeutralColors.Neutral40
                                )
                            }
                        },
                        isError = isError,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MainColors.Primary1,
                            unfocusedBorderColor = NeutralColors.Neutral30,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        singleLine = true
                    )

                    if (isError) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = AppTypography.smallParagraphRegular,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button
                        OutlinedButton(
                            onClick = {
                                password = ""
                                isError = false
                                errorMessage = ""
                                onCancel()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Cancel")
                        }

                        // Submit button
                        Button(
                            onClick = {
                                if (password.isNotEmpty()) {
                                    // Verify password against stored hash
                                    if (userLoginRepository.verifyPassword(password)) {
                                        // Password is correct
                                        onSubmit(password)
                                        password = ""
                                        isError = false
                                        errorMessage = ""
                                    } else {
                                        // Password is incorrect
                                        isError = true
                                        errorMessage = "Incorrect password. Please try again."
                                    }
                                } else {
                                    isError = true
                                    errorMessage = "Password cannot be empty"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColors.Primary1
                            )
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}