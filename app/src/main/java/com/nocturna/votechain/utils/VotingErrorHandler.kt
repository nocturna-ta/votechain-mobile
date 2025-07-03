package com.nocturna.votechain.utils

import android.content.Context
import android.util.Log

/**
 * Enhanced error handler for voting operations
 * Provides user-friendly error messages and recovery suggestions
 */
class VotingErrorHandler(private val context: Context) {

    companion object {
        private const val TAG = "VotingErrorHandler"
    }

    /**
     * Data class for enhanced error information
     */
    data class VotingError(
        val title: String,
        val message: String,
        val recoveryAction: String? = null,
        val isRetryable: Boolean = false,
        val errorCode: String? = null
    )

    /**
     * Convert exception to user-friendly error
     */
    fun handleVotingError(exception: Exception): VotingError {

        return when {
            // Network related errors
            exception is java.net.UnknownHostException -> {
                VotingError(
                    title = "networkError",
                    message = "Cannot connect to voting server. Please check your internet connection.",
                    recoveryAction = "Check your internet connection and try again",
                    isRetryable = true,
                    errorCode = "NETWORK_UNREACHABLE"
                )
            }

            exception is java.net.SocketTimeoutException -> {
                VotingError(
                    title = "networkError",
                    message = "Connection timeout. The server is taking too long to respond.",
                    recoveryAction = "Please try again in a few moments",
                    isRetryable = true,
                    errorCode = "NETWORK_TIMEOUT"
                )
            }

            exception is javax.net.ssl.SSLException -> {
                VotingError(
                    title = "Security Error",
                    message = "Secure connection failed. This might be a temporary issue.",
                    recoveryAction = "Please try again or contact support if the problem persists",
                    isRetryable = true,
                    errorCode = "SSL_ERROR"
                )
            }

            // Authentication errors
            exception.message?.contains("Authentication failed") == true -> {
                VotingError(
                    title = "Authentication Required",
                    message = "Your session has expired. Please log in again.",
                    recoveryAction = "Go to login screen and sign in again",
                    isRetryable = false,
                    errorCode = "AUTH_EXPIRED"
                )
            }

            // OTP related errors
            exception.message?.contains("OTP") == true -> {
                VotingError(
                    title = "OTP Verification Required",
                    message = "Please verify your OTP before voting.",
                    recoveryAction = "Go back and verify your OTP code",
                    isRetryable = false,
                    errorCode = "OTP_REQUIRED"
                )
            }

            // Cryptographic errors
            exception.message?.contains("Cryptographic") == true ||
                    exception.message?.contains("signing failed") == true -> {
                VotingError(
                    title = "Security Setup Required",
                    message = "Your voting security keys need to be set up properly.",
                    recoveryAction = "Please complete the security setup in your profile",
                    isRetryable = false,
                    errorCode = "CRYPTO_SETUP_REQUIRED"
                )
            }

            // Already voted
            exception.message?.contains("already voted") == true -> {
                VotingError(
                    title = "Vote Already Cast",
                    message = "You have already voted in this election.",
                    recoveryAction = "You can view results or check your voting history",
                    isRetryable = false,
                    errorCode = "ALREADY_VOTED"
                )
            }

            // Invalid transaction
            exception.message?.contains("Invalid signed transaction") == true -> {
                VotingError(
                    title = "Transaction Error",
                    message = "There was an error processing your vote. Please try again.",
                    recoveryAction = "Please try voting again",
                    isRetryable = true,
                    errorCode = "INVALID_TRANSACTION"
                )
            }

            // Rate limiting
            exception.message?.contains("Too many requests") == true -> {
                VotingError(
                    title = "Too Many Attempts",
                    message = "Please wait a moment before trying again.",
                    recoveryAction = "Wait 30 seconds and try again",
                    isRetryable = true,
                    errorCode = "RATE_LIMITED"
                )
            }

            // Server errors
            exception.message?.contains("Server error") == true ||
                    exception.message?.contains("500") == true -> {
                VotingError(
                    title = "Server Error",
                    message = "The voting server is experiencing issues. Please try again later.",
                    recoveryAction = "Please try again in a few minutes",
                    isRetryable = true,
                    errorCode = "SERVER_ERROR"
                )
            }

            // Service unavailable
            exception.message?.contains("Service temporarily unavailable") == true ||
                    exception.message?.contains("maintenance") == true -> {
                VotingError(
                    title = "Service Maintenance",
                    message = "The voting service is temporarily unavailable for maintenance.",
                    recoveryAction = "Please try again later",
                    isRetryable = true,
                    errorCode = "SERVICE_MAINTENANCE"
                )
            }

            // Default error
            else -> {
                VotingError(
                    title = "Voting Error",
                    message = exception.message ?: "An unexpected error occurred while voting.",
                    recoveryAction = "Please try again or contact support if the problem persists",
                    isRetryable = true,
                    errorCode = "UNKNOWN_ERROR"
                )
            }
        }
    }

    /**
     * Get recovery suggestions based on error type
     */
    fun getRecoverySuggestions(error: VotingError): List<String> {
        return when (error.errorCode) {
            "NETWORK_UNREACHABLE", "NETWORK_TIMEOUT" -> listOf(
                "Check your internet connection",
                "Try switching between WiFi and mobile data",
                "Restart your network connection",
                "Try again in a few moments"
            )

            "AUTH_EXPIRED" -> listOf(
                "Log out and log back in",
                "Clear app cache if problem persists",
                "Contact support if you continue having issues"
            )

            "OTP_REQUIRED" -> listOf(
                "Verify your OTP code",
                "Request a new OTP if the current one expired",
                "Check your phone number is correct"
            )

            "CRYPTO_SETUP_REQUIRED" -> listOf(
                "Complete security setup in your profile",
                "Generate new voting keys if needed",
                "Contact support for assistance"
            )

            "ALREADY_VOTED" -> listOf(
                "Check your voting history",
                "View election results",
                "Contact support if you believe this is an error"
            )

            "RATE_LIMITED" -> listOf(
                "Wait 30 seconds before trying again",
                "Avoid rapid repeated attempts",
                "Try again later if the problem persists"
            )

            "SERVER_ERROR", "SERVICE_MAINTENANCE" -> listOf(
                "Try again in a few minutes",
                "Check the app for service status updates",
                "Contact support if the issue persists"
            )

            else -> listOf(
                "Try the action again",
                "Restart the app if needed",
                "Check your internet connection",
                "Contact support if the problem continues"
            )
        }
    }

    /**
     * Check if error requires immediate user action
     */
    fun requiresImmediateAction(error: VotingError): Boolean {
        return when (error.errorCode) {
            "AUTH_EXPIRED", "OTP_REQUIRED", "CRYPTO_SETUP_REQUIRED" -> true
            else -> false
        }
    }

    /**
     * Get estimated recovery time
     */
    fun getEstimatedRecoveryTime(error: VotingError): String {
        return when (error.errorCode) {
            "NETWORK_TIMEOUT", "NETWORK_UNREACHABLE" -> "30 seconds - 2 minutes"
            "RATE_LIMITED" -> "30 seconds - 1 minute"
            "SERVER_ERROR" -> "2 - 5 minutes"
            "SERVICE_MAINTENANCE" -> "15 minutes - 1 hour"
            "SSL_ERROR" -> "1 - 2 minutes"
            else -> "Immediate"
        }
    }

    /**
     * Log error for debugging purposes
     */
    fun logError(error: VotingError, originalException: Exception) {
        Log.e(TAG, "=== VOTING ERROR ===")
        Log.e(TAG, "Error Code: ${error.errorCode}")
        Log.e(TAG, "Title: ${error.title}")
        Log.e(TAG, "Message: ${error.message}")
        Log.e(TAG, "Recovery Action: ${error.recoveryAction}")
        Log.e(TAG, "Is Retryable: ${error.isRetryable}")
        Log.e(TAG, "Original Exception: ${originalException.javaClass.simpleName}")
        Log.e(TAG, "Original Message: ${originalException.message}")
        Log.e(TAG, "=== END ERROR ===")
    }

    /**
     * Create error report for support
     */
    fun createErrorReport(error: VotingError, originalException: Exception): Map<String, Any> {
        return mapOf(
            "timestamp" to System.currentTimeMillis(),
            "error_code" to (error.errorCode ?: "UNKNOWN"),
            "error_title" to error.title,
            "error_message" to error.message,
            "is_retryable" to error.isRetryable,
            "original_exception_type" to originalException.javaClass.simpleName,
            "original_exception_message" to (originalException.message ?: "No message"),
            "stack_trace" to originalException.stackTraceToString(),
            "device_info" to getDeviceInfo(),
            "app_version" to getAppVersion()
        )
    }

    /**
     * Get device information for error reports
     */
    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "model" to android.os.Build.MODEL,
            "manufacturer" to android.os.Build.MANUFACTURER,
            "android_version" to android.os.Build.VERSION.RELEASE,
            "sdk_version" to android.os.Build.VERSION.SDK_INT.toString()
        )
    }

    /**
     * Get app version for error reports
     */
    private fun getAppVersion(): String {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}