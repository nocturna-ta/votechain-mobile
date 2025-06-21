package com.nocturna.votechain.ui.screens.profilepage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.coroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nocturna.votechain.R
import com.nocturna.votechain.data.repository.UserLoginRepository
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.utils.LanguageManager
import kotlinx.coroutines.launch

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

    val strings = LanguageManager.getLocalizedStrings()
    val coroutineScope = rememberCoroutineScope()

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
                        text = strings.passwordConfirmationTitle,
                        style = AppTypography.heading4SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Subtitle
                    Text(
                        text = strings.passwordConfirmationSubtitle,
                        style = AppTypography.paragraphRegular,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            isError = false
                        },
                        label = { Text(strings.password) },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (showPassword) R.drawable.show else R.drawable.hide
                                    ),
                                    contentDescription = if (showPassword) "Hide password" else "Show password",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        isError = isError,
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MainColors.Primary1,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            unfocusedLabelColor = NeutralColors.Neutral40
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
                            modifier = Modifier
                                .weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(MaterialTheme.colorScheme.outline)
                            )
                        ) {
                            Text(
                                text = strings.passwordConfirmationCancel,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        // Submit button
                        Button(
                            onClick = {
                                if (password.isNotEmpty()) {
                                    // Verify password against stored hash
                                    coroutineScope.launch {
                                        // Verify password against stored hash
                                        if (userLoginRepository.verifyUserPassword(password)) {
                                            // Password is correct
                                            onSubmit(password)
                                            password = ""
                                            isError = false
                                            errorMessage = ""
                                        } else {
                                            // Password is incorrect
                                            isError = true
                                            errorMessage = strings.passwordIncorrect
                                        }
                                    }
                                } else {
                                    isError = true
                                    errorMessage = strings.passwordEmpty
                                }
                            },
                            modifier = Modifier
                                .weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MainColors.Primary1
                            )
                        ) {
                            Text(
                                text = strings.passwordConfirmationSubmit,
                                color = NeutralColors.Neutral10
                            )
                        }
                    }
                }
            }
        }
    }
}