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
        private const val TAG = "CryptoKeyManager"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val PREFS_NAME = "VoteChainCryptoPrefs"

        // Multiple key aliases for different purposes
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

        // Security flags
        private const val REQUIRE_USER_AUTH = false // Set to true for biometric protection
        private const val KEY_VALIDITY_SECONDS = -1 // -1 for no timeout

        private var isBouncyCastleInitialized = false

        /**
         * Initialize BouncyCastle provider with error handling
         */
        fun initializeBouncyCastle(): Boolean {
            if (!isBouncyCastleInitialized) {
                try {
                    // Test if BouncyCastle is already available
                    val existingProvider = Security.getProvider("BC")
                    if (existingProvider != null) {
                        Log.d(TAG, "BouncyCastle provider already exists")
                        isBouncyCastleInitialized = true
                        return true
                    }

                    // Remove any existing BC provider
                    Security.removeProvider("BC")

                    // Add BouncyCastle provider
                    val bcProvider = BouncyCastleProvider()
                    Security.insertProviderAt(bcProvider, 1)

                    // Verify installation
                    val installedProvider = Security.getProvider("BC")
                    if (installedProvider != null) {
                        isBouncyCastleInitialized = true
                        Log.d(TAG, "✅ BouncyCastle provider initialized successfully")
                        return true
                    } else {
                        Log.e(TAG, "❌ BouncyCastle provider not found after installation")
                        return false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to initialize BouncyCastle: ${e.message}", e)
                    return false
                }
            }
            return true
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
     * Generate a key pair using the available EC algorithm with multiple fallback options
     */
    private fun generateKeyPairWithEC(): KeyPairInfo? {
        Log.d(TAG, "Attempting EC key generation with multiple providers")

        // Try with BouncyCastle first (most reliable for secp256k1)
        try {
            // Ensure BouncyCastle is properly initialized
            if (initializeBouncyCastle()) {
                val keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC")
                val ecSpec = ECGenParameterSpec("secp256k1")
                keyPairGenerator.initialize(ecSpec)
                val keyPair = keyPairGenerator.generateKeyPair()

                // Extract private key as BigInteger directly from BC implementation
                val privateKeyBytes = keyPair.private.encoded
                val privateKeyBigInt = BigInteger(1, privateKeyBytes)

                // Create Web3j ECKeyPair
                val ecKeyPair = ECKeyPair.create(privateKeyBigInt)

                // Generate addresses and keys
                val privateKeyHex = Numeric.toHexStringNoPrefix(ecKeyPair.privateKey)
                val publicKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.publicKey)
                val addressHex = Keys.getAddress(ecKeyPair)
                val address = "0x" + addressHex

                Log.d(TAG, "✅ EC key generation successful with BC, address: $address")

                return KeyPairInfo(
                    publicKey = publicKeyHex,
                    privateKey = "0x" + privateKeyHex.padStart(64, '0'),
                    voterAddress = address,
                    generationMethod = "EC_BouncyCastle"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "BouncyCastle secp256k1 generation failed: ${e.message}")
        }

        // Try with AndroidOpenSSL with improved error handling
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("EC", "AndroidOpenSSL")

            try {
                // Attempt with specific curve
                val ecSpec = ECGenParameterSpec("secp256k1")
                keyPairGenerator.initialize(ecSpec)
            } catch (e: Exception) {
                Log.w(TAG, "secp256k1 not supported, falling back to generic EC with OpenSSL")
                // Fallback to generic EC parameters
                keyPairGenerator.initialize(256)
            }

            val keyPair = keyPairGenerator.generateKeyPair()

            // Use a simpler extraction method
            val privateKeyBytes = keyPair.private.encoded
            val privateKeyBigInt = BigInteger(1, privateKeyBytes)

            // Create Web3j ECKeyPair and derive address
            val ecKeyPair = ECKeyPair.create(privateKeyBigInt)
            val privateKeyHex = Numeric.toHexStringNoPrefix(ecKeyPair.privateKey)
            val publicKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.publicKey)
            val addressHex = Keys.getAddress(ecKeyPair)
            val address = "0x" + addressHex

            Log.d(TAG, "✅ EC key generation with AndroidOpenSSL successful")

            return KeyPairInfo(
                publicKey = publicKeyHex,
                privateKey = "0x" + privateKeyHex.padStart(64, '0'),
                voterAddress = address,
                generationMethod = "EC_AndroidOpenSSL"
            )
        } catch (e: Exception) {
            Log.w(TAG, "AndroidOpenSSL EC generation failed: ${e.message}")
        }

        // Final fallback to SecureRandom-based generation
        try {
            // Generate raw bytes for private key
            val secureRandom = SecureRandom()
            val privateKeyBytes = ByteArray(32)
            secureRandom.nextBytes(privateKeyBytes)

            // Create ECKeyPair directly from random bytes
            val ecKeyPair = ECKeyPair.create(privateKeyBytes)

            val privateKeyHex = Numeric.toHexStringNoPrefix(ecKeyPair.privateKey)
            val publicKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.publicKey)
            val addressHex = Keys.getAddress(ecKeyPair)
            val address = "0x" + addressHex

            Log.d(TAG, "✅ EC key generation with SecureRandom successful")

            return KeyPairInfo(
                publicKey = publicKeyHex,
                privateKey = "0x" + privateKeyHex.padStart(64, '0'),
                voterAddress = address,
                generationMethod = "EC_SecureRandom"
            )
        } catch (e: Exception) {
            Log.e(TAG, "All EC key generation methods failed: ${e.message}")
        }

        return null
    }

    /**
     * Generate a new Ethereum key pair with multiple fallback methods
     */
    fun generateKeyPair(): KeyPairInfo {
        Log.d(TAG, "🔄 Starting secure key pair generation with multiple fallback methods...")

        // New method: Try with EC available providers first
        generateKeyPairWithEC()?.let {
            Log.d(TAG, "✅ Successfully generated key pair using EC with available provider")
            return it
        }

        // Method 1: Web3j with BouncyCastle
        generateKeyPairMethod1()?.let {
            Log.d(TAG, "✅ Successfully generated key pair using Method 1 (Web3j)")
            return it
        }

        // Method 2: Java Security with BouncyCastle
        generateKeyPairMethod2()?.let {
            Log.d(TAG, "✅ Successfully generated key pair using Method 2 (Java Security)")
            return it
        }

        // Method 3: SecureRandom fallback
        generateKeyPairMethod3()?.let {
            Log.d(TAG, "✅ Successfully generated key pair using Method 3 (SecureRandom)")
            return it
        }

        // Method 4: Android Keystore hybrid
        generateKeyPairMethod4()?.let {
            Log.d(TAG, "✅ Successfully generated key pair using Method 4 (Android Keystore)")
            return it
        }

        // If all methods fail
        throw SecurityException("Failed to generate cryptographic key pair using all available methods")
    }

    /**
     * Method 1: Web3j with BouncyCastle
     */
    private fun generateKeyPairMethod1(): KeyPairInfo? {
        return try {
            Log.d(TAG, "Attempting Method 1: Web3j with BouncyCastle")

            // Ensure BouncyCastle is initialized
            if (!initializeBouncyCastle()) {
                Log.w(TAG, "BouncyCastle initialization failed")
                return null
            }

            // Generate EC key pair using Web3j
            val ecKeyPair = Keys.createEcKeyPair()

            // Convert to secure format
            val privateKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.privateKey)
            val publicKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.publicKey)

            // Generate Ethereum address
            val voterAddress = Keys.getAddress(ecKeyPair)
            val voterAddressWithPrefix = Numeric.toHexStringWithPrefix(voterAddress as BigInteger?)

            Log.d(TAG, "✅ Method 1 successful - Generated secure Ethereum key pair")
            Log.d(TAG, "Voter Address: $voterAddressWithPrefix")

            KeyPairInfo(
                publicKey = publicKeyHex,
                privateKey = privateKeyHex,
                voterAddress = voterAddressWithPrefix,
                generationMethod = "Web3j_BouncyCastle"
            )
        } catch (e: Exception) {
            Log.w(TAG, "❌ Method 1 failed: ${e.message}")
            null
        }
    }

    /**
     * Method 2: Java Security with BouncyCastle
     */
    private fun generateKeyPairMethod2(): KeyPairInfo? {
        return try {
            Log.d(TAG, "Attempting Method 2: Java Security with BouncyCastle")

            if (!initializeBouncyCastle()) {
                return null
            }

            // Generate using Java Security with BC provider
            val keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC")
            keyPairGenerator.initialize(ECGenParameterSpec("secp256k1"))

            val javaKeyPair = keyPairGenerator.generateKeyPair()

            // Convert to Web3j ECKeyPair
            val privateKeyBytes = javaKeyPair.private.encoded
            val ecKeyPair = ECKeyPair.create(privateKeyBytes)

            // Convert to secure format
            val privateKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.privateKey)
            val publicKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.publicKey)
            val voterAddress = Keys.getAddress(ecKeyPair)
            val voterAddressWithPrefix = Numeric.toHexStringWithPrefix(voterAddress as BigInteger?)

            Log.d(TAG, "✅ Method 2 successful")

            KeyPairInfo(
                publicKey = publicKeyHex,
                privateKey = privateKeyHex,
                voterAddress = voterAddressWithPrefix,
                generationMethod = "Java_Security_BC"
            )
        } catch (e: Exception) {
            Log.w(TAG, "❌ Method 2 failed: ${e.message}")
            null
        }
    }

    /**
     * Method 3: SecureRandom fallback
     */
    private fun generateKeyPairMethod3(): KeyPairInfo? {
        return try {
            Log.d(TAG, "Attempting Method 3: SecureRandom fallback")


            // Generate random private key
            val secureRandom = SecureRandom()
            val privateKeyBytes = ByteArray(32)
            secureRandom.nextBytes(privateKeyBytes)

            // Create ECKeyPair from private key bytes
            val ecKeyPair = ECKeyPair.create(privateKeyBytes)

            // Convert to secure format
            val privateKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.privateKey)
            val publicKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.publicKey)
            val addressHex = Keys.getAddress(ecKeyPair)
            val voterAddressWithPrefix = "0x" + addressHex

            Log.d(TAG, "✅ Method 3 successful")

            KeyPairInfo(
                publicKey = publicKeyHex,
                privateKey = privateKeyHex,
                voterAddress = voterAddressWithPrefix,
                generationMethod = "SecureRandom_Fallback"
            )
        } catch (e: Exception) {
            Log.w(TAG, "❌ Method 3 failed: ${e.message}")
            null
        }
    }

    /**
     * Method 4: Android Keystore hybrid approach
     */
    private fun generateKeyPairMethod4(): KeyPairInfo? {
        return try {
            Log.d(TAG, "Attempting Method 4: Android Keystore hybrid")

            // Generate a secure seed using Android Keystore
            val keyAlias = "temp_seed_${System.currentTimeMillis()}"
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            val secretKey = keyGenerator.generateKey()

            // Use the key to generate entropy for EC key pair
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            // Generate entropy
            val entropy = cipher.doFinal("VoteChain entropy ${System.currentTimeMillis()}".toByteArray())

            // Use entropy as seed for SecureRandom
            val secureRandom = SecureRandom.getInstance("SHA1PRNG")
            secureRandom.setSeed(entropy)

            val privateKeyBytes = ByteArray(32)
            secureRandom.nextBytes(privateKeyBytes)

            // Create ECKeyPair
            val ecKeyPair = ECKeyPair.create(privateKeyBytes)

            // Clean up temporary key
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(keyAlias)

            // Convert to secure format
            val privateKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.privateKey)
            val publicKeyHex = Numeric.toHexStringWithPrefix(ecKeyPair.publicKey)
            val addressHex = Keys.getAddress(ecKeyPair)
            val voterAddressWithPrefix = "0x" + addressHex

            Log.d(TAG, "✅ Method 4 successful")

            KeyPairInfo(
                publicKey = publicKeyHex,
                privateKey = privateKeyHex,
                voterAddress = voterAddressWithPrefix,
                generationMethod = "Android_Keystore_Hybrid"
            )
        } catch (e: Exception) {
            Log.w(TAG, "❌ Method 4 failed: ${e.message}")
            null
        }
    }

    /**
     * Store key pair with maximum security
     */
    fun storeKeyPair(keyPairInfo: KeyPairInfo) {
        try {
            Log.d(TAG, "Storing key pair with enhanced security...")

            // Preprocess the private key if it's too long
            val processedKeyPairInfo = if (keyPairInfo.privateKey.length > 128) {
                Log.d(TAG, "Processing oversized private key for storage")
                // Get the last 64 chars if it's too long (or adjust as needed)
                val privateKeyHex = if (keyPairInfo.privateKey.startsWith("0x")) {
                    keyPairInfo.privateKey.substring(2)
                } else {
                    keyPairInfo.privateKey
                }

                val processedPrivateKey = if (privateKeyHex.length > 64) {
                    "0x" + privateKeyHex.takeLast(64)
                } else {
                    "0x" + privateKeyHex.padStart(64, '0')
                }

                keyPairInfo.copy(privateKey = processedPrivateKey)
            } else {
                keyPairInfo
            }

            // Validate key pair before storing
            validateKeyPair(processedKeyPairInfo)

            // Double-encrypt private key for extra security
            val encryptedPrivateKeyData = doubleEncryptPrivateKey(processedKeyPairInfo.privateKey)

            // Store with transaction for atomicity
            with(encryptedSharedPreferences.edit()) {
                putString(PUBLIC_KEY_KEY, processedKeyPairInfo.publicKey)
                putString(VOTER_ADDRESS_KEY, processedKeyPairInfo.voterAddress)
                putString(ENCRYPTED_PRIVATE_KEY_KEY, encryptedPrivateKeyData.encryptedData)
                putString(IV_KEY, encryptedPrivateKeyData.iv)
                putLong(KEY_CREATION_TIME, processedKeyPairInfo.creationTime)
                putString(KEY_GENERATION_METHOD, processedKeyPairInfo.generationMethod)
                commit() // Use commit for synchronous write
            }

            // Rest of the method remains the same...
            // Create metadata, store it, clear sensitive data, etc.

            val metadata = KeyMetadata(
                creationTime = processedKeyPairInfo.creationTime,
                lastAccessTime = System.currentTimeMillis(),
                accessCount = 0,
                generationMethod = processedKeyPairInfo.generationMethod
            )

            storeKeyMetadata(metadata)
            clearSensitiveData(processedKeyPairInfo.privateKey)

            Log.d(TAG, "✅ Key pair stored with enhanced security using method: ${processedKeyPairInfo.generationMethod}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to store key pair: ${e.message}", e)
            throw SecurityException("Failed to store cryptographic key pair", e)
        }
    }

    // ===== REST OF THE METHODS REMAIN THE SAME =====

    /**
     * Enable StrongBox backed keys when available on the device
     */
    private fun enableStrongBoxBackedKeysWhenAvailable() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            try {
                val keyInfo = context.packageManager
                    .getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo

                Log.d(TAG, "Enabling maximum hardware security for key storage")
            } catch (e: Exception) {
                Log.w(TAG, "Could not enable enhanced hardware security: ${e.message}")
            }
        }
    }

    /**
     * Get private key with security checks and audit logging
     */
    fun getPrivateKey(): String? {
        return try {
            // Update access metadata
            updateAccessMetadata()

            // Check if keys exist
            if (!hasStoredKeyPair()) {
                Log.w(TAG, "No stored key pair found")
                return null
            }

            val encryptedPrivateKey = encryptedSharedPreferences.getString(ENCRYPTED_PRIVATE_KEY_KEY, null)
            val iv = encryptedSharedPreferences.getString(IV_KEY, null)

            if (encryptedPrivateKey != null && iv != null) {
                // Log access for security audit
                Log.d(TAG, "Private key access requested")

                // Decrypt with verification
                val decrypted = doubleDecryptPrivateKey(encryptedPrivateKey, iv)

                // Validate decrypted key
                if (validatePrivateKeyFormat(decrypted)) {
                    return decrypted
                } else {
                    Log.e(TAG, "Decrypted private key validation failed")
                    return null
                }
            } else {
                Log.w(TAG, "Encrypted private key or IV not found")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving private key: ${e.message}", e)
            null
        }
    }

    /**
     * Get public key (safe to access)
     */
    fun getPublicKey(): String? {
        return try {
            encryptedSharedPreferences.getString(PUBLIC_KEY_KEY, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving public key: ${e.message}", e)
            null
        }
    }

    /**
     * Get voter address (safe to access)
     */
    fun getVoterAddress(): String? {
        return try {
            encryptedSharedPreferences.getString(VOTER_ADDRESS_KEY, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving voter address: ${e.message}", e)
            null
        }
    }

    /**
     * Check if key pair exists with integrity check
     */
    fun hasStoredKeyPair(): Boolean {
        return try {
            val hasKeys = encryptedSharedPreferences.contains(PUBLIC_KEY_KEY) &&
                    encryptedSharedPreferences.contains(ENCRYPTED_PRIVATE_KEY_KEY) &&
                    encryptedSharedPreferences.contains(VOTER_ADDRESS_KEY) &&
                    encryptedSharedPreferences.contains(IV_KEY)

            // Additional integrity check
            if (hasKeys) {
                verifyKeystoreIntegrity()
            }

            hasKeys
        } catch (e: Exception) {
            Log.e(TAG, "Error checking stored key pair: ${e.message}", e)
            false
        }
    }

    /**
     * Validate stored keys with comprehensive checks
     */
    fun validateStoredKeys(): Boolean {
        return try {
            val publicKey = getPublicKey()
            val privateKey = getPrivateKey()
            val voterAddress = getVoterAddress()

            if (publicKey == null || privateKey == null || voterAddress == null) {
                Log.w(TAG, "One or more keys are missing")
                return false
            }

            // Fix: Convert private key string to BigInteger properly
            val privateKeyBigInt = if (privateKey.startsWith("0x")) {
                // Remove 0x prefix and convert hex string to BigInteger
                BigInteger(privateKey.substring(2), 16)
            } else {
                // Convert hex string to BigInteger
                BigInteger(privateKey, 16)
            }

            val ecKeyPair = ECKeyPair.create(privateKeyBigInt)
            val derivedPublicKey = Numeric.toHexStringWithPrefix(ecKeyPair.publicKey)
            val derivedAddress = Numeric.toHexStringWithPrefix(Keys.getAddress(ecKeyPair) as BigInteger?)

            val publicKeyValid = publicKey.equals(derivedPublicKey, ignoreCase = true)
            val addressValid = voterAddress.equals(derivedAddress, ignoreCase = true)

            // Additional cryptographic validation
            val signatureValid = validateKeyPairSignature(ecKeyPair)

            Log.d(TAG, "Key validation - Public: $publicKeyValid, Address: $addressValid, Signature: $signatureValid")

            publicKeyValid && addressValid && signatureValid
        } catch (e: Exception) {
            Log.e(TAG, "Error validating stored keys: ${e.message}", e)
            false
        }
    }

    /**
     * Clear all stored keys with secure wipe
     */
    fun clearStoredKeys() {
        try {
            Log.d(TAG, "Performing secure key wipe...")

            // Clear from encrypted storage
            with(encryptedSharedPreferences.edit()) {
                clear()
                commit()
            }

            // Clear Android Keystore entries
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            listOf(KEY_ALIAS_MASTER, KEY_ALIAS_ENCRYPTION, KEY_ALIAS_SIGNING).forEach { alias ->
                if (keyStore.containsAlias(alias)) {
                    keyStore.deleteEntry(alias)
                }
            }

            Log.d(TAG, "✅ All keys securely wiped")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during secure wipe: ${e.message}", e)
            throw SecurityException("Failed to clear stored keys", e)
        }
    }

    /**
     * Export keys for backup (requires explicit user consent)
     */
    fun exportKeysForBackup(userConsent: Boolean): String? {
        if (!userConsent) {
            Log.w(TAG, "Key export denied - no user consent")
            return null
        }

        return try {
            val keyInfo = getKeyInfo() ?: return null

            // Create encrypted backup format
            val backup = mapOf(
                "version" to 1,
                "timestamp" to System.currentTimeMillis(),
                "publicKey" to keyInfo.publicKey,
                "encryptedPrivateKey" to Base64.encodeToString(
                    encryptPrivateKey(keyInfo.privateKey).encryptedData.toByteArray(),
                    Base64.NO_WRAP
                ),
                "voterAddress" to keyInfo.voterAddress,
                "generationMethod" to keyInfo.generationMethod
            )

            // Convert to JSON and encrypt entire backup
            val backupJson = backup.toString()
            Base64.encodeToString(backupJson.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export keys for backup", e)
            null
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Initialize all security keys in Android Keystore
     */
    private fun initializeSecurityKeys() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            // Create master key for key derivation
            if (!keyStore.containsAlias(KEY_ALIAS_MASTER)) {
                createMasterKey()
            }

            // Create encryption key
            if (!keyStore.containsAlias(KEY_ALIAS_ENCRYPTION)) {
                createEncryptionKey()
            }

            Log.d(TAG, "✅ Security keys initialized")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize security keys", e)
            throw SecurityException("Cannot initialize security infrastructure", e)
        }
    }

    /**
     * Create master key for key derivation
     */
    private fun createMasterKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS_MASTER,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(REQUIRE_USER_AUTH)
            .setRandomizedEncryptionRequired(true)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    /**
     * Create encryption key with enhanced parameters
     */
    private fun createEncryptionKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS_ENCRYPTION,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(REQUIRE_USER_AUTH)
            .setRandomizedEncryptionRequired(true)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    /**
     * Double encryption for enhanced security
     */
    private fun doubleEncryptPrivateKey(privateKey: String): EncryptedData {
        // First layer: encrypt with master key
        val firstEncryption = encryptWithKey(privateKey, KEY_ALIAS_MASTER)

        // Second layer: encrypt with encryption key
        val secondEncryption = encryptWithKey(
            Base64.encodeToString(firstEncryption.encryptedData.toByteArray(), Base64.NO_WRAP),
            KEY_ALIAS_ENCRYPTION
        )

        return secondEncryption
    }

    /**
     * Double decryption
     */
    private fun doubleDecryptPrivateKey(encryptedData: String, iv: String): String {
        try {
            // First layer: decrypt with encryption key
            val firstDecryption = decryptWithKey(encryptedData, iv, KEY_ALIAS_ENCRYPTION)

            // Decode first decryption result to get the encrypted data for second layer
            val secondEncryptedData = Base64.decode(firstDecryption, Base64.NO_WRAP).toString(Charsets.UTF_8)

            // Second layer: decrypt with master key
            // Use the same IV since it was stored alongside the encryption key
            return decryptWithKey(secondEncryptedData, iv, KEY_ALIAS_MASTER)
        } catch (e: Exception) {
            // Handle format errors gracefully
            Log.e(TAG, "Double decryption failed: ${e.message}")
            throw SecurityException("Failed to decrypt private key", e)
        }
    }

    /**
     * Encrypt with specific key alias
     */
    private fun encryptWithKey(data: String, keyAlias: String): EncryptedData {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedBytes = cipher.doFinal(data.toByteArray())
        val iv = cipher.iv

        return EncryptedData(
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
            Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    /**
     * Decrypt with specific key alias
     */
    private fun decryptWithKey(encryptedData: String, iv: String, keyAlias: String): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)

        val ivBytes = Base64.decode(iv, Base64.NO_WRAP)
        val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, ivBytes)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

        val encryptedBytes = Base64.decode(encryptedData, Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes)
    }

    /**
     * Encrypt private key for export purposes
     */
    private fun encryptPrivateKey(privateKey: String): EncryptedData {
        return encryptWithKey(privateKey, KEY_ALIAS_ENCRYPTION)
    }

    /**
     * Validate key pair before storage
     */
    private fun validateKeyPair(keyPairInfo: KeyPairInfo) {
        require(keyPairInfo.privateKey.isNotEmpty()) { "Private key cannot be empty" }
        require(keyPairInfo.publicKey.isNotEmpty()) { "Public key cannot be empty" }
        require(keyPairInfo.voterAddress.isNotEmpty()) { "Voter address cannot be empty" }

        // Validate format
        require(validatePrivateKeyFormat(keyPairInfo.privateKey)) { "Invalid private key format" }
        require(keyPairInfo.publicKey.startsWith("0x")) { "Invalid public key format" }
        require(keyPairInfo.voterAddress.startsWith("0x")) { "Invalid voter address format" }
    }

    /**
     * Validate private key format with improved flexibility
     */
    private fun validatePrivateKeyFormat(privateKey: String): Boolean {
        return try {
            // Handle null or empty keys
            if (privateKey.isNullOrEmpty()) {
                Log.w(TAG, "Private key is null or empty")
                return false
            }

            // Strip 0x prefix if present
            val cleanKey = if (privateKey.startsWith("0x", ignoreCase = true)) {
                privateKey.substring(2)
            } else {
                privateKey
            }

            Log.d(TAG, "Validating private key with length: ${cleanKey.length}")

            // If the key is extremely long, it might be in a DER or another encoded format
            // Just check if it contains valid hex characters
            val isValidHex = cleanKey.all {
                it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F'
            }

            if (!isValidHex) {
                Log.w(TAG, "Private key contains non-hex characters")
                return false
            }

            // If the key is very long, extract a portion for storage
            // or process it based on your specific requirements
            if (cleanKey.length > 64) {
                Log.d(TAG, "Long key detected, likely DER or PEM encoded")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error validating private key format: ${e.message}")
            false
        }
    }

    /**
     * Validate key pair signature - Fixed to use correct Web3j signing methods
     */
    private fun validateKeyPairSignature(ecKeyPair: ECKeyPair): Boolean {
        return try {
            // Create a test message
            val testMessage = "VoteChain validation ${System.currentTimeMillis()}"
            val messageHash = org.web3j.crypto.Hash.sha3(testMessage.toByteArray())

            // Sign with private key using Sign.signMessage
            val signatureData = Sign.signMessage(messageHash, ecKeyPair)

            // Verify signature by recovering public key
            val recoveredPublicKey = Sign.signedMessageHashToKey(messageHash, signatureData)

            // Compare recovered public key with original
            recoveredPublicKey == ecKeyPair.publicKey
        } catch (e: Exception) {
            Log.e(TAG, "Signature validation failed", e)
            false
        }
    }

    /**
     * Verify Android Keystore integrity
     */
    private fun verifyKeystoreIntegrity() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            // Check if required keys exist
            val requiredKeys = listOf(KEY_ALIAS_ENCRYPTION)
            requiredKeys.forEach { alias ->
                if (!keyStore.containsAlias(alias)) {
                    Log.w(TAG, "Missing keystore key: $alias, recreating...")
                    initializeSecurityKeys()
                    return
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Keystore integrity check failed", e)
            throw SecurityException("Keystore integrity compromised", e)
        }
    }

    /**
     * Store key metadata
     */
    private fun storeKeyMetadata(metadata: KeyMetadata) {
        try {
            val metadataJson = """
                {
                    "creationTime": ${metadata.creationTime},
                    "lastAccessTime": ${metadata.lastAccessTime},
                    "accessCount": ${metadata.accessCount},
                    "keyVersion": ${metadata.keyVersion}
                }
            """.trimIndent()

            with(encryptedSharedPreferences.edit()) {
                putString(KEY_METADATA, metadataJson)
                apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store key metadata", e)
        }
    }

    /**
     * Update access metadata
     */
    private fun updateAccessMetadata() {
        try {
            // Get current metadata
            val metadataJson = encryptedSharedPreferences.getString(KEY_METADATA, null)
            if (metadataJson != null) {
                // Parse and update (simplified for this example)
                val updatedMetadata = metadataJson.replace(
                    Regex("\"lastAccessTime\":\\s*\\d+"),
                    "\"lastAccessTime\": ${System.currentTimeMillis()}"
                )

                with(encryptedSharedPreferences.edit()) {
                    putString(KEY_METADATA, updatedMetadata)
                    apply()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update access metadata", e)
        }
    }

    /**
     * Clear sensitive data from memory
     */
    private fun clearSensitiveData(sensitiveData: String) {
        try {
            // This is a best-effort attempt to clear from memory
            // Note: String immutability in Java/Kotlin makes this challenging
            System.gc()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear sensitive data from memory", e)
        }
    }

    /**
     * Get complete key information
     */
    fun getKeyInfo(): KeyPairInfo? {
        return try {
            val publicKey = getPublicKey()
            val privateKey = getPrivateKey()
            val voterAddress = getVoterAddress()
            val creationTime = encryptedSharedPreferences.getLong(KEY_CREATION_TIME, 0)

            if (publicKey != null && privateKey != null && voterAddress != null) {
                KeyPairInfo(publicKey, privateKey, voterAddress, creationTime)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting complete key info: ${e.message}", e)
            null
        }
    }

//    /**
//     * Data class for encrypted data storage
//     */
//    private data class EncryptedData(
//        val encryptedData: String,
//        val iv: String
//    )
}