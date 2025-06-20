//package com.nocturna.votechain.viewmodel.forgotpassword
//
//import android.content.Context
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import kotlin.random.Random
//
//class ForgotPasswordViewModel(private val context: Context) : ViewModel() {
//
//    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Initial)
//    val uiState: StateFlow<ForgotPasswordUiState> = _uiState
//
//    // Store generated OTPs for verification
//    private val emailOtpMap = mutableMapOf<String, String>()
//
//    // Generate a random 4-digit OTP
//    private fun generateOtp(): String {
//        return Random.nextInt(1000, 10000).toString()
//    }
//
//    fun sendOtpToEmail(email: String) {
//        viewModelScope.launch {
//            try {
//                _uiState.value = ForgotPasswordUiState.Loading
//
//                val otp = generateOtp()
//                // Store OTP for later verification
//                emailOtpMap[email] = otp
//
//                // Send email with OTP
//                val emailSent = sendEmail(email, otp)
//
//                if (emailSent) {
//                    _uiState.value = ForgotPasswordUiState.OtpSent(email)
//                } else {
//                    _uiState.value = ForgotPasswordUiState.Error("Failed to send verification code. Please try again.")
//                }
//            } catch (e: Exception) {
//                _uiState.value = ForgotPasswordUiState.Error(e.message ?: "An unknown error occurred")
//            }
//        }
//    }
//
//    fun verifyOtp(email: String, otp: String) {
//        viewModelScope.launch {
//            try {
//                _uiState.value = ForgotPasswordUiState.Loading
//
//                // Get the stored OTP for this email
//                val storedOtp = emailOtpMap[email]
//
//                if (storedOtp == otp) {
//                    _uiState.value = ForgotPasswordUiState.OtpVerified(email)
//                } else {
//                    _uiState.value = ForgotPasswordUiState.Error("Invalid verification code. Please try again.")
//                }
//            } catch (e: Exception) {
//                _uiState.value = ForgotPasswordUiState.Error(e.message ?: "An unknown error occurred")
//            }
//        }
//    }
//
//    private suspend fun sendEmail(recipientEmail: String, otp: String): Boolean {
//        return try {
//            // Email configurations - replace with your app's email
//            val senderEmail = "your-app-email@gmail.com"
//            val senderPassword = "your-app-email-password"
//
//            val props = System.getProperties()
//            props.put("mail.smtp.host", "smtp.gmail.com")
//            props.put("mail.smtp.socketFactory.port", "465")
//            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
//            props.put("mail.smtp.auth", "true")
//            props.put("mail.smtp.port", "465")
//
//            val session = Session.getInstance(props,
//                object : javax.mail.Authenticator() {
//                    override fun getPasswordAuthentication(): PasswordAuthentication {
//                        return PasswordAuthentication(senderEmail, senderPassword)
//                    }
//                })
//
//            val message = MimeMessage(session)
//            message.setFrom(InternetAddress(senderEmail))
//            message.addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
//            message.subject = "VoteChain Password Reset Verification Code"
//
//            // Email body with OTP
//            message.setText("""
//                Your verification code for password reset is: $otp
//
//                This code will expire in 10 minutes.
//
//                If you didn't request a password reset, please ignore this email.
//
//                Best regards,
//                VoteChain Team
//            """.trimIndent())
//
//            // Send message
//            Transport.send(message)
//            true
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//    }
//
//    // UI States
//    sealed class ForgotPasswordUiState {
//        object Initial : ForgotPasswordUiState()
//        object Loading : ForgotPasswordUiState()
//        data class OtpSent(val email: String) : ForgotPasswordUiState()
//        data class OtpVerified(val email: String) : ForgotPasswordUiState()
//        data class Error(val message: String) : ForgotPasswordUiState()
//    }
//
//    // Factory to provide context to ViewModel
//    class Factory(private val context: Context) : ViewModelProvider.Factory {
//        @Suppress("UNCHECKED_CAST")
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
//                return ForgotPasswordViewModel(context) as T
//            }
//            throw IllegalArgumentException("Unknown ViewModel class")
//        }
//    }
//}