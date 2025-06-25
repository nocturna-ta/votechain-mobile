package com.nocturna.votechain.ui.screens.profilepage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.AccountDisplayData
import com.nocturna.votechain.data.repository.UserLoginRepository
import com.nocturna.votechain.data.repository.UserProfileRepository
import com.nocturna.votechain.data.repository.VoterRepository
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.screens.login.LoginScreen
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.DangerColors
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.login.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val strings = LanguageManager.getLocalizedStrings()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Repository instances
    val userProfileRepository = remember { UserProfileRepository(context) }
    val userLoginRepository = remember { UserLoginRepository(context) }
    val voterRepository = remember { VoterRepository(context) }

    // Get LoginViewModel instance
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory(context))

    // State for account data
    var accountData by remember { mutableStateOf(AccountDisplayData()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showPrivateKey by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // State untuk profile data dengan fallback
//    var completeUserProfile by remember { mutableStateOf(userProfileRepository.getSavedCompleteProfile()) }
//    var fallbackVoterData by remember { mutableStateOf(voterRepository.getVoterDataLocally()) }
//    var dataLoadError by remember { mutableStateOf<String?>(null) }

    // For copy to clipboard functionality
    val clipboardManager = LocalClipboardManager.current
    var showCopiedMessage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Function to load account data
    suspend fun loadAccountData() {
        try {
            isLoading = true
            errorMessage = null

            // Get complete user profile first
            userProfileRepository.fetchCompleteUserProfile().fold(
                onSuccess = { profile ->
                    // Profile loaded successfully, now get account data
                    val displayData = voterRepository.getAccountDisplayData()
                    accountData = displayData.copy(
                        email = profile.userProfile?.email ?: ""
                    )
                },
                onFailure = { error ->
                    // Use fallback data if profile fetch fails
                    val displayData = voterRepository.getAccountDisplayData()
                    accountData = displayData.copy(
                        email = userLoginRepository.getUserEmail() ?: ""
                    )
                    errorMessage = "Some data may be outdated: ${error.message}"
                }
            )
        } catch (e: Exception) {
            errorMessage = "Failed to load account data: ${e.message}"
            // Set default values on error
            accountData = AccountDisplayData(
                errorMessage = errorMessage
            )
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    // Function to refresh balance
    fun refreshBalance() {
        scope.launch {
            try {
                isRefreshing = true
                val newBalance = voterRepository.refreshBalance()
                accountData = accountData.copy(
                    ethBalance = newBalance,
                    errorMessage = null
                )

                // Show success message
                snackbarHostState.showSnackbar("Balance updated successfully")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Failed to refresh balance: ${e.message}")
            } finally {
                isRefreshing = false
            }
        }
    }

    // Function to copy text to clipboard
    fun copyToClipboard(text: String, label: String) {
        clipboardManager.setText(AnnotatedString(text))
        scope.launch {
            snackbarHostState.showSnackbar("$label copied to clipboard")
        }
    }

    // Password confirmation dialog for private key access
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Logout",
                    style = AppTypography.heading4Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout from your account?",
                    style = AppTypography.paragraphRegular,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        loginViewModel.logoutUser()
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = NeutralColors.Neutral40
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Load account data when screen opens
    LaunchedEffect(Unit) {
        loadAccountData()
    }

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
                    .clickable(onClick = onBackClick)
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = strings.back,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Centered title
            Text(
                text = strings.profileNav,
                style = AppTypography.heading4Regular,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Show loading indicator when loading
        if (isLoading) {
            LoadingScreen()
        }
        // Show error message when there's an error
        else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Balance
                    Text(
                        text = strings.balance,
                        style = AppTypography.heading5Regular,
                        color = NeutralColors.Neutral70,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = "${accountData.ethBalance} ETH",
                        onValueChange = { },
                        readOnly = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = NeutralColors.Neutral30,
                            unfocusedTextColor = NeutralColors.Neutral50,
                            disabledBorderColor = NeutralColors.Neutral30,
                            disabledTextColor = NeutralColors.Neutral70,
                            focusedBorderColor = MainColors.Primary1,
                            focusedTextColor = NeutralColors.Neutral50,
                        ),
                        textStyle = AppTypography.heading5Regular
                    )

                    // NIK
                    Text(
                        text = strings.nik,
                        style = AppTypography.heading5Regular,
                        color = NeutralColors.Neutral70,
                        modifier = Modifier.padding(bottom = 8.dp, top = 24.dp)
                    )

                    OutlinedTextField(
                        value = accountData.nik,
                        onValueChange = { },
                        readOnly = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = NeutralColors.Neutral30,
                            unfocusedTextColor = NeutralColors.Neutral50,
                            disabledBorderColor = NeutralColors.Neutral30,
                            disabledTextColor = NeutralColors.Neutral70,
                            focusedBorderColor = MainColors.Primary1,
                            focusedTextColor = NeutralColors.Neutral50,
                        ),
                        textStyle = AppTypography.heading5Regular
                    )

                    // Full Name
                    Text(
                        text = "Full Name",
                        style = AppTypography.heading5Regular,
                        color = NeutralColors.Neutral70,
                        modifier = Modifier.padding(bottom = 8.dp, top = 24.dp)
                    )

                    OutlinedTextField(
                        value = accountData.fullName,
                        onValueChange = { },
                        readOnly = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = NeutralColors.Neutral30,
                            unfocusedTextColor = NeutralColors.Neutral50,
                            disabledBorderColor = NeutralColors.Neutral30,
                            disabledTextColor = NeutralColors.Neutral70,
                            focusedBorderColor = MainColors.Primary1,
                            focusedTextColor = NeutralColors.Neutral50,
                        ),
                        textStyle = AppTypography.heading5Regular
                    )

                    // Private Key
                    Text(
                        text = strings.privateKey,
                        style = AppTypography.heading5Regular,
                        color = NeutralColors.Neutral70,
                        modifier = Modifier.padding(bottom = 8.dp, top = 24.dp)
                    )

                    OutlinedTextField(
                        value =
                            if (showPrivateKey && accountData.privateKey.isNotEmpty()) {
                                accountData.privateKey
                            } else {
                                "••••••••••••••••••••••••••••••••"
                            },
                        onValueChange = { },
                        readOnly = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPrivateKey) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = NeutralColors.Neutral30,
                            unfocusedTextColor = NeutralColors.Neutral50,
                            disabledBorderColor = NeutralColors.Neutral30,
                            disabledTextColor = NeutralColors.Neutral70,
                            focusedBorderColor = MainColors.Primary1,
                            focusedTextColor = NeutralColors.Neutral50,
                            unfocusedTrailingIconColor = NeutralColors.Neutral40,
                            focusedTrailingIconColor = NeutralColors.Neutral40,
                        ),
                        textStyle = AppTypography.heading5Regular,
                        trailingIcon = {
                            IconButton(onClick = { showPrivateKey = !showPrivateKey }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (showPrivateKey) R.drawable.show else R.drawable.hide
                                    ),
                                    contentDescription = if (showPrivateKey) "Hide private key" else "Show private key",
                                    tint = NeutralColors.Neutral40
                                )
                            }
                        }
                    )

                    // Public Key
                    Text(
                        text = strings.publicKey,
                        style = AppTypography.heading5Regular,
                        color = NeutralColors.Neutral70,
                        modifier = Modifier.padding(bottom = 8.dp, top = 24.dp)
                    )

                    OutlinedTextField(
                        value = accountData.voterAddress,
                        onValueChange = { },
                        readOnly = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = NeutralColors.Neutral30,
                            unfocusedTextColor = NeutralColors.Neutral50,
                            disabledBorderColor = NeutralColors.Neutral30,
                            disabledTextColor = NeutralColors.Neutral70,
                            focusedBorderColor = MainColors.Primary1,
                            focusedTextColor = NeutralColors.Neutral50,
                            unfocusedTrailingIconColor = NeutralColors.Neutral40,
                            focusedTrailingIconColor = NeutralColors.Neutral40,
                        ),
                        textStyle = AppTypography.heading5Regular,
                        trailingIcon = {
                            IconButton(onClick = {
                                copyToClipboard(accountData.publicKey, "Public Key")
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.copy),
                                    contentDescription = "Copy public key",
                                    tint = NeutralColors.Neutral40
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(120.dp))

                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerColors.Danger70,
                            contentColor = NeutralColors.Neutral10
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Logout",
                                style = AppTypography.heading5Medium,
                                color = NeutralColors.Neutral10
                            )
                        }
                    }
                }
            }
        }
    }
}