package com.nocturna.votechain.ui.screens.profilepage

import android.util.Log
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
import com.nocturna.votechain.VoteChainApplication
import com.nocturna.votechain.data.model.AccountDisplayData
import com.nocturna.votechain.data.repository.UserLoginRepository
import com.nocturna.votechain.data.repository.UserProfileRepository
import com.nocturna.votechain.data.repository.VoterRepository
import com.nocturna.votechain.security.CryptoKeyManager
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.screens.login.LoginScreen
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.DangerColors
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.viewmodel.login.LoginViewModel
import com.nocturna.votechain.viewmodel.login.LoginViewModel.KeyIntegrityStatus
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
    val cryptoKeyManager = remember { CryptoKeyManager(context) }

    // Get LoginViewModel instance
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory(context))

    // State for account data
    var accountData by remember { mutableStateOf(AccountDisplayData()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isKeyLoading by remember { mutableStateOf(false) }
    var showPrivateKey by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var keyIntegrityStatus by remember { mutableStateOf<KeyIntegrityStatus?>(null) }
    var keyDiagnostics by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Enhanced error states
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var keyErrorMessage by remember { mutableStateOf<String?>(null) }

    // For copy to clipboard functionality
    val clipboardManager = LocalClipboardManager.current
    var showCopiedMessage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    /**
     * Derive voter address from public key
     */
    fun deriveVoterAddressFromPublicKey(publicKey: String): String {
        return try {
            val cleanPublicKey = if (publicKey.startsWith("0x")) {
                publicKey.substring(2)
            } else {
                publicKey
            }

            val publicKeyBigInt = java.math.BigInteger(cleanPublicKey, 16)
            val addressHex = org.web3j.crypto.Keys.getAddress(publicKeyBigInt)
            org.web3j.crypto.Keys.toChecksumAddress("0x" + addressHex)
        } catch (e: Exception) {
            Log.e("AccountDetails", "Error deriving voter address: ${e.message}")
            "0x0000000000000000000000000000000000000000"
        }
    }

    /**
     * Load minimal account data dengan key recovery
     */
    suspend fun loadMinimalAccountDataWithKeyRecovery(userEmail: String) {
        try {
            Log.d("AccountDetails", "📦 Loading minimal account data with key recovery...")

            val localVoterData = voterRepository.getVoterDataLocally()
            val walletInfo = voterRepository.getCompleteWalletInfo()

            // Try to get keys from both storages
            val cryptoKeyManager = CryptoKeyManager(context)
            var privateKey = cryptoKeyManager.getPrivateKey()
            var publicKey = cryptoKeyManager.getPublicKey()
            var voterAddress = cryptoKeyManager.getVoterAddress()

            // Fallback to backup storage
            if (privateKey.isNullOrEmpty()) {
                privateKey = userLoginRepository.getPrivateKey(userEmail)
                publicKey = userLoginRepository.getPublicKey(userEmail)

                if (publicKey != null) {
                    voterAddress = deriveVoterAddressFromPublicKey(publicKey)
                }
            }

            accountData = AccountDisplayData(
                fullName = localVoterData?.full_name ?: "N/A",
                email = userEmail,
                nik = localVoterData?.nik ?: "N/A",
                publicKey = publicKey ?: "",
                privateKey = privateKey ?: "",
                voterAddress = voterAddress ?: localVoterData?.voter_address ?: "",
                ethBalance = walletInfo.balance,
                hasVoted = localVoterData?.has_voted ?: false
            )

            Log.d("AccountDetails", "📦 Minimal account data loaded with key recovery")
        } catch (e: Exception) {
            Log.e("AccountDetails", "❌ Error loading minimal data: ${e.message}", e)
        }
    }

    // Function to load account data
    suspend fun loadAccountDataEnhanced() {
        try {
            isLoading = true
            errorMessage = null

            Log.d("AccountDetails", "🔄 Loading enhanced account data...")

            // Step 1: Get user email
            val userEmail = userLoginRepository.getUserEmail()
            if (userEmail.isNullOrEmpty()) {
                errorMessage = "No user session found"
                return
            }

            // Step 2: Verify dan load crypto keys
            Log.d("AccountDetails", "🔐 Verifying crypto keys...")
            val keyStatus = userLoginRepository.verifyKeysIntegrityAfterLogin(userEmail)

            if (!keyStatus) {
                Log.w("AccountDetails", "⚠️ Keys need repair, attempting auto-repair...")

                try {
                    // Try to repair keys
                    val app = context.applicationContext as VoteChainApplication
                    val repairSuccess = app.forceReloadAllKeys()

                    if (repairSuccess) {
                        Log.d("AccountDetails", "✅ Keys repaired successfully")
                    } else {
                        Log.w("AccountDetails", "⚠️ Key repair failed")
                    }
                } catch (e: Exception) {
                    Log.e("AccountDetails", "❌ Error during key repair: ${e.message}")
                }
            }

            // Step 3: Load profile data
            userProfileRepository.fetchCompleteUserProfile().fold(
                onSuccess = { profile ->
                    Log.d("AccountDetails", "✅ Profile data loaded")

                    // Step 4: Get wallet info dengan enhanced verification
                    val walletInfo = voterRepository.getCompleteWalletInfo()

                    // Step 5: Get crypto keys dengan verification
                    val cryptoKeyManager = CryptoKeyManager(context)
                    var privateKey = cryptoKeyManager.getPrivateKey()
                    var publicKey = cryptoKeyManager.getPublicKey()
                    var voterAddress = cryptoKeyManager.getVoterAddress()

                    // Step 6: Fallback ke backup storage jika keys tidak ada
                    if (privateKey.isNullOrEmpty() && userEmail != null) {
                        Log.w("AccountDetails", "🔧 Primary keys empty, checking backup storage...")

                        val backupPrivateKey = userLoginRepository.getPrivateKey(userEmail)
                        val backupPublicKey = userLoginRepository.getPublicKey(userEmail)

                        if (backupPrivateKey != null && backupPublicKey != null) {
                            Log.d("AccountDetails", "✅ Found keys in backup storage")
                            privateKey = backupPrivateKey
                            publicKey = backupPublicKey

                            // Try to restore to primary storage
                            try {
                                val restoredVoterAddress = deriveVoterAddressFromPublicKey(backupPublicKey)
                                val keyPairInfo = CryptoKeyManager.KeyPairInfo(
                                    publicKey = backupPublicKey,
                                    privateKey = backupPrivateKey,
                                    voterAddress = restoredVoterAddress,
                                    generationMethod = "Profile_Backup_Restoration"
                                )
                                cryptoKeyManager.storeKeyPair(keyPairInfo)
                                voterAddress = restoredVoterAddress
                                Log.d("AccountDetails", "✅ Keys restored to primary storage")
                            } catch (e: Exception) {
                                Log.e("AccountDetails", "❌ Failed to restore keys: ${e.message}")
                            }
                        } else {
                            Log.e("AccountDetails", "❌ No keys found in backup storage either")
                        }
                    }

                    // Step 7: Update account data
                    accountData = AccountDisplayData(
//                        fullName = profile.userProfile?.full_name ?: "N/A",
                        email = userEmail,
//                        nik = localVoterData?.nik ?: "N/A",
                        publicKey = publicKey ?: "",
                        privateKey = privateKey ?: "",
                        voterAddress = voterAddress ?: "",
                        ethBalance = walletInfo.balance,
                        hasVoted = profile.voterProfile?.has_voted ?: false
                    )

                    // Step 8: Log status
                    Log.d("AccountDetails", "✅ Account data updated:")
                    Log.d("AccountDetails", "- Private Key: ${if (privateKey != null) "✅ Available" else "❌ Missing"}")
                    Log.d("AccountDetails", "- Public Key: ${if (publicKey != null) "✅ Available" else "❌ Missing"}")
                    Log.d("AccountDetails", "- Voter Address: ${if (voterAddress != null) "✅ Available" else "❌ Missing"}")

                },
                onFailure = { error ->
                    Log.e("AccountDetails", "❌ Failed to load profile: ${error.message}")
                    errorMessage = "Failed to load profile data: ${error.message}"

                    // Try to load minimal data from local storage
                    loadMinimalAccountDataWithKeyRecovery(userEmail)
                }
            )
        } catch (e: Exception) {
            Log.e("AccountDetails", "❌ Exception loading account data: ${e.message}", e)
            errorMessage = "Unexpected error: ${e.message}"
        } finally {
            isLoading = false
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


    // Load data on first composition
    LaunchedEffect(Unit) {
        loadAccountDataEnhanced()
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