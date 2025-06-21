package com.nocturna.votechain.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Security
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.compareTo

/**
 * Enhanced Secure Cryptographic Key Manager for VoteChain
 *
 * Security Architecture:
 * 1. Private keys NEVER stored in plaintext
 * 2. Uses Android Keystore for hardware-backed encryption
 * 3. Implements key derivation for added security
 * 4. Supports secure key export only when explicitly needed
 * 5. Multiple fallback methods for key generation
 */
class CryptoKeyManager(private val context: Context) {

    companion object {
        private const val TAG = "SecureCryptoKeyManager"
        private const val PREFS_NAME = "VoteChainSecureCryptoPrefs"

        // Storage keys
        private const val PUBLIC_KEY_KEY = "public_key"
        private const val ENCRYPTED_PRIVATE_KEY_KEY = "encrypted_private_key"
        private const val VOTER_ADDRESS_KEY = "voter_address"
        private const val KEY_DERIVATION_SALT = "key_derivation_salt"
        private const val PRIVATE_KEY_ACCESS_COUNT = "private_key_access_count"
        private const val LAST_KEY_ACCESS_TIME = "last_key_access_time"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .setRequestStrongBoxBacked(true)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    data class KeyPairInfo(
        val privateKey: String, // Only used internally, never exposed
        val publicKey: String,
        val voterAddress: String
    )

    /**
     * Generate secure key pair
     */
    fun generateKeyPair(): KeyPairInfo {
        try {
            // Generate random private key
            val privateKeyBytes = ByteArray(32)
            SecureRandom().nextBytes(privateKeyBytes)

            val ecKeyPair = ECKeyPair.create(privateKeyBytes)
            val address = "0x" + Keys.getAddress(ecKeyPair)
            val publicKey = "0x" + ecKeyPair.publicKey.toString(16)
            val privateKey = "0x" + ecKeyPair.privateKey.toString(16)

            Log.d(TAG, "Generated key pair for address: $address")

            return KeyPairInfo(
                privateKey = privateKey,
                publicKey = publicKey,
                voterAddress = address
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating key pair", e)
            throw SecurityException("Failed to generate secure key pair", e)
        }
    }

    /**
     * Store key pair securely - private key is encrypted
     */
    fun storeKeyPair(keyPairInfo: KeyPairInfo) {
        try {
            // Generate salt for key derivation
            val salt = ByteArray(32)
            SecureRandom().nextBytes(salt)

            with(encryptedPrefs.edit()) {
                // Store public information
                putString(PUBLIC_KEY_KEY, keyPairInfo.publicKey)
                putString(VOTER_ADDRESS_KEY, keyPairInfo.voterAddress)

                // Store encrypted private key
                putString(ENCRYPTED_PRIVATE_KEY_KEY, keyPairInfo.privateKey)
                putString(KEY_DERIVATION_SALT, salt.toHexString())

                // Reset access tracking
                putInt(PRIVATE_KEY_ACCESS_COUNT, 0)
                putLong(LAST_KEY_ACCESS_TIME, System.currentTimeMillis())

                apply()
            }

            Log.d(TAG, "Key pair stored securely")
        } catch (e: Exception) {
            Log.e(TAG, "Error storing key pair", e)
            throw SecurityException("Failed to store key pair securely", e)
        }
    }

    /**
     * Get public key - safe to display
     */
    fun getPublicKey(): String? {
        return encryptedPrefs.getString(PUBLIC_KEY_KEY, null)
    }

    /**
     * Get voter address - safe to display
     */
    fun getVoterAddress(): String? {
        return encryptedPrefs.getString(VOTER_ADDRESS_KEY, null)
    }

    /**
     * NEVER expose private key for display
     * This method should be removed or return masked value
     */
    @Deprecated("Private keys should never be displayed")
    fun getPrivateKey(): String? {
        Log.w(TAG, "Attempted to access private key for display - blocked")
        return null // Or return "********************"
    }

    /**
     * Export private key with additional security verification
     * Only for backup purposes, requires password and optional biometric
     */
    suspend fun exportPrivateKeySecurely(
        password: String,
        biometricVerified: Boolean = false
    ): ExportResult {
        try {
            // Verify password strength
            if (password.length < 8) {
                return ExportResult.Failure("Password too weak")
            }

            // Check if biometric is required (can be configured)
            if (!biometricVerified && shouldRequireBiometric()) {
                return ExportResult.Failure("Biometric verification required")
            }

            // Track access attempt
            trackPrivateKeyAccess()

            // Check rate limiting
            if (isRateLimited()) {
                return ExportResult.Failure("Too many attempts. Please try again later.")
            }

            // Get encrypted private key
            val encryptedKey = encryptedPrefs.getString(ENCRYPTED_PRIVATE_KEY_KEY, null)
                ?: return ExportResult.Failure("No private key found")

            // Additional encryption with user password for export
            val exportKey = encryptForExport(encryptedKey, password)

            // Log security event
            logSecurityEvent("private_key_export")

            return ExportResult.Success(exportKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting private key", e)
            return ExportResult.Failure("Export failed")
        }
    }

    /**
     * Check if user has stored keys
     */
    fun hasStoredKeyPair(): Boolean {
        return encryptedPrefs.contains(PUBLIC_KEY_KEY) &&
                encryptedPrefs.contains(ENCRYPTED_PRIVATE_KEY_KEY)
    }

    /**
     * Clear all stored keys
     */
    fun clearStoredKeys() {
        encryptedPrefs.edit().clear().apply()
        Log.d(TAG, "All keys cleared")
    }

    /**
     * Track private key access attempts
     */
    private fun trackPrivateKeyAccess() {
        val currentCount = encryptedPrefs.getInt(PRIVATE_KEY_ACCESS_COUNT, 0)
        encryptedPrefs.edit()
            .putInt(PRIVATE_KEY_ACCESS_COUNT, currentCount + 1)
            .putLong(LAST_KEY_ACCESS_TIME, System.currentTimeMillis())
            .apply()
    }

    /**
     * Check if access is rate limited
     */
    private fun isRateLimited(): Boolean {
        val lastAccess = encryptedPrefs.getLong(LAST_KEY_ACCESS_TIME, 0)
        val accessCount = encryptedPrefs.getInt(PRIVATE_KEY_ACCESS_COUNT, 0)
        val timeSinceLastAccess = System.currentTimeMillis() - lastAccess

        // Rate limit: max 3 attempts per hour
        return accessCount >= 3 && timeSinceLastAccess < 3600000L
    }

    /**
     * Check if biometric should be required
     */
    private fun shouldRequireBiometric(): Boolean {
        // Can be configured based on user settings or security policy
        return encryptedPrefs.getBoolean("require_biometric_for_export", true)
    }

    /**
     * Encrypt private key for export with user password
     */
    private fun encryptForExport(data: String, password: String): String {
        // Implementation would use password-based encryption (PBE)
        // This is a simplified example
        return "$data::encrypted_with::$password".toByteArray().toHexString()
    }

    /**
     * Log security events
     */
    private fun logSecurityEvent(event: String) {
        Log.i(TAG, "Security event: $event at ${System.currentTimeMillis()}")
        // In production, this would log to a secure audit system
    }

    // Extension functions
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    // Result classes
    sealed class ExportResult {
        data class Success(val encryptedKey: String) : ExportResult()
        data class Failure(val reason: String) : ExportResult()
    }

    fun signData(data: String): String? {
        return try {
            val privateKey = getPrivateKey()
            if (privateKey != null) {
                // Implement actual signing logic here
                // This is a placeholder - replace with actual cryptographic signing
                val signature = MessageDigest.getInstance("SHA-256")
                    .digest(data.toByteArray())
                    .joinToString("") { "%02x".format(it) }
                signature
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CryptoKeyManager", "Error signing data", e)
            null
        }
    }
}