package com.nocturna.votechain.ui.screens.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.DangerColors
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import android.net.Uri
import android.net.wifi.WifiConfiguration.AuthAlgorithm.strings
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import com.nocturna.votechain.R
import com.nocturna.votechain.viewmodel.register.RegisterViewModel
import com.nocturna.votechain.data.repository.UserRepository
import kotlinx.coroutines.delay
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.drawWithContent
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.utils.Numeric
import java.security.SecureRandom
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutCubic
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.ui.theme.SuccessColors
import com.nocturna.votechain.utils.LanguageManager
import androidx.compose.material3.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.nocturna.votechain.ui.theme.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocturna.votechain.viewmodel.register.KeyGenerationState

// Data class to manage validation state for each field
data class ValidationState(
    val hasError: Boolean = false,
    val errorMessage: String = ""
)

@Composable
fun RegisterScreen(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onWaitingScreen: () -> Unit,
    navigateToAccepted: () -> Unit,
    navigateToRejected: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory(LocalContext.current))
) {
    val context = LocalContext.current
    val strings = LanguageManager.getLocalizedStrings()
    val keyGenerationState by viewModel.keyGenerationState.collectAsState()
    val nodeConnected by viewModel.nodeConnected.collectAsState()

    // Email validation regex
    val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()

    // Observe UI state
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is RegisterViewModel.RegisterUiState.Loading

    // Form field values
    var nationalId by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthPlace by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedProvince by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // API data state
    val provinces by viewModel.provinces.collectAsState()
    val regencies by viewModel.regencies.collectAsState()
    val isProvincesLoading by viewModel.isProvincesLoading.collectAsState()
    val isRegenciesLoading by viewModel.isRegenciesLoading.collectAsState()
    var selectedProvinceCode by remember { mutableStateOf("") }

    // File selection states
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isFileSelected by remember { mutableStateOf(false) }
    var isFileUploaded by remember { mutableStateOf(false) }

    // Validation states
    var nationalIdValidation by remember { mutableStateOf(ValidationState()) }
    var fullNameValidation by remember { mutableStateOf(ValidationState()) }
    var emailValidation by remember { mutableStateOf(ValidationState()) }
    var birthPlaceValidation by remember { mutableStateOf(ValidationState()) }
    var birthDateValidation by remember { mutableStateOf(ValidationState()) }
    var addressValidation by remember { mutableStateOf(ValidationState()) }
    var provinceValidation by remember { mutableStateOf(ValidationState()) }
    var regionValidation by remember { mutableStateOf(ValidationState()) }
    var genderValidation by remember { mutableStateOf(ValidationState()) }
    var passwordValidation by remember { mutableStateOf(ValidationState()) }
    var fileValidation by remember { mutableStateOf(ValidationState()) }

    // Animation states
    var showElements by remember { mutableStateOf(false) }
    val headerAlpha = animateFloatAsState(
        targetValue = if (showElements) 1f else 0f,
        animationSpec = tween(700)
    )
    val formAlpha = animateFloatAsState(
        targetValue = if (showElements) 1f else 0f,
        animationSpec = tween(1000)
    )
    val buttonScale = animateFloatAsState(
        targetValue = if (showElements) 1f else 0.8f,
        animationSpec = tween(800)
    )

    // Start animations when screen appears
    LaunchedEffect(Unit) {
        delay(100)
        showElements = true
    }

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterViewModel.RegisterUiState.Loading -> {
                // Show loading screen
                onWaitingScreen()
            }
            is RegisterViewModel.RegisterUiState.Success -> {
                val response = (uiState as RegisterViewModel.RegisterUiState.Success).data
                Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()

                val status = response.data?.verification_status?.lowercase()
                // Based on the response, navigate to the appropriate screen
                when (status) {
                    "accepted" -> navigateToAccepted()
                    "rejected" -> navigateToRejected()
                    "pending" -> onWaitingScreen()
                    else -> navigateToRejected()
                }


                // Reset the state in the view model
                viewModel.resetState()
            }
            is RegisterViewModel.RegisterUiState.Error -> {
                val errorMessage = (uiState as RegisterViewModel.RegisterUiState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                // Navigate to rejected screen for any error
                navigateToRejected()

                // Reset the state in the view model
                viewModel.resetState()
            }
            else -> { /* Initial state, do nothing */ }
        }
    }

    // Function to validate a specific field
    fun validateField(field: String, value: String): ValidationState {
        return when (field) {
            "nationalId" -> {
                if (value.isEmpty()) {
                    ValidationState(true, "National ID is required")
                } else if (!value.all { it.isDigit() }) {
                    ValidationState(true, "National ID should contain only digits")
                } else if (value.length != 16) {
                    ValidationState(true, "National ID should be 16 digits")
                } else {
                    ValidationState(false, "")
                }
            }
            "fullName" -> {
                if (value.isEmpty()) {
                    ValidationState(true, "Full name is required")
                } else if (value.length < 3) {
                    ValidationState(true, "Name is too short")
                } else {
                    ValidationState(false, "")
                }
            }
            "email" -> {
                if (value.isEmpty()) {
                    ValidationState(true, "Email is required")
                } else if (!value.matches(emailRegex)) {
                    ValidationState(true, "Invalid email format")
                } else {
                    ValidationState(false, "")
                }
            }
            "birthPlace" -> {
                if (value.isEmpty()) {
                    ValidationState(true, "Birth place is required")
                } else {
                    ValidationState(false, "")
                }
            }
            "birthDate" -> {
                if (value.isEmpty()) {
                    ValidationState(true, "Birth date is required")
                } else {
                    ValidationState(false, "")
                }
            }
            "address" -> {
                if (value.isEmpty()) {
                    ValidationState(true, "Address is required")
                } else {
                    ValidationState(false, "")
                }
            }
            "province" -> {
                if (value.isEmpty()) {
                    ValidationState(true, "Province is required")
                } else {
                    ValidationState(false, "")
                }
            }
            "region" -> {
                if (value.isEmpty()) {
                    ValidationState(true, "Region is required")
                } else {
                    ValidationState(false, "")
                }
            }
            "gender" -> {
                if (value.isEmpty()) {
                    ValidationState(true, "Gender selection is required")
                } else {
                    ValidationState(false, "")
                }
            }
            "password" -> {
                if (value.isEmpty()) {
                    ValidationState(true, "Password is required")
                } else if (value.length < 8) {
                    ValidationState(true, "Password must be at least 8 characters")
                } else {
                    ValidationState(false, "")
                }
            }
            else -> ValidationState(false, "")
        }
    }

    // Function to validate all fields
    fun validateAllFields(): Boolean {
        nationalIdValidation = validateField("nationalId", nationalId)
        fullNameValidation = validateField("fullName", fullName)
        emailValidation = validateField("email", email)
        birthPlaceValidation = validateField("birthPlace", birthPlace)
        birthDateValidation = validateField("birthDate", birthDate)
        addressValidation = validateField("address", address)
        provinceValidation = validateField("province", selectedProvince)
        regionValidation = validateField("region", selectedRegion)
        genderValidation = validateField("gender", selectedGender)
        passwordValidation = validateField("password", password)

        fileValidation = if (!isFileSelected) {
            ValidationState(true, "ID card upload is required")
        } else {
            ValidationState(false, "")
        }

        return !nationalIdValidation.hasError &&
                !fullNameValidation.hasError &&
                !emailValidation.hasError &&
                !birthPlaceValidation.hasError &&
                !birthDateValidation.hasError &&
                !addressValidation.hasError &&
                !provinceValidation.hasError &&
                !regionValidation.hasError &&
                !genderValidation.hasError &&
                !passwordValidation.hasError &&
                !fileValidation.hasError
    }

    // Check if the form is valid (all fields filled correctly)
    val isFormValid = remember(
        nationalId, fullName, email, birthPlace, birthDate, address,
        selectedProvince, selectedRegion, selectedGender, password,
        isFileSelected
    ) {
        nationalId.isNotEmpty() && fullName.isNotEmpty() &&
                email.isNotEmpty() && email.matches(emailRegex) &&
                birthPlace.isNotEmpty() && birthDate.isNotEmpty() &&
                address.isNotEmpty() && selectedProvince.isNotEmpty() &&
                selectedRegion.isNotEmpty() && selectedGender.isNotEmpty() &&
                password.isNotEmpty() && password.length >= 8 &&
                isFileSelected
    }

    val scrollState = rememberScrollState()

    // File picker launcher - Modified to only accept image file types (JPG, JPEG, PNG)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Verify the MIME type to ensure it's an image format we accept
            val mimeType = context.contentResolver.getType(it)
            val isValidImageType = mimeType == "image/jpeg" || mimeType == "image/jpg" || mimeType == "image/png"

            if (isValidImageType) {
                selectedFileUri = it
                // Get file name from URI
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    selectedFileName = cursor.getString(nameIndex)
                }
                isFileSelected = true
                isFileUploaded = false // Reset upload state when new file is selected
                fileValidation = ValidationState(false, "")
            } else {
                Toast.makeText(
                    context,
                    "Please select a JPG, JPEG, or PNG image file",
                    Toast.LENGTH_SHORT
                ).show()
                fileValidation = ValidationState(true, "Only JPG, JPEG, and PNG formats are allowed")
            }
        }
    }

    // Main content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp, bottom = 40.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header section with animation
        Box(
            modifier = Modifier
                .alpha(headerAlpha.value)
                .padding(bottom = 24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = strings.registerAccount,
                    style = AppTypography.heading1Bold,
                    color = MainColors.Primary1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = strings.registerDescription,
                    style = AppTypography.heading4Medium,
                    color = NeutralColors.Neutral70,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        CryptoKeyStatusCard(
            keyGenerationState = keyGenerationState,
            nodeConnected = nodeConnected,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Form section with animation
        Box(modifier = Modifier.alpha(formAlpha.value)) {
            Column {
                // National ID Field
                OutlinedTextField(
                    value = nationalId,
                    onValueChange = {
                        nationalId = it
                        nationalIdValidation = validateField("nationalId", it)
                    },
                    label = { Text(strings.nationalId) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (nationalIdValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                        unfocusedBorderColor = if (nationalIdValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                        focusedTextColor = NeutralColors.Neutral70,
                        unfocusedTextColor = NeutralColors.Neutral70,
                        focusedLabelColor = if (fullNameValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                        unfocusedLabelColor = if (fullNameValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    supportingText = {
                        if (nationalIdValidation.hasError) {
                            Text(
                                text = nationalIdValidation.errorMessage,
                                color = DangerColors.Danger50,
                                style = AppTypography.paragraphRegular
                            )
                        }
                    },
                    isError = nationalIdValidation.hasError
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Full Name Field
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        fullNameValidation = validateField("fullName", it)
                    },
                    label = { Text(strings.fullName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (fullNameValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                        unfocusedBorderColor = if (fullNameValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                        focusedTextColor = NeutralColors.Neutral70,
                        unfocusedTextColor = NeutralColors.Neutral70,
                        focusedLabelColor = if (fullNameValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                        unfocusedLabelColor = if (fullNameValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    supportingText = {
                        if (fullNameValidation.hasError) {
                            Text(
                                text = fullNameValidation.errorMessage,
                                color = DangerColors.Danger50,
                                style = AppTypography.paragraphRegular
                            )
                        }
                    },
                    isError = fullNameValidation.hasError
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailValidation = validateField("email", it)
                    },
                    label = { Text(strings.email) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (emailValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                        unfocusedBorderColor = if (emailValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                        focusedTextColor = NeutralColors.Neutral70,
                        unfocusedTextColor = NeutralColors.Neutral70,
                        focusedLabelColor = if (emailValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                        unfocusedLabelColor = if (emailValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    supportingText = {
                        if (emailValidation.hasError) {
                            Text(
                                text = emailValidation.errorMessage,
                                color = DangerColors.Danger50,
                                style = AppTypography.paragraphRegular
                            )
                        }
                    },
                    isError = emailValidation.hasError
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Birth Place and Date Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Birth Place Field
                    OutlinedTextField(
                        value = birthPlace,
                        onValueChange = {
                            birthPlace = it
                            birthPlaceValidation = validateField("birthPlace", it)
                        },
                        label = { Text(strings.birthPlace) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (birthPlaceValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                            unfocusedBorderColor = if (birthPlaceValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                            focusedTextColor = NeutralColors.Neutral70,
                            unfocusedTextColor = NeutralColors.Neutral70,
                            focusedLabelColor = if (birthPlaceValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                            unfocusedLabelColor = if (birthPlaceValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        supportingText = {
                            if (birthPlaceValidation.hasError) {
                                Text(
                                    text = birthPlaceValidation.errorMessage,
                                    color = DangerColors.Danger50,
                                    style = AppTypography.paragraphRegular
                                )
                            }
                        },
                        isError = birthPlaceValidation.hasError
                    )

                    // Birth Date Field (with date picker icon)
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    val datePickerDialog = DatePickerDialog(
                        context,
                        { _, selectedYear, selectedMonth, selectedDay ->
                            // Format the date in dd/MM/yyyy format
                            val selectedDate = Calendar.getInstance()
                            selectedDate.set(selectedYear, selectedMonth, selectedDay)
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            birthDate = dateFormat.format(selectedDate.time)
                            birthDateValidation = validateField("birthDate", birthDate)
                        },
                        year,
                        month,
                        day
                    )

                    // Limit date selection to past dates only (for birth date)
                    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text(strings.birthDate) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (birthDateValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                            unfocusedBorderColor = if (birthDateValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                            focusedTextColor = NeutralColors.Neutral70,
                            unfocusedTextColor = NeutralColors.Neutral70,
                            focusedLabelColor = if (birthDateValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                            unfocusedLabelColor = if (birthDateValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.calendar),
                                    contentDescription = "Select Date",
                                    tint = NeutralColors.Neutral30
                                )
                            }
                        },
                        supportingText = {
                            if (birthDateValidation.hasError) {
                                Text(
                                    text = birthDateValidation.errorMessage,
                                    color = DangerColors.Danger50,
                                    style = AppTypography.paragraphRegular
                                )
                            }
                        },
                        isError = birthDateValidation.hasError
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Address Field
                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        addressValidation = validateField("address", it)
                    },
                    label = { Text(strings.address) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (addressValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                        unfocusedBorderColor = if (addressValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                        focusedTextColor = NeutralColors.Neutral70,
                        unfocusedTextColor = NeutralColors.Neutral70,
                        focusedLabelColor = if (fullNameValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                        unfocusedLabelColor = if (fullNameValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    supportingText = {
                        if (addressValidation.hasError) {
                            Text(
                                text = addressValidation.errorMessage,
                                color = DangerColors.Danger50,
                                style = AppTypography.paragraphRegular
                            )
                        }
                    },
                    isError = addressValidation.hasError
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Province Dropdown
                var expandedProvince by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedProvince.ifEmpty { strings.province },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedProvince = !expandedProvince }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.down2),
                                    contentDescription = "Dropdown",
                                    tint = NeutralColors.Neutral30
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedProvince = !expandedProvince },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (provinceValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                            unfocusedBorderColor = if (provinceValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                            focusedTextColor = if (selectedProvince.isEmpty()) NeutralColors.Neutral30 else NeutralColors.Neutral70,
                            unfocusedTextColor = if (selectedProvince.isEmpty()) NeutralColors.Neutral30 else NeutralColors.Neutral70,
                        ),
                        supportingText = {
                            if (provinceValidation.hasError) {
                                Text(
                                    text = provinceValidation.errorMessage,
                                    color = DangerColors.Danger50,
                                    style = AppTypography.paragraphRegular
                                )
                            }
                        },
                        isError = provinceValidation.hasError
                    )

                    DropdownMenu(
                        expanded = expandedProvince,
                        onDismissRequest = { expandedProvince = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 250.dp)
                            .align(Alignment.TopStart)
                    ) {
                        if (isProvincesLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MainColors.Primary1,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else if (provinces.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "No provinces found",
                                        color = NeutralColors.Neutral40
                                    )
                                },
                                onClick = { /* No action */ }
                            )
                        } else {
                            provinces.forEach { province ->
                                DropdownMenuItem(
                                    text = { Text(province.name, color = NeutralColors.Neutral70) },
                                    onClick = {
                                        selectedProvince = province.name
                                        selectedProvinceCode = province.code
                                        expandedProvince = false
                                        provinceValidation = validateField("province", province.name)

                                        // Reset region when province changes
                                        selectedRegion = ""
                                        regionValidation = validateField("region", "")

                                        // Fetch regencies for selected province
                                        viewModel.fetchRegencies(province.code)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Region Dropdown
                var expandedRegion by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRegion.ifEmpty { strings.region },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                if (selectedProvince.isNotEmpty()) {
                                    expandedRegion = !expandedRegion
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.down2),
                                    contentDescription = "Dropdown",
                                    tint = NeutralColors.Neutral30
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedProvince.isNotEmpty()) {
                                    expandedRegion = !expandedRegion
                                }
                            },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (regionValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                            unfocusedBorderColor = if (regionValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                            focusedTextColor = if (selectedRegion.isEmpty()) NeutralColors.Neutral30 else NeutralColors.Neutral70,
                            unfocusedTextColor = if (selectedRegion.isEmpty()) NeutralColors.Neutral30 else NeutralColors.Neutral70,
                        ),
                        supportingText = {
                            if (regionValidation.hasError) {
                                Text(
                                    text = regionValidation.errorMessage,
                                    color = DangerColors.Danger50,
                                    style = AppTypography.paragraphRegular
                                )
                            } else if (selectedProvince.isEmpty()) {
                                Text(
                                    text = "Please select a province first",
                                    color = NeutralColors.Neutral50,
                                    style = AppTypography.paragraphRegular
                                )
                            }
                        },
                        isError = regionValidation.hasError
                    )

                    DropdownMenu(
                        expanded = expandedRegion && selectedProvince.isNotEmpty(),
                        onDismissRequest = { expandedRegion = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 250.dp)
                            .align(Alignment.TopStart)
                    ) {
                        if (isRegenciesLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MainColors.Primary1,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else if (regencies.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "No regions found for this province",
                                        color = NeutralColors.Neutral40
                                    )
                                },
                                onClick = { /* No action */ }
                            )
                        } else {
                            regencies.forEach { regency ->
                                DropdownMenuItem(
                                    text = { Text(regency.name, color = NeutralColors.Neutral70) },
                                    onClick = {
                                        selectedRegion = regency.name
                                        expandedRegion = false
                                        regionValidation = validateField("region", regency.name)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Gender Dropdown
                var expandedGender by remember { mutableStateOf(false) }
                val genderOptions = listOf(strings.male, strings.female)

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedGender.ifEmpty { strings.gender },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedGender = !expandedGender },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (genderValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                            unfocusedBorderColor = if (genderValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                            focusedTextColor = if (selectedGender.isEmpty()) NeutralColors.Neutral30 else NeutralColors.Neutral70,
                            unfocusedTextColor = if (selectedGender.isEmpty()) NeutralColors.Neutral30 else NeutralColors.Neutral70,
                        ),
                        trailingIcon = {
                            IconButton(onClick = { expandedGender = !expandedGender }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.down2),
                                    contentDescription = "Toggle Gender Dropdown",
                                    tint = NeutralColors.Neutral30
                                )
                            }
                        },
                        supportingText = {
                            if (genderValidation.hasError) {
                                Text(
                                    text = genderValidation.errorMessage,
                                    color = DangerColors.Danger50,
                                    style = AppTypography.paragraphRegular
                                )
                            }
                        },
                        isError = genderValidation.hasError
                    )

                    DropdownMenu(
                        expanded = expandedGender,
                        onDismissRequest = { expandedGender = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.TopStart)
                    ) {
                        genderOptions.forEach { gender ->
                            DropdownMenuItem(
                                text = { Text(gender, color = NeutralColors.Neutral70) },
                                onClick = {
                                    selectedGender = gender
                                    expandedGender = false
                                    genderValidation = validateField("gender", gender)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordValidation = validateField("password", it)
                    },
                    label = { Text(strings.setPassword) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (passwordValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                        unfocusedBorderColor = if (passwordValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                        focusedTextColor = NeutralColors.Neutral70,
                        unfocusedTextColor = NeutralColors.Neutral70,
                        focusedLabelColor = if (fullNameValidation.hasError) DangerColors.Danger50 else MainColors.Primary1,
                        unfocusedLabelColor = if (fullNameValidation.hasError) DangerColors.Danger50 else NeutralColors.Neutral30,
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    supportingText = {
                        if (passwordValidation.hasError) {
                            Text(
                                text = passwordValidation.errorMessage,
                                color = DangerColors.Danger50,
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
                    },
                    isError = passwordValidation.hasError
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Custom modifier for dashed border
                fun Modifier.dashedBorder(width: Dp, radius: Dp, color: Color) = this.drawWithContent {
                    drawContent()
                    val strokeWidth = width.toPx()
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawRoundRect(
                        color = color,
                        style = Stroke(width = strokeWidth, pathEffect = pathEffect),
                        cornerRadius = CornerRadius(radius.toPx(), radius.toPx())
                    )
                }

                // ID card upload section
                if (!isFileSelected && !isFileUploaded) {
                    // Initial state - no file selected
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .dashedBorder(
                                width = 1.dp,
                                radius = 8.dp,
                                color = if (fileValidation.hasError) DangerColors.Danger50 else MainColors.Primary1
                            )
                            .background(Color(NeutralColors.Neutral10.value))
                            .padding(top = 20.dp, bottom = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = strings.uploadKtp,
                                style = AppTypography.heading5Medium,
                                color = NeutralColors.Neutral70,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.uploadKtpDescription,
                                style = AppTypography.paragraphRegular,
                                color = NeutralColors.Neutral40,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = {
                                    filePickerLauncher.launch("image/*")
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MainColors.Primary1
                                )
                            ) {
                                Text(
                                    strings.browseFiles,
                                    style = AppTypography.paragraphBold
                                )
                            }

                            if (fileValidation.hasError) {
                                Text(
                                    text = fileValidation.errorMessage,
                                    color = DangerColors.Danger50,
                                    style = AppTypography.paragraphRegular
                                )
                            }
                        }
                    }
                } else if (isFileSelected && !isFileUploaded) {
                    // File selected but not uploaded yet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .dashedBorder(
                                width = 1.dp,
                                radius = 8.dp,
                                color = MainColors.Primary1
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.paper),
                                    contentDescription = "File",
                                    tint = MainColors.Primary1,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedFileName ?: "Selected file",
                                    style = AppTypography.heading5Medium,
                                    color = NeutralColors.Neutral70,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "delete",
                                    color = MainColors.Primary2,
                                    style = AppTypography.paragraphBold,
                                    modifier = Modifier.clickable {
                                        selectedFileUri = null
                                        selectedFileName = null
                                        isFileSelected = false
                                        isFileUploaded = false
                                    }
                                )
                            }
                        }
                    }
                } else if (isFileUploaded) {
                    // File uploaded successfully
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .dashedBorder(
                                width = 1.dp,
                                radius = 8.dp,
                                color = Color(0xFFCEE0E3)
                            )
                            .background(Color(0xFFEAF2F4))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.paper),
                                contentDescription = "File",
                                tint = MainColors.Primary1,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedFileName ?: "Uploaded file",
                                    style = AppTypography.heading5Medium,
                                    color = NeutralColors.Neutral70
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "File uploaded successfully",
                                    style = AppTypography.paragraphRegular,
                                    color = NeutralColors.Neutral50
                                )
                            }
                            Text(
                                text = "delete",
                                color = MainColors.Primary2,
                                style = AppTypography.paragraphBold,
                                modifier = Modifier.clickable {
                                    selectedFileUri = null
                                    selectedFileName = null
                                    isFileSelected = false
                                    isFileUploaded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ Submit Button - TETAP menggunakan method signature yang sama
                Button(
                    onClick = {
                        if (validateAllFields()) {
                            // ✅ Tetap memanggil method yang sama seperti existing code
                            selectedFileUri?.let { fileUri ->
                                viewModel.registerUserWithVoterAddress(
                                    nationalId = nationalId,
                                    fullName = fullName,
                                    email = email,
                                    password = password,
                                    birthPlace = birthPlace,
                                    birthDate = birthDate,
                                    address = address,
                                    region = selectedRegion,
                                    gender = selectedGender,
                                    ktpFileUri = fileUri,
                                    role = "voter"
                                )
                            } ?: run {
                                Toast.makeText(context, "Please upload your ID card", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainColors.Primary1),
                    enabled = isFormValid && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = strings.register,
                            color = Color.White,
                            style = AppTypography.paragraphRegular.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Login link with animation
                AnimatedVisibility(
                    visible = showElements,
                    enter = fadeIn(animationSpec = tween(1000)) +
                            slideInVertically(
                                animationSpec = tween(800),
                                initialOffsetY = { it / 2 }
                            )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = strings.alreadyHaveAccount,
                            color = NeutralColors.Neutral70,
                            style = AppTypography.heading5Medium
                        )
                        Text(
                            text = strings.login,
                            color = MainColors.Primary1,
                            style = AppTypography.heading5Medium,
                            modifier = Modifier.clickable(onClick = onLoginClick)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ✅ New Composable untuk Crypto Key Status
@Composable
fun CryptoKeyStatusCard(
    keyGenerationState: KeyGenerationState,
    nodeConnected: Boolean,
    modifier: Modifier = Modifier
) {
    when (keyGenerationState) {
        KeyGenerationState.NotStarted -> {
            // Show info about crypto security
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8FAFC)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Modern crypto icon with gradient background
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MainColors.Primary1,
                                            MainColors.Primary1.copy(alpha = 0.7f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.lock),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Blockchain Security",
                                style = AppTypography.heading6Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Cryptographic key protection",
                                style = AppTypography.paragraphRegular,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Connection status indicator
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = if (nodeConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                    shape = CircleShape
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Bar Section
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Connection Status",
                                style = AppTypography.paragraphRegular,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = if (nodeConnected) "Ready" else "Unavailable",
                                style = AppTypography.paragraphRegular,
                                color = if (nodeConnected) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Modern Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(
                                    color = Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(if (nodeConnected) 1f else 0.3f)
                                    .background(
                                        brush = if (nodeConnected) {
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF10B981),
                                                    Color(0xFF059669)
                                                )
                                            )
                                        } else {
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFFEF4444),
                                                    Color(0xFFDC2626)
                                                )
                                            )
                                        },
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .animateContentSize(
                                        animationSpec = tween(
                                            durationMillis = 600,
                                            easing = EaseInOutCubic
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Message
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (nodeConnected) R.drawable.tickcircle else R.drawable.dangercircle
                                ),
                                contentDescription = null,
                                tint = if (nodeConnected) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )

                            Text(
                                text = if (nodeConnected)
                                    "Blockchain network is connected and ready for secure registration. Your cryptographic keys will be protected."
                                else
                                    "Blockchain network connection is currently unavailable. Please check your internet connection and try again.",
                                style = AppTypography.paragraphRegular,
                                color = Color(0xFF64748B),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        KeyGenerationState.Generating -> {
            // Enhanced generating state card
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0F9FF)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Animated progress indicator
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MainColors.Primary1,
                                strokeWidth = 3.dp,
                                strokeCap = StrokeCap.Round
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.lock), // Replace with key icon
                                contentDescription = null,
                                tint = MainColors.Primary1,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Generating Cryptographic Keys",
                                style = AppTypography.heading6Bold,
                                color = Color(0xFF1E293B)
                            )

                            Text(
                                text = "Creating secure key pair for blockchain registration...",
                                style = AppTypography.paragraphRegular,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated progress bar for generation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                color = Color(0xFFE0F2FE),
                                shape = RoundedCornerShape(3.dp)
                            )
                    ) {
                        var progress by remember { mutableStateOf(0f) }

                        LaunchedEffect(Unit) {
                            while (progress < 1f) {
                                progress += 0.01f
                                delay(50)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            MainColors.Primary1,
                                            MainColors.Primary1.copy(alpha = 0.8f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .animateContentSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Please wait while we generate your secure cryptographic keys. This process ensures maximum security for your blockchain transactions.",
                        style = AppTypography.paragraphRegular,
                        color = Color(0xFF64748B),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        KeyGenerationState.Generated -> {
            // Success state card
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0FDF4)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFF10B981),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.tickcircle), // Replace with check icon
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Keys Generated Successfully",
                            style = AppTypography.heading6Bold,
                            color = Color(0xFF059669)
                        )

                        Text(
                            text = "Your cryptographic keys are ready for secure blockchain registration",
                            style = AppTypography.paragraphRegular,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }
        }

        else -> {
            // Fallback for any other states
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEF2F2)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFFEF4444),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.dangercircle), // Replace with error icon
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Key Generation Failed",
                            style = AppTypography.heading6Bold,
                            color = Color(0xFFDC2626)
                        )

                        Text(
                            text = "There was an error generating cryptographic keys. Please try again.",
                            style = AppTypography.paragraphRegular,
                            color = Color(0xFFB91C1C)
                        )
                    }
                }
            }
        }
    }
}