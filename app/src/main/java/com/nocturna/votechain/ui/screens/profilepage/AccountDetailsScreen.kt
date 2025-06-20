package com.nocturna.votechain.ui.screens.profilepage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
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
import com.nocturna.votechain.data.repository.EnhancedUserRepository
import com.nocturna.votechain.data.storage.WalletManager
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.login.LoginViewModel
import kotlinx.coroutines.launch
import org.web3j.utils.Convert
import java.math.BigDecimal

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
    val scope = rememberCoroutineScope()

    // Repository instances
    val userProfileRepository = remember { UserProfileRepository(context) }
    val userLoginRepository = remember { UserLoginRepository(context) }
    val voterRepository = remember { VoterRepository(context) }
    val enhancedUserRepository = remember { EnhancedUserRepository(context) }
    val walletManager = remember { WalletManager.getInstance(context) }

    // Get LoginViewModel instance
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory(context))

    // State management
    var completeUserProfile by remember { mutableStateOf(userProfileRepository.getSavedCompleteProfile()) }
    var fallbackVoterData by remember { mutableStateOf(voterRepository.getVoterDataLocally()) }
    var dataLoadError by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPrivateKey by remember { mutableStateOf(false) }
    var showCopiedMessage by remember { mutableStateOf(false) }

    // Wallet-specific state
    var walletData by remember { mutableStateOf<com.nocturna.votechain.data.model.WalletData?>(null) }
    var walletBalance by remember { mutableStateOf("Loading...") }
    var isLoadingWallet by remember { mutableStateOf(true) }
    var walletError by remember { mutableStateOf<String?>(null) }
    var showWalletDialog by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Load profile and wallet data
    LaunchedEffect(Unit) {
        // Load profile data
        userProfileRepository.fetchCompleteUserProfile().fold(
            onSuccess = { profile ->
                completeUserProfile = profile
                dataLoadError = null
            },
            onFailure = { error ->
                dataLoadError = error.message
                completeUserProfile = userProfileRepository.getSavedCompleteProfile()
                if (completeUserProfile?.voterProfile == null) {
                    fallbackVoterData = voterRepository.getVoterDataLocally()
                }
            }
        )

        // Load wallet data
        scope.launch {
            try {
                isLoadingWallet = true
                walletError = null

                val userEmail = userLoginRepository.getUserEmail()
                val voterData = completeUserProfile?.voterProfile ?: fallbackVoterData

                if (userEmail.isNotEmpty() && voterData?.nik?.isNotEmpty() == true) {
                    // Try to setup wallet access
                    val walletSetupSuccess = enhancedUserRepository.setupWalletAccessAfterLogin(
                        userEmail,
                        voterData.nik
                    )

                    if (walletSetupSuccess) {
                        // Get the wallet address for this user
                        val walletAddress = enhancedUserRepository.getWalletAddressForUser(userEmail)

                        if (walletAddress != null) {
                            // Load wallet with registration PIN
                            val pin = enhancedUserRepository.getRegistrationPinForUser(userEmail, voterData.nik)
                            val wallet = walletManager.loadWallet(walletAddress, pin)

                            if (wallet != null) {
                                walletData = wallet
                                // Format balance
                                val balanceEth = Convert.fromWei(wallet.balance.toString(), Convert.Unit.ETHER)
                                walletBalance = "${balanceEth.setScale(4, BigDecimal.ROUND_DOWN)} ETH"
                            } else {
                                walletError = "Failed to load wallet data"
                            }
                        } else {
                            walletError = "No wallet found for this account"
                        }
                    } else {
                        walletError = "Failed to setup wallet access"
                    }
                } else {
                    walletError = "Missing user information for wallet access"
                }
            } catch (e: Exception) {
                walletError = "Error loading wallet: ${e.message}"
            } finally {
                isLoadingWallet = false
            }
        }
    }

    // Auto-hide copied message
    LaunchedEffect(showCopiedMessage) {
        if (showCopiedMessage) {
            kotlinx.coroutines.delay(2000)
            showCopiedMessage = false
        }
    }

    // Extract data
    val voterData = completeUserProfile?.voterProfile ?: fallbackVoterData
    val userProfile = completeUserProfile?.userProfile
    val nik = voterData?.nik ?: "No NIK available"

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
                    }
                ) {
                    Text("Logout", color = Color(0xFFE53E3E))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Wallet Setup Dialog
    if (showWalletDialog) {
        AlertDialog(
            onDismissRequest = { showWalletDialog = false },
            title = { Text("Setup Wallet") },
            text = { Text("Would you like to access the advanced wallet features? This will take you to the wallet management screen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWalletDialog = false
                        // Navigate to wallet navigation
                        navController.navigate("wallet_setup")
                    }
                ) {
                    Text("Continue", color = MainColors.Primary1)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWalletDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Back",
                        tint = MainColors.Primary1
                    )
                }
                Text(
                    text = "Account Details",
                    style = AppTypography.heading4Bold,
                    color = MainColors.Primary1,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Information Section
            Text(
                text = "Account Information",
                style = AppTypography.heading5Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // NIK Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NeutralColors.Neutral80),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "National ID Number (NIK)",
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral40
                    )
                    Text(
                        text = nik,
                        style = AppTypography.paragraphSemiBold,
                        color = NeutralColors.Neutral10
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Wallet Information Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wallet Information",
                    style = AppTypography.heading5Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { showWalletDialog = true }
                ) {
                    Text(
                        text = "Advanced",
                        style = AppTypography.paragraphRegular,
                        color = MainColors.Primary1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoadingWallet) {
                // Loading state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeutralColors.Neutral80),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MainColors.Primary1)
                    }
                }
            } else if (walletError != null) {
                // Error state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Wallet Error",
                            style = AppTypography.paragraphSemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = walletError!!,
                            style = AppTypography.paragraphRegular,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (walletData != null) {
                // Wallet data loaded successfully

                // Balance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MainColors.Primary1),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Balance",
                            style = AppTypography.paragraphRegular,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = walletBalance,
                            style = AppTypography.heading4Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Public Key Card
                WalletDetailCard(
                    title = "Public Key",
                    value = walletData!!.address,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(walletData!!.address))
                        showCopiedMessage = true
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Private Key Card
                WalletDetailCard(
                    title = "Private Key",
                    value = if (showPrivateKey) walletData!!.privateKey else "•".repeat(64),
                    onCopy = {
                        if (showPrivateKey) {
                            clipboardManager.setText(AnnotatedString(walletData!!.privateKey))
                            showCopiedMessage = true
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { showPrivateKey = !showPrivateKey }
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (showPrivateKey) R.drawable.hide else R.drawable.show
                                ),
                                contentDescription = if (showPrivateKey) "Hide" else "Show",
                                tint = MainColors.Primary1
                            )
                        }
                    }
                )

                // Warning about private key
                if (showPrivateKey) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Never share your private key with anyone!",
                            style = AppTypography.paragraphRegular,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Logout Button - Fixed at bottom
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53E3E),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Logout",
                style = AppTypography.paragraphRegular,
                color = Color.White
            )
        }

        // Copied Message Snackbar
        if (showCopiedMessage) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NeutralColors.Neutral70
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = AppTypography.paragraphRegular.copy(color = Color.White),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun WalletDetailCard(
    title: String,
    value: String,
    onCopy: () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NeutralColors.Neutral80),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = AppTypography.paragraphRegular,
                    color = NeutralColors.Neutral40
                )
                Row {
                    trailingIcon?.invoke()
                    IconButton(onClick = onCopy) {
                        Icon(
                            painter = painterResource(id = R.drawable.copy),
                            contentDescription = "Copy",
                            tint = MainColors.Primary1
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = AppTypography.paragraphRegular,
                color = NeutralColors.Neutral10,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountDetailsPreview() {
    MaterialTheme {
        AccountDetailsScreen(navController = NavController(LocalContext.current))
    }
}