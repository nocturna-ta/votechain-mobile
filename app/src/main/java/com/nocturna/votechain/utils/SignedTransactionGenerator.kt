package com.nocturna.votechain.utils

import android.util.Log
import com.nocturna.votechain.security.CryptoKeyManager
import java.security.MessageDigest

/**
 * Utility class for generating signed transactions for voting
 */
class SignedTransactionGenerator(private val cryptoKeyManager: CryptoKeyManager) {

    companion object {
        private const val TAG = "SignedTransactionGenerator"
    }

    /**
     * Generate signed transaction for voting
     * @param electionPairId The ID of the selected candidate pair
     * @param voterId The voter's unique identifier
     * @param region The voter's region
     * @return Signed transaction string or null if failed
     */
    fun generateVoteSignedTransaction(
        electionPairId: String,
        voterId: String,
        region: String
    ): String? {
        return try {
            Log.d(TAG, "🔐 Starting signed transaction generation")
            Log.d(TAG, "  - Election Pair ID: $electionPairId")
            Log.d(TAG, "  - Voter ID: $voterId")
            Log.d(TAG, "  - Region: $region")

            // Validate inputs
            if (electionPairId.isEmpty() || voterId.isEmpty() || region.isEmpty()) {
                Log.e(TAG, "❌ Invalid input parameters for signing")
                return null
            }

            // Create data string to sign
            val dataToSign = "$electionPairId:$voterId:$region"
            Log.d(TAG, "📝 Data to sign: $dataToSign")

            // Validate crypto key manager
            if (!cryptoKeyManager.hasStoredKeyPair()) {
                Log.e(TAG, "❌ No stored key pair available for signing")
                return null
            }

            if (!cryptoKeyManager.canSignData()) {
                Log.e(TAG, "❌ Crypto key manager cannot sign data")
                return null
            }

            // Generate signed transaction
            val signedTransaction = cryptoKeyManager.signData(dataToSign)

            if (signedTransaction.isNullOrEmpty()) {
                Log.e(TAG, "❌ Failed to generate signed transaction")
                return null
            }

            Log.d(TAG, "✅ Signed transaction generated successfully")
            Log.d(TAG, "  - Length: ${signedTransaction.length} characters")
            Log.d(TAG, "  - Preview: ${signedTransaction.take(16)}...")

            return signedTransaction

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ Security error during signing: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error during signing: ${e.message}", e)
            null
        }
    }

    /**
     * Validate signed transaction format
     * @param signedTransaction The signed transaction to validate
     * @return true if valid, false otherwise
     */
    fun validateSignedTransaction(signedTransaction: String?): Boolean {
        return when {
            signedTransaction.isNullOrEmpty() -> {
                Log.w(TAG, "⚠️ Signed transaction is null or empty")
                false
            }
            signedTransaction.length < 32 -> {
                Log.w(TAG, "⚠️ Signed transaction too short: ${signedTransaction.length} chars")
                false
            }
            !signedTransaction.matches(Regex("^[a-fA-F0-9]+$")) -> {
                Log.w(TAG, "⚠️ Signed transaction contains invalid characters")
                false
            }
            else -> {
                Log.d(TAG, "✅ Signed transaction validation passed")
                true
            }
        }
    }
}