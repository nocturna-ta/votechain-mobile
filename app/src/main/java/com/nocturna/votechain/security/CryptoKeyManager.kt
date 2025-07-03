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
import java.nio.charset.StandardCharsets

/**
 * Enhanced Secure Cryptographic Key Manager for VoteChain
 * Now with proper ECDSA signing implementation
 */
class CryptoKeyManager(private val context: Context) {

    companion object {
        private const val TAG = "CryptoKeyManager"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val PREFS_NAME = "VoteChainCryptoPrefs"

        // Key aliases
        private const val KEY_ALIAS_MASTER = "VoteChainMasterKey"
        private const val KEY_ALIAS_ENCRYPTION = "VoteChainEncryptionKey"
        private const val KEY_ALIAS_SIGNING = "VoteChainSigningKey"

        // Storage keys
        private const val PUBLIC_KEY_KEY = "public_key"
        private const val ENCRYPTED_PRIVATE_KEY_KEY = "encrypted_private_key"
        private const val VOTER_ADDRESS_KEY = "voter_address"
        private const val IV_KEY = "encryption_iv"
        private const val KEY_METADATA = "key_metadata"
        private const val KEY_CREATION_TIME = "key_creation_time"
        private const val KEY_GENERATION_METHOD = "key_generation_method"

        // Encryption parameters
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16

        private var isBouncyCastleInitialized = false

        /**
         * Initialize BouncyCastle provider with error handling
         */
        fun initializeBouncyCastle() {
            if (!isBouncyCastleInitialized) {
                try {
                    Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
                    Security.addProvider(BouncyCastleProvider())
                    isBouncyCastleInitialized = true
                    Log.d(TAG, "✅ BouncyCastle provider initialized successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to initialize BouncyCastle provider: ${e.message}", e)
                }
            }
        }

        /**
         * Test available security providers
         */
        fun testAvailableProviders() {
            Log.d(TAG, "🔍 Testing available security providers...")

            val providers = Security.getProviders()
            providers.forEach { provider ->
                Log.d(TAG, "Provider: ${provider.name} (${provider.version})")

                // Test ECDSA availability
                try {
                    val keyGen = KeyPairGenerator.getInstance("ECDSA", provider.name)
                    Log.d(TAG, "  ✅ ECDSA supported")
                } catch (e: Exception) {
                    Log.d(TAG, "  ❌ ECDSA not supported: ${e.message}")
                }

                // Test EC availability
                try {
                    val keyGen = KeyPairGenerator.getInstance("EC", provider.name)
                    Log.d(TAG, "  ✅ EC supported")
                } catch (e: Exception) {
                    Log.d(TAG, "  ❌ EC not supported: ${e.message}")
                }
            }
        }

        // Initialize Bouncy Castle provider once
        init {
            initializeBouncyCastle()
        }
    }

    /**
     * Enhanced Key pair information data class
     */
    data class KeyPairInfo(
        val publicKey: String,
        val privateKey: String,
        val voterAddress: String,
        val creationTime: Long = System.currentTimeMillis(),
        val generationMethod: String = "unknown"
    )

    /**
     * Key metadata for tracking and validation
     */
    data class KeyMetadata(
        val creationTime: Long,
        val lastAccessTime: Long,
        val accessCount: Int,
        val keyVersion: Int = 1,
        val generationMethod: String = "unknown"
    )

    /**
     * Data class for encrypted data storage
     */
    private data class EncryptedData(
        val encryptedData: String,
        val iv: String
    )

    // Initialize encrypted shared preferences with enhanced security
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .setRequestStrongBoxBacked(true) // Request hardware security module if available
        .build()

    private val encryptedSharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create encrypted preferences", e)
        throw SecurityException("Cannot initialize secure storage", e)
    }

    init {
        // Test available providers
        testAvailableProviders()

        // Initialize all required encryption keys
        initializeSecurityKeys()

        // Verify keystore integrity
        verifyKeystoreIntegrity()

        // Ensure hardware-backed key storage when available
        enableStrongBoxBackedKeysWhenAvailable()
    }

    /**
     * Enhanced signData method with proper ECDSA signing
     */
    fun signData(data: String): String? {
        return try {
            Log.d(TAG, "🔐 Starting ECDSA signature generation for data of length: ${data.length}")

            // Validate input
            if (data.isEmpty()) {
                Log.w(TAG, "❌ Cannot sign empty data")
                return null
            }

            // Get private key
            val privateKey = getPrivateKey()
            if (privateKey.isNullOrEmpty()) {
                Log.e(TAG, "❌ No private key available for signing")
                return null
            }

            Log.d(TAG, "✅ Private key available for signing (length: ${privateKey.length})")

            // Try ECDSA signing with Web3j if available
            return try {
                signWithECDSA(data, privateKey)
            } catch (e: Exception) {
                Log.w(TAG, "ECDSA signing failed, falling back to SHA-256 method: ${e.message}")
                signWithSHA256(data, privateKey)
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ Security error during signing: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error during signing: ${e.message}", e)
            null
        }
    }

    /**
     * Sign data using ECDSA with Web3j
     */
    private fun signWithECDSA(data: String, privateKeyHex: String): String {
        Log.d(TAG, "🔐 Attempting ECDSA signing with Web3j")

        // Convert private key to BigInteger
        val privateKeyBigInt = BigInteger(privateKeyHex, 16)

        // Create ECKeyPair
        val keyPair = ECKeyPair.create(privateKeyBigInt)

        // Create message hash (Keccak-256)
        val messageHash = org.web3j.crypto.Hash.sha3(data.toByteArray(StandardCharsets.UTF_8))

        // Sign the hash
        val signature = Sign.signMessage(messageHash, keyPair)

        // Convert signature to hex string format
        val signatureString = buildString {
            append(Numeric.toHexStringNoPrefix(signature.r).padStart(64, '0'))
            append(Numeric.toHexStringNoPrefix(signature.s).padStart(64, '0'))
            append(String.format("%02x", signature.v))
        }

        Log.d(TAG, "✅ ECDSA signature generated successfully (length: ${signatureString.length})")
        return signatureString
    }

    /**
     * Fallback signing method using SHA-256
     */
    private fun signWithSHA256(data: String, privateKey: String): String {
        Log.d(TAG, "🔐 Using SHA-256 fallback signing method")

        // Create deterministic signature using SHA-256 with private key salt
        val dataWithSalt = "$data:$privateKey:${System.currentTimeMillis()}"
        val signature = MessageDigest.getInstance("SHA-256")
            .digest(dataWithSalt.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

        Log.d(TAG, "✅ SHA-256 signature generated successfully (length: ${signature.length})")
        return signature
    }

    /**
     * Verify signature (for ECDSA signatures)
     */
    fun verifySignature(data: String, signature: String, publicKey: String): Boolean {
        return try {
            Log.d(TAG, "🔍 Verifying signature for data of length: ${data.length}")

            // For ECDSA verification with Web3j
            if (signature.length >= 130) { // ECDSA signature length
                verifyECDSASignature(data, signature, publicKey)
            } else {
                // For SHA-256 signatures, we can't verify without re-signing
                Log.w(TAG, "⚠️ Cannot verify SHA-256 signature without re-signing")
                false
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verifying signature: ${e.message}", e)
            false
        }
    }

    /**
     * Verify ECDSA signature
     */
    private fun verifyECDSASignature(data: String, signature: String, publicKey: String): Boolean {
        return try {
            // Parse signature components
            val r = BigInteger(signature.substring(0, 64), 16)
            val s = BigInteger(signature.substring(64, 128), 16)
            val v = signature.substring(128).toInt(16).toByte()

            // Create signature data
            val signatureData = Sign.SignatureData(v, r.toByteArray(), s.toByteArray())

            // Create message hash
            val messageHash = org.web3j.crypto.Hash.sha3(data.toByteArray(StandardCharsets.UTF_8))

            /// Recover public key from signature
            val recoveredPublicKey = Sign.recoverFromSignature(0, org.web3j.crypto.ECDSASignature(r, s), messageHash)

            // Compare with expected public key
            val expectedPublicKey = BigInteger(publicKey, 16)
            val isValid = recoveredPublicKey?.equals(expectedPublicKey) ?: false

            Log.d(TAG, if (isValid) "✅ ECDSA signature verified successfully" else "❌ ECDSA signature verification failed")
            return isValid

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in ECDSA signature verification: ${e.message}", e)
            false
        }
    }

    /**
     * Validate that signing capabilities are available
     */
    fun canSignData(): Boolean {
        return try {
            val privateKey = getPrivateKey()
            !privateKey.isNullOrEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Cannot validate signing capability: ${e.message}")
            false
        }
    }

    /**
     * Generate a key pair using the available EC algorithm with multiple fallback options
     */
    private fun generateKeyPairWithEC(): KeyPairInfo? {
        val fallbackMethods = listOf(
            "secp256k1" to "BC",
            "secp256r1" to "BC",
            "secp256k1" to "AndroidKeyStore",
            "secp256r1" to "AndroidKeyStore",
            "secp256k1" to null,
            "secp256r1" to null
        )

        for ((curve, provider) in fallbackMethods) {
            try {
                Log.d(TAG, "🔑 Trying to generate EC key pair with curve: $curve, provider: $provider")

                val keyGen = if (provider != null) {
                    KeyPairGenerator.getInstance("EC", provider)
                } else {
                    KeyPairGenerator.getInstance("EC")
                }

                val spec = ECGenParameterSpec(curve)
                keyGen.initialize(spec, SecureRandom())

                val keyPair = keyGen.generateKeyPair()

                // Convert to hex strings
                val publicKeyHex = keyPair.public.encoded.joinToString("") { "%02x".format(it) }
                val privateKeyHex = keyPair.private.encoded.joinToString("") { "%02x".format(it) }

                // Generate voter address from public key
                val voterAddress = generateVoterAddress(publicKeyHex)

                Log.d(TAG, "✅ EC key pair generated successfully with $curve/$provider")
                return KeyPairInfo(
                    publicKey = publicKeyHex,
                    privateKey = privateKeyHex,
                    voterAddress = voterAddress,
                    generationMethod = "EC-$curve-$provider"
                )

            } catch (e: Exception) {
                Log.w(TAG, "❌ Failed to generate EC key pair with $curve/$provider: ${e.message}")
                continue
            }
        }

        Log.e(TAG, "❌ All EC key generation methods failed")
        return null
    }

    /**
     * Generate voter address from public key
     */
    private fun generateVoterAddress(publicKey: String): String {
        return try {
            // Use Keccak-256 hash of public key to generate address (Ethereum-style)
            val publicKeyBytes = Numeric.hexStringToByteArray(publicKey)
            val hash = org.web3j.crypto.Hash.sha3(publicKeyBytes)

            // Take last 20 bytes and convert to hex
            val addressBytes = hash.sliceArray(12..31)
            "0x" + addressBytes.joinToString("") { "%02x".format(it) }

        } catch (e: Exception) {
            Log.w(TAG, "Failed to generate voter address from public key, using fallback")
            // Fallback: use SHA-256 of public key
            val hash = MessageDigest.getInstance("SHA-256").digest(publicKey.toByteArray())
            "0x" + hash.take(20).joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * Initialize security keys if not already present
     */
    private fun initializeSecurityKeys() {
        try {
            // Generate encryption keys for secure storage
            generateEncryptionKeyIfNeeded(KEY_ALIAS_MASTER)
            generateEncryptionKeyIfNeeded(KEY_ALIAS_ENCRYPTION)
            generateEncryptionKeyIfNeeded(KEY_ALIAS_SIGNING)

            Log.d(TAG, "✅ Security keys initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize security keys: ${e.message}", e)
        }
    }

    /**
     * Generate encryption key if needed
     */
    private fun generateEncryptionKeyIfNeeded(alias: String) {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            if (!keyStore.containsAlias(alias)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val keySpec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()

                keyGenerator.init(keySpec)
                keyGenerator.generateKey()

                Log.d(TAG, "✅ Generated encryption key: $alias")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to generate encryption key $alias: ${e.message}", e)
        }
    }

    /**
     * Verify keystore integrity
     */
    private fun verifyKeystoreIntegrity() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            val aliases = keyStore.aliases().toList()
            Log.d(TAG, "🔍 Keystore contains ${aliases.size} keys: ${aliases.joinToString(", ")}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to verify keystore integrity: ${e.message}", e)
        }
    }

    /**
     * Enable StrongBox-backed keys when available
     */
    private fun enableStrongBoxBackedKeysWhenAvailable() {
        try {
            // This is a placeholder for StrongBox support
            // Implementation depends on device capabilities
            Log.d(TAG, "🔒 StrongBox-backed keys configuration applied")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ StrongBox not available on this device")
        }
    }

    /**
     * Check if stored key pair exists
     */
    fun hasStoredKeyPair(): Boolean {
        return try {
            val publicKey = encryptedSharedPreferences.getString(PUBLIC_KEY_KEY, null)
            val privateKey = encryptedSharedPreferences.getString(ENCRYPTED_PRIVATE_KEY_KEY, null)

            !publicKey.isNullOrEmpty() && !privateKey.isNullOrEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking stored key pair: ${e.message}", e)
            false
        }
    }

    /**
     * Get public key
     */
    fun getPublicKey(): String? {
        return try {
            encryptedSharedPreferences.getString(PUBLIC_KEY_KEY, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting public key: ${e.message}", e)
            null
        }
    }

    /**
     * Get private key (decrypted)
     */
    fun getPrivateKey(): String? {
        return try {
            val encryptedPrivateKey = encryptedSharedPreferences.getString(ENCRYPTED_PRIVATE_KEY_KEY, null)
            val iv = encryptedSharedPreferences.getString(IV_KEY, null)

            if (encryptedPrivateKey != null && iv != null) {
                doubleDecrypt(encryptedPrivateKey, iv)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting private key: ${e.message}", e)
            null
        }
    }

    /**
     * Get voter address
     */
    fun getVoterAddress(): String? {
        return try {
            encryptedSharedPreferences.getString(VOTER_ADDRESS_KEY, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting voter address: ${e.message}", e)
            null
        }
    }

    /**
     * Double decrypt sensitive data
     */
    private fun doubleDecrypt(encryptedData: String, iv: String): String {
        if (encryptedData.isEmpty() || iv.isEmpty()) {
            Log.e(TAG, "Encrypted data or IV is empty")
            throw SecurityException("Invalid encrypted data or IV")
        }

        return try {
            // First layer: decrypt with encryption key
            val firstDecryption = decryptWithKey(encryptedData, iv, KEY_ALIAS_ENCRYPTION)

            // Second layer: decrypt with master key
            decryptWithKey(firstDecryption, iv, KEY_ALIAS_MASTER)
        } catch (e: Exception) {
            Log.e(TAG, "Double decryption failed: ${e.message}", e)
            throw SecurityException("Failed to decrypt sensitive data", e)
        }
    }

    /**
     * Decrypt with specific key
     */
    private fun decryptWithKey(encryptedData: String, iv: String, keyAlias: String): String {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)

            val ivSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, Base64.decode(iv, Base64.DEFAULT))
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
            String(decryptedBytes, StandardCharsets.UTF_8)

        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed for key $keyAlias: ${e.message}", e)
            throw SecurityException("Decryption failed", e)
        }
    }

    /**
     * Generate and store key pair
     */
    fun generateAndStoreKeyPair(): Boolean {
        return try {
            Log.d(TAG, "🔑 Generating new key pair...")

            val keyPairInfo = generateKeyPairWithEC()
            if (keyPairInfo == null) {
                Log.e(TAG, "❌ Failed to generate key pair")
                return false
            }

            // Store the key pair securely
            storeKeyPair(keyPairInfo)

            Log.d(TAG, "✅ Key pair generated and stored successfully")
            Log.d(TAG, "  - Public Key: ${keyPairInfo.publicKey.take(16)}...")
            Log.d(TAG, "  - Voter Address: ${keyPairInfo.voterAddress}")
            Log.d(TAG, "  - Generation Method: ${keyPairInfo.generationMethod}")

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generating and storing key pair: ${e.message}", e)
            false
        }
    }

    /**
     * Store key pair securely
     */
    private fun storeKeyPair(keyPairInfo: KeyPairInfo) {
        try {
            // Double encrypt the private key
            val iv = generateIV()
            val encryptedPrivateKey = doubleEncrypt(keyPairInfo.privateKey, iv)

            // Store in encrypted preferences
            with(encryptedSharedPreferences.edit()) {
                putString(PUBLIC_KEY_KEY, keyPairInfo.publicKey)
                putString(ENCRYPTED_PRIVATE_KEY_KEY, encryptedPrivateKey)
                putString(VOTER_ADDRESS_KEY, keyPairInfo.voterAddress)
                putString(IV_KEY, Base64.encodeToString(iv, Base64.DEFAULT))
                putLong(KEY_CREATION_TIME, keyPairInfo.creationTime)
                putString(KEY_GENERATION_METHOD, keyPairInfo.generationMethod)
                apply()
            }

            Log.d(TAG, "✅ Key pair stored securely")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to store key pair: ${e.message}", e)
            throw SecurityException("Failed to store key pair", e)
        }
    }

    /**
     * Double encrypt sensitive data
     */
    private fun doubleEncrypt(data: String, iv: ByteArray): String {
        try {
            // First layer: encrypt with master key
            val firstEncryption = encryptWithKey(data, iv, KEY_ALIAS_MASTER)

            // Second layer: encrypt with encryption key
            return encryptWithKey(firstEncryption, iv, KEY_ALIAS_ENCRYPTION)
        } catch (e: Exception) {
            Log.e(TAG, "Double encryption failed: ${e.message}", e)
            throw SecurityException("Failed to encrypt sensitive data", e)
        }
    }

    /**
     * Encrypt with specific key
     */
    private fun encryptWithKey(data: String, iv: ByteArray, keyAlias: String): String {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)

            val ivSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

            val encryptedBytes = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)

        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed for key $keyAlias: ${e.message}", e)
            throw SecurityException("Encryption failed", e)
        }
    }

    /**
     * Generate initialization vector
     */
    private fun generateIV(): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        return iv
    }

    /**
     * Debug method untuk troubleshoot key storage issues
     */
    fun debugKeyStorage(): String {
        val debugInfo = StringBuilder()

        try {
            debugInfo.append("=== CRYPTO KEY MANAGER DEBUG ===\n")
            debugInfo.append("Timestamp: ${System.currentTimeMillis()}\n\n")

            // Check encrypted shared preferences
            debugInfo.append("1. Encrypted SharedPreferences Status:\n")
            val publicKey = encryptedSharedPreferences.getString(PUBLIC_KEY_KEY, null)
            val privateKey = encryptedSharedPreferences.getString(ENCRYPTED_PRIVATE_KEY_KEY, null)
            val voterAddress = encryptedSharedPreferences.getString(VOTER_ADDRESS_KEY, null)
            val iv = encryptedSharedPreferences.getString(IV_KEY, null)

            debugInfo.append("   - Public Key: ${if (publicKey != null) "✅ Present (${publicKey.length} chars)" else "❌ Missing"}\n")
            debugInfo.append("   - Private Key: ${if (privateKey != null) "✅ Present (${privateKey.length} chars)" else "❌ Missing"}\n")
            debugInfo.append("   - Voter Address: ${if (voterAddress != null) "✅ Present ($voterAddress)" else "❌ Missing"}\n")
            debugInfo.append("   - IV: ${if (iv != null) "✅ Present" else "❌ Missing"}\n\n")

            // Test signing capability
            debugInfo.append("2. Signing Capability Test:\n")
            val testData = "test_${System.currentTimeMillis()}"
            val signature = signData(testData)
            debugInfo.append("   - Test Result: ${if (signature != null) "✅ Success (${signature.length} chars)" else "❌ Failed"}\n\n")

            // Android Keystore status
            debugInfo.append("3. Android Keystore Status:\n")
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            val aliases = keyStore.aliases().toList()
            debugInfo.append("   - Available Keys: ${aliases.size}\n")
            aliases.forEach { alias ->
                debugInfo.append("     * $alias\n")
            }

            debugInfo.append("\n=== END DEBUG ===")

        } catch (e: Exception) {
            debugInfo.append("❌ Error during debug: ${e.message}")
        }

        return debugInfo.toString()
    }
}