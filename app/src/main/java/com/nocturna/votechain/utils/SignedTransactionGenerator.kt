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
        private const val TRANSACTION_VERSION = "1.0"
        private const val HASH_ALGORITHM = "SHA-256"
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

            // Step 1: Validate inputs
            if (!validateInputs(electionPairId, voterId, region)) {
                Log.e(TAG, "❌ Input validation failed")
                return null
            }

            // Step 2: Validate crypto key manager
            if (!validateCryptoKeyManager()) {
                Log.e(TAG, "❌ Crypto key manager validation failed")
                return null
            }

            // Step 3: Create transaction data
            val timestamp = System.currentTimeMillis()
            val nonce = generateNonce()

            val transactionData = createTransactionData(
                electionPairId = electionPairId,
                voterId = voterId,
                region = region,
                timestamp = timestamp,
                nonce = nonce
            )

            Log.d(TAG, "📝 Transaction data created: $transactionData")

            // Step 4: Create hash of transaction data
            val dataHash = createHash(transactionData)
            if (dataHash.isNullOrEmpty()) {
                Log.e(TAG, "❌ Failed to create data hash")
                return null
            }

            Log.d(TAG, "🔐 Data hash created: ${dataHash.take(16)}...")

            // Step 5: Sign the hash
            val signature = cryptoKeyManager.signData(dataHash)
            if (signature.isNullOrEmpty()) {
                Log.e(TAG, "❌ Failed to generate signature")
                return null
            }

            Log.d(TAG, "✅ Signature generated successfully")

            // Step 6: Create final signed transaction
            val signedTransaction = createSignedTransaction(
                transactionData = transactionData,
                signature = signature,
                timestamp = timestamp,
                nonce = nonce
            )

            Log.d(TAG, "✅ Signed transaction generated successfully")
            Log.d(TAG, "  - Total length: ${signedTransaction.length} characters")
            Log.d(TAG, "  - Preview: ${signedTransaction.take(32)}...")

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
        return try {
            if (signedTransaction.isNullOrEmpty()) {
                Log.e(TAG, "❌ Signed transaction is null or empty")
                return false
            }

            // Check minimum length
            if (signedTransaction.length < 32) {
                Log.e(TAG, "❌ Signed transaction too short: ${signedTransaction.length} chars")
                return false
            }

            // Check if it contains required components (basic format validation)
            val requiredComponents = listOf(":", "|", "_")
            val hasAllComponents = requiredComponents.all { component ->
                signedTransaction.contains(component)
            }

            if (!hasAllComponents) {
                Log.e(TAG, "❌ Signed transaction missing required components")
                return false
            }

            // Additional validation: check if it's a valid base64-like format
            val isValidFormat = signedTransaction.matches(Regex("^[A-Za-z0-9+/=:_|.-]+$"))
            if (!isValidFormat) {
                Log.e(TAG, "❌ Signed transaction has invalid format")
                return false
            }

            Log.d(TAG, "✅ Signed transaction validation passed")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error validating signed transaction: ${e.message}", e)
            false
        }
    }

    /**
     * Validate input parameters
     */
    private fun validateInputs(electionPairId: String, voterId: String, region: String): Boolean {
        return when {
            electionPairId.isEmpty() -> {
                Log.e(TAG, "❌ Election pair ID is empty")
                false
            }
            voterId.isEmpty() -> {
                Log.e(TAG, "❌ Voter ID is empty")
                false
            }
            region.isEmpty() -> {
                Log.e(TAG, "❌ Region is empty")
                false
            }
            electionPairId.length > 100 -> {
                Log.e(TAG, "❌ Election pair ID too long")
                false
            }
            voterId.length > 100 -> {
                Log.e(TAG, "❌ Voter ID too long")
                false
            }
            region.length > 50 -> {
                Log.e(TAG, "❌ Region too long")
                false
            }
            else -> {
                Log.d(TAG, "✅ Input validation passed")
                true
            }
        }
    }

    /**
     * Validate crypto key manager
     */
    private fun validateCryptoKeyManager(): Boolean {
        return try {
            if (!cryptoKeyManager.hasStoredKeyPair()) {
                Log.e(TAG, "❌ No stored key pair available")
                return false
            }

            // Test signing capability with a simple test
            val testData = "test_${System.currentTimeMillis()}"
            val testSignature = cryptoKeyManager.signData(testData)

            if (testSignature.isNullOrEmpty()) {
                Log.e(TAG, "❌ Crypto key manager cannot sign data")
                return false
            }

            Log.d(TAG, "✅ Crypto key manager validation passed")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Crypto key manager validation failed: ${e.message}", e)
            false
        }
    }

    /**
     * Create transaction data string
     */
    private fun createTransactionData(
        electionPairId: String,
        voterId: String,
        region: String,
        timestamp: Long,
        nonce: String
    ): String {
        return buildString {
            append("version:$TRANSACTION_VERSION")
            append("|type:vote")
            append("|election_pair_id:$electionPairId")
            append("|voter_id:$voterId")
            append("|region:$region")
            append("|timestamp:$timestamp")
            append("|nonce:$nonce")
        }
    }

    /**
     * Create SHA-256 hash of data
     */
    private fun createHash(data: String): String? {
        return try {
            val digest = MessageDigest.getInstance(HASH_ALGORITHM)
            val hashBytes = digest.digest(data.toByteArray(StandardCharsets.UTF_8))

            // Convert to hex string
            hashBytes.joinToString("") { "%02x".format(it) }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating hash: ${e.message}", e)
            null
        }
    }

    /**
     * Generate a unique nonce for the transaction
     */
    private fun generateNonce(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1..999999).random()
        return "${timestamp}_${random}"
    }

    /**
     * Create the final signed transaction format
     */
    private fun createSignedTransaction(
        transactionData: String,
        signature: String,
        timestamp: Long,
        nonce: String
    ): String {
        return buildString {
            append("signed_tx:")
            append("data:${encodeBase64Safe(transactionData)}")
            append("|signature:${encodeBase64Safe(signature)}")
            append("|timestamp:$timestamp")
            append("|nonce:$nonce")
            append("|version:$TRANSACTION_VERSION")
        }
    }

    /**
     * Safe base64 encoding that replaces problematic characters
     */
    private fun encodeBase64Safe(data: String): String {
        return try {
            val encoded = android.util.Base64.encodeToString(
                data.toByteArray(StandardCharsets.UTF_8),
                android.util.Base64.NO_WRAP
            )
            // Make URL-safe by replacing problematic characters
            encoded.replace("+", "-")
                .replace("/", "_")
                .replace("=", ".")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error encoding base64: ${e.message}", e)
            data // fallback to original data
        }
    }

    /**
     * Additional method: Get transaction info for debugging
     */
    fun getTransactionInfo(signedTransaction: String): Map<String, String> {
        return try {
            val info = mutableMapOf<String, String>()

            // Extract basic info from signed transaction
            val parts = signedTransaction.split("|")
            for (part in parts) {
                val keyValue = part.split(":", limit = 2)
                if (keyValue.size == 2) {
                    info[keyValue[0]] = keyValue[1]
                }
            }

            info["length"] = signedTransaction.length.toString()
            info["preview"] = signedTransaction.take(32) + "..."

            info
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error extracting transaction info: ${e.message}", e)
            mapOf("error" to "Failed to extract transaction info")
        }
    }
}