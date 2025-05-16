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

@Composable
fun PasswordConfirmationDialog(
    onCancel: () -> Unit,
    onSubmit: (String) -> Unit,
    isOpen: Boolean
) {
    if (!isOpen) return

    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Password validation
    val isPasswordValid = password.length >= 8
    val passwordError = "Password must be at least 8 characters"

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Enter Your Password",
                    style = AppTypography.heading3Bold,
                    color = MainColors.Primary1,
                    textAlign = TextAlign.Center
                )

                // Description
                Text(
                    text = "Please enter your password to proceed. This ensures that your account remain secure",
                    style = AppTypography.heading6Regular,
                    color = NeutralColors.Neutral70,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter your password", style = AppTypography.paragraphRegular) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MainColors.Primary1,
                        unfocusedBorderColor = NeutralColors.Neutral30,
                        focusedTextColor = NeutralColors.Neutral70,
                        unfocusedTextColor = NeutralColors.Neutral30,
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

                // Buttons row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NeutralColors.Neutral60
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(NeutralColors.Neutral30)
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            style = AppTypography.heading5Medium
                        )
                    }

                    // Submit Button
                    Button(
                        onClick = { onSubmit(password) },
                        enabled = isPasswordValid,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MainColors.Primary1,
                            contentColor = Color.White,
                            disabledContainerColor = NeutralColors.Neutral30,
                            disabledContentColor = NeutralColors.Neutral50
                        )
                    ) {
                        Text(
                            text = "Submit",
                            style = AppTypography.heading5Medium
                        )
                    }
                }
            }
        }
    }
}