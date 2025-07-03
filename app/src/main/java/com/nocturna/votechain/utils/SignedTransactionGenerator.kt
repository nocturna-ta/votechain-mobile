package com.nocturna.votechain.utils

import android.util.Log
import com.nocturna.votechain.security.CryptoKeyManager
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Enhanced Utility class for generating signed transactions for voting
 * Now with improved transaction format and verification capabilities
 */
class SignedTransactionGenerator(private val cryptoKeyManager: CryptoKeyManager) {

    companion object {
        private const val TAG = "SignedTransactionGenerator"
        private const val TRANSACTION_VERSION = "1.0"
        private const val HASH_ALGORITHM = "SHA-256"
        private const val TRANSACTION_TYPE = "vote"
    }

    /**
     * Generate signed transaction for voting with enhanced format
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
            Log.d(TAG, "🔐 Starting enhanced signed transaction generation")
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

            // Step 3: Create transaction data with enhanced format
            val timestamp = System.currentTimeMillis()
            val nonce = generateNonce()

            val transactionData = createEnhancedTransactionData(
                electionPairId = electionPairId,
                voterId = voterId,
                region = region,
                timestamp = timestamp,
                nonce = nonce
            )

            Log.d(TAG, "📝 Enhanced transaction data created")

            // Step 4: Create hash of transaction data
            val dataHash = createHash(transactionData)
            if (dataHash.isNullOrEmpty()) {
                Log.e(TAG, "❌ Failed to create data hash")
                return null
            }

            Log.d(TAG, "🔐 Data hash created: ${dataHash.take(16)}...")

            // Step 5: Sign the hash with enhanced cryptography
            val signature = cryptoKeyManager.signData(dataHash)
            if (signature.isNullOrEmpty()) {
                Log.e(TAG, "❌ Failed to generate signature")
                return null
            }

            Log.d(TAG, "✅ Signature generated successfully")

            // Step 6: Create final signed transaction with enhanced format
            val signedTransaction = createEnhancedSignedTransaction(
                transactionData = transactionData,
                signature = signature,
                timestamp = timestamp,
                nonce = nonce
            )

            Log.d(TAG, "✅ Enhanced signed transaction generated successfully")
            Log.d(TAG, "  - Total length: ${signedTransaction.length} characters")
            Log.d(TAG, "  - Preview: ${signedTransaction.take(50)}...")

            // Step 7: Verify transaction before returning
            if (!verifySignedTransaction(signedTransaction)) {
                Log.e(TAG, "❌ Generated transaction failed verification")
                return null
            }

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
     * Create enhanced transaction data in JSON format
     */
    private fun createEnhancedTransactionData(
        electionPairId: String,
        voterId: String,
        region: String,
        timestamp: Long,
        nonce: String
    ): String {
        return try {
            val jsonObject = JSONObject().apply {
                put("version", TRANSACTION_VERSION)
                put("type", TRANSACTION_TYPE)
                put("election_pair_id", electionPairId)
                put("voter_id", voterId)
                put("region", region)
                put("timestamp", timestamp)
                put("nonce", nonce)
                put("chain_id", "votechain-mainnet")
                put("gas_limit", "21000")
                put("gas_price", "1000000000")
            }

            // Return compact JSON string
            jsonObject.toString()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating enhanced transaction data: ${e.message}", e)
            // Fallback to pipe-separated format
            createLegacyTransactionData(electionPairId, voterId, region, timestamp, nonce)
        }
    }

    /**
     * Legacy transaction data format (fallback)
     */
    private fun createLegacyTransactionData(
        electionPairId: String,
        voterId: String,
        region: String,
        timestamp: Long,
        nonce: String
    ): String {
        return buildString {
            append("version:$TRANSACTION_VERSION")
            append("|type:$TRANSACTION_TYPE")
            append("|election_pair_id:$electionPairId")
            append("|voter_id:$voterId")
            append("|region:$region")
            append("|timestamp:$timestamp")
            append("|nonce:$nonce")
        }
    }

    /**
     * Create enhanced signed transaction format
     */
    private fun createEnhancedSignedTransaction(
        transactionData: String,
        signature: String,
        timestamp: Long,
        nonce: String
    ): String {
        return try {
            val signedTxJson = JSONObject().apply {
                put("version", TRANSACTION_VERSION)
                put("type", "signed_transaction")
                put("transaction_data", encodeBase64Safe(transactionData))
                put("signature", signature)
                put("signature_algorithm", detectSignatureAlgorithm(signature))
                put("timestamp", timestamp)
                put("nonce", nonce)
                put("public_key", cryptoKeyManager.getPublicKey())
                put("voter_address", cryptoKeyManager.getVoterAddress())
            }

            // Return base64 encoded JSON for API transmission
            encodeBase64Safe(signedTxJson.toString())
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating enhanced signed transaction: ${e.message}", e)
            // Fallback to legacy format
            createLegacySignedTransaction(transactionData, signature, timestamp, nonce)
        }
    }

    /**
     * Legacy signed transaction format (fallback)
     */
    private fun createLegacySignedTransaction(
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
     * Detect signature algorithm based on signature length
     */
    private fun detectSignatureAlgorithm(signature: String): String {
        return when {
            signature.length >= 130 -> "ECDSA"
            signature.length == 64 -> "SHA-256"
            else -> "UNKNOWN"
        }
    }

    /**
     * Verify signed transaction before sending
     */
    private fun verifySignedTransaction(signedTransaction: String): Boolean {
        return try {
            Log.d(TAG, "🔍 Verifying signed transaction...")

            // Extract transaction data and signature
            val (transactionData, signature) = extractTransactionComponents(signedTransaction)

            if (transactionData.isEmpty() || signature.isEmpty()) {
                Log.e(TAG, "❌ Failed to extract transaction components")
                return false
            }

            // Verify with crypto key manager
            val publicKey = cryptoKeyManager.getPublicKey()
            if (publicKey.isNullOrEmpty()) {
                Log.e(TAG, "❌ No public key available for verification")
                return false
            }

            // For ECDSA signatures, verify with public key
            if (signature.length >= 130) {
                val dataHash = createHash(transactionData)
                if (dataHash != null) {
                    val isValid = cryptoKeyManager.verifySignature(dataHash, signature, publicKey)
                    Log.d(TAG, if (isValid) "✅ Transaction verification successful" else "❌ Transaction verification failed")
                    return isValid
                }
            }

            // For other signatures, just validate format
            val isValidFormat = validateSignedTransaction(signedTransaction)
            Log.d(TAG, if (isValidFormat) "✅ Transaction format validation successful" else "❌ Transaction format validation failed")
            return isValidFormat

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verifying signed transaction: ${e.message}", e)
            false
        }
    }

    /**
     * Extract transaction components from signed transaction
     */
    private fun extractTransactionComponents(signedTransaction: String): Pair<String, String> {
        return try {
            // Try to parse as base64 encoded JSON first
            val decodedJson = decodeBase64Safe(signedTransaction)
            if (decodedJson.isNotEmpty()) {
                val jsonObject = JSONObject(decodedJson)
                val transactionData = decodeBase64Safe(jsonObject.getString("transaction_data"))
                val signature = jsonObject.getString("signature")
                return Pair(transactionData, signature)
            }

            // Fallback to legacy format parsing
            val parts = signedTransaction.split("|")
            var transactionData = ""
            var signature = ""

            for (part in parts) {
                when {
                    part.startsWith("data:") -> {
                        transactionData = decodeBase64Safe(part.substring(5))
                    }
                    part.startsWith("signature:") -> {
                        signature = decodeBase64Safe(part.substring(10))
                    }
                }
            }

            Pair(transactionData, signature)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error extracting transaction components: ${e.message}", e)
            Pair("", "")
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
            if (signedTransaction.length < 50) {
                Log.e(TAG, "❌ Signed transaction too short: ${signedTransaction.length} chars")
                return false
            }

            // Try enhanced format validation first
            if (validateEnhancedSignedTransactionFormat(signedTransaction)) {
                Log.d(TAG, "✅ Enhanced format validation passed")
                return true
            }

            // Fallback to legacy format validation
            val isValidLegacy = validateLegacySignedTransactionFormat(signedTransaction)
            Log.d(TAG, if (isValidLegacy) "✅ Legacy format validation passed" else "❌ All format validations failed")
            return isValidLegacy

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error validating signed transaction: ${e.message}", e)
            false
        }
    }

    /**
     * Validate enhanced signed transaction format
     */
    private fun validateEnhancedSignedTransactionFormat(signedTransaction: String): Boolean {
        return try {
            // Try to decode as base64 JSON
            val decodedJson = decodeBase64Safe(signedTransaction)
            if (decodedJson.isEmpty()) return false

            val jsonObject = JSONObject(decodedJson)

            // Check required fields
            val requiredFields = listOf("version", "type", "transaction_data", "signature")
            val hasAllFields = requiredFields.all { jsonObject.has(it) }

            if (!hasAllFields) {
                Log.d(TAG, "❌ Missing required fields in enhanced format")
                return false
            }

            // Validate signature
            val signature = jsonObject.getString("signature")
            if (signature.isEmpty()) {
                Log.d(TAG, "❌ Empty signature in enhanced format")
                return false
            }

            Log.d(TAG, "✅ Enhanced format validation successful")
            return true

        } catch (e: Exception) {
            Log.d(TAG, "❌ Enhanced format validation failed: ${e.message}")
            false
        }
    }

    /**
     * Validate legacy signed transaction format
     */
    private fun validateLegacySignedTransactionFormat(signedTransaction: String): Boolean {
        return try {
            // Check if it contains required legacy components
            val requiredComponents = listOf(":", "|", "signed_tx", "data", "signature")
            val hasAllComponents = requiredComponents.all { component ->
                signedTransaction.contains(component)
            }

            if (!hasAllComponents) {
                Log.d(TAG, "❌ Missing required components in legacy format")
                return false
            }

            // Additional validation: check if it's a valid format
            val isValidFormat = signedTransaction.matches(Regex("^[A-Za-z0-9+/=:_|.-]+$"))
            if (!isValidFormat) {
                Log.d(TAG, "❌ Invalid character format in legacy transaction")
                return false
            }

            Log.d(TAG, "✅ Legacy format validation successful")
            return true

        } catch (e: Exception) {
            Log.d(TAG, "❌ Legacy format validation failed: ${e.message}")
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
     * Safe base64 decoding that handles URL-safe characters
     */
    private fun decodeBase64Safe(data: String): String {
        return try {
            // Restore original base64 characters
            val restored = data.replace("-", "+")
                .replace("_", "/")
                .replace(".", "=")

            val decoded = android.util.Base64.decode(restored, android.util.Base64.DEFAULT)
            String(decoded, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error decoding base64: ${e.message}", e)
            "" // return empty string on error
        }
    }

    /**
     * Get transaction information for debugging
     */
    fun getTransactionInfo(signedTransaction: String): Map<String, String> {
        return try {
            val info = mutableMapOf<String, String>()

            // Try enhanced format first
            try {
                val decodedJson = decodeBase64Safe(signedTransaction)
                if (decodedJson.isNotEmpty()) {
                    val jsonObject = JSONObject(decodedJson)
                    info["format"] = "enhanced"
                    info["version"] = jsonObject.optString("version", "unknown")
                    info["type"] = jsonObject.optString("type", "unknown")
                    info["signature_algorithm"] = jsonObject.optString("signature_algorithm", "unknown")
                    info["voter_address"] = jsonObject.optString("voter_address", "unknown")
                    info["length"] = signedTransaction.length.toString()
                    info["preview"] = signedTransaction.take(50) + "..."
                    return info
                }
            } catch (e: Exception) {
                Log.d(TAG, "Not enhanced format, trying legacy...")
            }

            // Fallback to legacy format
            val parts = signedTransaction.split("|")
            info["format"] = "legacy"
            for (part in parts) {
                val keyValue = part.split(":", limit = 2)
                if (keyValue.size == 2) {
                    info[keyValue[0]] = keyValue[1].take(20) + "..."
                }
            }

            info["length"] = signedTransaction.length.toString()
            info["preview"] = signedTransaction.take(50) + "..."

            info
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error extracting transaction info: ${e.message}", e)
            mapOf("error" to "Failed to extract transaction info")
        }
    }

    /**
     * Verify transaction integrity before API submission
     */
    fun verifyTransactionIntegrity(signedTransaction: String): Boolean {
        return try {
            Log.d(TAG, "🔍 Verifying transaction integrity...")

            // Basic format validation
            if (!validateSignedTransaction(signedTransaction)) {
                Log.e(TAG, "❌ Transaction format validation failed")
                return false
            }

            // Extract and verify components
            val (transactionData, signature) = extractTransactionComponents(signedTransaction)

            if (transactionData.isEmpty() || signature.isEmpty()) {
                Log.e(TAG, "❌ Cannot extract transaction components")
                return false
            }

            // Verify signature if possible
            val publicKey = cryptoKeyManager.getPublicKey()
            if (!publicKey.isNullOrEmpty() && signature.length >= 130) {
                val dataHash = createHash(transactionData)
                if (dataHash != null) {
                    val isValid = cryptoKeyManager.verifySignature(dataHash, signature, publicKey)
                    Log.d(TAG, if (isValid) "✅ Signature verification successful" else "❌ Signature verification failed")
                    return isValid
                }
            }

            Log.d(TAG, "✅ Transaction integrity check passed (format validation only)")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verifying transaction integrity: ${e.message}", e)
            false
        }
    }
}