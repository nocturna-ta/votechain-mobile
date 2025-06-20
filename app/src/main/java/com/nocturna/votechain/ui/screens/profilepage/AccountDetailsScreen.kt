package com.nocturna.votechain.ui.screens.profilepage

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
import com.nocturna.votechain.data.repository.UserLoginRepository
import com.nocturna.votechain.data.repository.UserProfileRepository
import com.nocturna.votechain.data.repository.VoterRepository
import com.nocturna.votechain.ui.screens.login.LoginScreen
import com.nocturna.votechain.ui.theme.AppTypography
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
    onLogout: () -> Unit = {}
) {
    val strings = LanguageManager.getLocalizedStrings()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Repository instances
    val userProfileRepository = remember { UserProfileRepository(context) }
    val userLoginRepository = remember { UserLoginRepository(context) }
    val voterRepository = remember { VoterRepository(context) }

    // Get LoginViewModel instance
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory(context))

    // State untuk profile data dengan fallback
    var completeUserProfile by remember { mutableStateOf(userProfileRepository.getSavedCompleteProfile()) }
    var fallbackVoterData by remember { mutableStateOf(voterRepository.getVoterDataLocally()) }
    var dataLoadError by remember { mutableStateOf<String?>(null) }

    // State for logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Refresh profile data saat screen dibuka
    LaunchedEffect(Unit) {
        userProfileRepository.fetchCompleteUserProfile().fold(
            onSuccess = { profile ->
                completeUserProfile = profile
                dataLoadError = null
            },
            onFailure = { error ->
                dataLoadError = error.message
                // Gunakan saved profile sebagai fallback
                completeUserProfile = userProfileRepository.getSavedCompleteProfile()

                // Jika masih tidak ada, gunakan local voter data
                if (completeUserProfile?.voterProfile == null) {
                    fallbackVoterData = voterRepository.getVoterDataLocally()
                }
            }
        )
    }

    // Extract data dari complete profile
    val voterData = completeUserProfile?.voterProfile
    val userProfile = completeUserProfile?.userProfile

    // Data dari voter atau default values
    val balance = "0.0000" // Placeholder balance
    val nik = voterData?.nik ?: "No NIK available"
    val privateKey = "Not available yet" // Placeholder private key
    val publicKey = voterData?.voter_address ?: "No public key available"

    var showPrivateKey by remember { mutableStateOf(false) }

    // For copy to clipboard functionality
    val clipboardManager = LocalClipboardManager.current
    var showCopiedMessage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Logout Dialog
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
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Account",
                        style = AppTypography.heading4Regular,
                        color = NeutralColors.Neutral90
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = strings.back,
                            tint = MainColors.Primary1
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            // Balance
            Text(
                text = strings.balance,
                style = AppTypography.heading5Regular,
                color = NeutralColors.Neutral70,
                modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
            )

            OutlinedTextField(
                value = balance,
                onValueChange = { },
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = NeutralColors.Neutral30,
                    unfocusedTextColor = NeutralColors.Neutral70,
                    disabledBorderColor = NeutralColors.Neutral30,
                    disabledTextColor = NeutralColors.Neutral70,
                    focusedBorderColor = MainColors.Primary1,
                    focusedTextColor = NeutralColors.Neutral70,
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
                value = nik,
                onValueChange = { },
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = NeutralColors.Neutral30,
                    unfocusedTextColor = NeutralColors.Neutral70,
                    disabledBorderColor = NeutralColors.Neutral30,
                    disabledTextColor = NeutralColors.Neutral70,
                    focusedBorderColor = MainColors.Primary1,
                    focusedTextColor = NeutralColors.Neutral70,
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
                value = voterData?.full_name ?: "No name available",
                onValueChange = { },
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = NeutralColors.Neutral30,
                    unfocusedTextColor = NeutralColors.Neutral70,
                    disabledBorderColor = NeutralColors.Neutral30,
                    disabledTextColor = NeutralColors.Neutral70,
                    focusedBorderColor = MainColors.Primary1,
                    focusedTextColor = NeutralColors.Neutral70,
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
                value = privateKey,
                onValueChange = { },
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPrivateKey) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = NeutralColors.Neutral30,
                    unfocusedTextColor = NeutralColors.Neutral70,
                    disabledBorderColor = NeutralColors.Neutral30,
                    disabledTextColor = NeutralColors.Neutral70,
                    focusedBorderColor = MainColors.Primary1,
                    focusedTextColor = NeutralColors.Neutral70,
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
                value = publicKey,
                onValueChange = { },
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = NeutralColors.Neutral30,
                    unfocusedTextColor = NeutralColors.Neutral70,
                    disabledBorderColor = NeutralColors.Neutral30,
                    disabledTextColor = NeutralColors.Neutral70,
                    focusedBorderColor = MainColors.Primary1,
                    focusedTextColor = NeutralColors.Neutral70,
                    unfocusedTrailingIconColor = NeutralColors.Neutral40,
                    focusedTrailingIconColor = NeutralColors.Neutral40,
                ),
                textStyle = AppTypography.heading5Regular,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(publicKey))
                            scope.launch {
                                showCopiedMessage = true
                                snackbarHostState.showSnackbar(
                                    message = "Public Key copied to clipboard",
                                    duration = SnackbarDuration.Short
                                )
                                delay(2000)
                                showCopiedMessage = false
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.copy),
                            contentDescription = "Copy public key",
                            tint = NeutralColors.Neutral40
                        )
                    }
                }
            )

            // Add spacer at the bottom for better scrolling experience
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Logout Button - Fixed at bottom
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53E3E),
                contentColor = Color.White
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
                    style = AppTypography.paragraphRegular,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountDetailsPreview() {
    MaterialTheme {
        AccountDetailsScreen(navController = NavController(LocalContext.current), modifier = Modifier, onLogout = {})
    }
}