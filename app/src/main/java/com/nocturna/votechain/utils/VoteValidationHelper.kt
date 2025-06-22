package com.nocturna.votechain.utils

import android.content.Context
import android.util.Log
import com.nocturna.votechain.security.CryptoKeyManager

/**
 * Helper class for validating vote prerequisites and debugging issues
 */
object VoteValidationHelper {
    private const val TAG = "VoteValidationHelper"

    /**
     * Comprehensive validation of all vote prerequisites
     */
    fun validateVotePrerequisites(
        context: Context,
        cryptoKeyManager: CryptoKeyManager,
        tokenManager: TokenManager
    ): ValidationResult {
        val issues = mutableListOf<String>()

        Log.d(TAG, "🔍 Starting comprehensive vote validation...")

        // 1. Check authentication token
        val token = tokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            issues.add("No authentication token available")
            Log.e(TAG, "❌ Authentication token missing")
        } else {
            Log.d(TAG, "✅ Authentication token available (${token.length} chars)")
        }

        // 2. Check voter ID
        val prefs = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
        val voterId = prefs.getString("user_id", "")
        if (voterId.isNullOrEmpty()) {
            issues.add("Voter ID not found in preferences")
            Log.e(TAG, "❌ Voter ID missing")
        } else {
            Log.d(TAG, "✅ Voter ID available: $voterId")
        }

        // 3. Check cryptographic keys
        if (!cryptoKeyManager.hasStoredKeyPair()) {
            issues.add("No cryptographic key pair stored")
            Log.e(TAG, "❌ No key pair stored")
        } else {
            Log.d(TAG, "✅ Key pair exists")

            // Check private key specifically
            val privateKey = cryptoKeyManager.getPrivateKey()
            if (privateKey.isNullOrEmpty()) {
                issues.add("Private key is null or empty")
                Log.e(TAG, "❌ Private key not accessible")
            } else {
                Log.d(TAG, "✅ Private key accessible (${privateKey.length} chars)")
            }

            // Check public key
            val publicKey = cryptoKeyManager.getPublicKey()
            if (publicKey.isNullOrEmpty()) {
                issues.add("Public key is null or empty")
                Log.e(TAG, "❌ Public key not accessible")
            } else {
                Log.d(TAG, "✅ Public key accessible (${publicKey.length} chars)")
            }

            // Check voter address
            val voterAddress = cryptoKeyManager.getVoterAddress()
            if (voterAddress.isNullOrEmpty()) {
                issues.add("Voter address is null or empty")
                Log.e(TAG, "❌ Voter address not accessible")
            } else {
                Log.d(TAG, "✅ Voter address accessible: $voterAddress")
            }
        }

        // 4. Test signing functionality
        if (cryptoKeyManager.hasStoredKeyPair()) {
            try {
                val testData = "test_signing_${System.currentTimeMillis()}"
                val signature = cryptoKeyManager.signData(testData)
                if (signature.isNullOrEmpty()) {
                    issues.add("Signing test failed - no signature generated")
                    Log.e(TAG, "❌ Signing test failed")
                } else {
                    Log.d(TAG, "✅ Signing test successful (signature: ${signature.take(16)}...)")
                }
            } catch (e: Exception) {
                issues.add("Signing test threw exception: ${e.message}")
                Log.e(TAG, "❌ Signing test exception: ${e.message}")
            }
        }

        // 5. Check region data
        val region = prefs.getString("user_region", "")
        if (region.isNullOrEmpty()) {
            issues.add("User region not found in preferences")
            Log.w(TAG, "⚠️ User region missing (will use default)")
        } else {
            Log.d(TAG, "✅ User region available: $region")
        }

        Log.d(TAG, "🔍 Validation complete. Issues found: ${issues.size}")

        return ValidationResult(
            isValid = issues.isEmpty(),
            issues = issues
        )
    }

    /**
     * Log detailed vote request information for debugging
     */
    fun logVoteRequestDetails(
        electionPairId: String,
        region: String,
        voterId: String,
        signedTransaction: String?
    ) {
        Log.d(TAG, "📋 Vote Request Details:")
        Log.d(TAG, "  Election Pair ID: $electionPairId")
        Log.d(TAG, "  Region: $region")
        Log.d(TAG, "  Voter ID: $voterId")
        Log.d(TAG, "  Signed Transaction: ${
            when {
                signedTransaction.isNullOrEmpty() -> "❌ NULL/EMPTY"
                signedTransaction.length < 10 -> "❌ TOO_SHORT (${signedTransaction.length} chars): $signedTransaction"
                else -> "✅ VALID (${signedTransaction.length} chars): ${signedTransaction.take(16)}..."
            }
        }")
    }

    /**
     * Result of validation check
     */
    data class ValidationResult(
        val isValid: Boolean,
        val issues: List<String>
    ) {
        fun getErrorMessage(): String {
            return if (issues.isEmpty()) {
                "All validations passed"
            } else {
                "Validation failed:\n${issues.joinToString("\n• ", "• ")}"
            }
        }
    }
}