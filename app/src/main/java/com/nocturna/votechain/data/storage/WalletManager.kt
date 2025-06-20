package com.nocturna.votechain.data.storage

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nocturna.votechain.data.model.WalletData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.*
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

data class WalletInfo(
    val address: String,
    val encryptedPrivateKey: String,
    val name: String = "Wallet",
    val mnemonic: String? = null,
    val isDefault: Boolean = false
)

data class WalletData(
    val address: String,
    val privateKey: String,
    val publicKey: String,
    val balance: BigInteger,
    val name: String,
    val mnemonic: String? = null
)

/**
 * Singleton class to manage wallet operations including creation, import, and storage
 */
class WalletManager private constructor(private val context: Context) {
    private val TAG = "WalletManager"

    private val PREFS_NAME = "VoteChainWallets"
    private val KEY_WALLETS = "stored_wallets"
    private val KEY_SELECTED_WALLET = "selected_wallet_address"
    private val KEY_DEFAULT_WALLET = "default_wallet_address"

    // Encrypted SharedPreferences for secure storage
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()

    companion object {
        @Volatile
        private var INSTANCE: WalletManager? = null

        fun getInstance(context: Context): WalletManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WalletManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Create a new wallet with PIN protection
     */
    suspend fun createNewWallet(pin: String, walletName: String): WalletData = withContext(Dispatchers.IO) {
        try {
            // Generate new key pair
            val keyPair = Keys.createEcKeyPair()
            val privateKeyHex = Numeric.toHexStringWithPrefix(keyPair.privateKey)
            val publicKeyHex = Numeric.toHexStringWithPrefix(keyPair.publicKey)
            val address = "0x" + Keys.getAddress(keyPair)

            // Generate mnemonic
            val mnemonic = generateMnemonic()

            // Create wallet data
            val walletData = WalletData(
                address = address,
                privateKey = privateKeyHex,
                publicKey = publicKeyHex,
                balance = BigInteger.ZERO,
                name = walletName,
                mnemonic = mnemonic
            )

            // Save wallet with PIN encryption
            saveWallet(walletData, pin)

            // Set as default if it's the first wallet
            if (!hasWallets()) {
                setDefaultWallet(address)
            }

            setSelectedWallet(address)

            Log.d(TAG, "New wallet created: $address")
            walletData
        } catch (e: Exception) {
            Log.e(TAG, "Error creating wallet", e)
            throw e
        }
    }

    /**
     * Import wallet from private key
     */
    suspend fun importWallet(privateKey: String, pin: String, walletName: String): WalletData = withContext(Dispatchers.IO) {
        try {
            val cleanPrivateKey = if (privateKey.startsWith("0x")) {
                privateKey.substring(2)
            } else {
                privateKey
            }

            val keyPair = ECKeyPair.create(Numeric.toBigInt(cleanPrivateKey))
            val publicKeyHex = Numeric.toHexStringWithPrefix(keyPair.publicKey)
            val address = "0x" + Keys.getAddress(keyPair)

            val walletData = WalletData(
                address = address,
                privateKey = Numeric.toHexStringWithPrefix(keyPair.privateKey),
                publicKey = publicKeyHex,
                balance = BigInteger.ZERO,
                name = walletName,
                mnemonic = null // Imported wallets don't have mnemonic
            )

            saveWallet(walletData, pin)
            setSelectedWallet(address)

            Log.d(TAG, "Wallet imported: $address")
            walletData
        } catch (e: Exception) {
            Log.e(TAG, "Error importing wallet", e)
            throw e
        }
    }

    /**
     * Import wallet from mnemonic
     */
    suspend fun importWalletFromMnemonic(mnemonic: String, pin: String, walletName: String): WalletData = withContext(Dispatchers.IO) {
        try {
            val credentials = WalletUtils.loadBip39Credentials("", mnemonic)
            val keyPair = credentials.ecKeyPair
            val address = credentials.address

            val walletData = WalletData(
                address = address,
                privateKey = Numeric.toHexStringWithPrefix(keyPair.privateKey),
                publicKey = Numeric.toHexStringWithPrefix(keyPair.publicKey),
                balance = BigInteger.ZERO,
                name = walletName,
                mnemonic = mnemonic
            )

            saveWallet(walletData, pin)
            setSelectedWallet(address)

            Log.d(TAG, "Wallet restored from mnemonic: $address")
            walletData
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring wallet from mnemonic", e)
            throw e
        }
    }

    /**
     * Load wallet with PIN
     */
    suspend fun loadWallet(address: String, pin: String): WalletData? = withContext(Dispatchers.IO) {
        try {
            val wallets = getAllWallets()
            val walletInfo = wallets.find { it.address.equals(address, ignoreCase = true) }
                ?: return@withContext null

            val decryptedPrivateKey = decryptData(walletInfo.encryptedPrivateKey, pin)
            val balance = getWalletBalance(address)

            WalletData(
                address = walletInfo.address,
                privateKey = decryptedPrivateKey,
                publicKey = walletInfo.address, // Using address as public key for simplicity
                balance = balance,
                name = walletInfo.name,
                mnemonic = walletInfo.mnemonic
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading wallet", e)
            null
        }
    }

    /**
     * Get selected wallet with PIN
     */
    suspend fun loadSelectedWallet(pin: String): WalletData? {
        val selectedAddress = getSelectedWalletAddress() ?: getDefaultWalletAddress()
        return selectedAddress?.let { loadWallet(it, pin) }
    }

    /**
     * Get all wallet information (without private keys)
     */
    fun getAllWallets(): List<WalletInfo> {
        try {
            val walletsJson = encryptedPrefs.getString(KEY_WALLETS, "[]") ?: "[]"
            val type = object : TypeToken<List<WalletInfo>>() {}.type
            return gson.fromJson(walletsJson, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all wallets", e)
            return emptyList()
        }
    }

    /**
     * Save wallet securely
     */
    private fun saveWallet(walletData: WalletData, pin: String) {
        try {
            val wallets = getAllWallets().toMutableList()

            // Remove existing wallet with same address
            wallets.removeAll { it.address.equals(walletData.address, ignoreCase = true) }

            // Encrypt private key with PIN
            val encryptedPrivateKey = encryptData(walletData.privateKey, pin)

            // Add new wallet info
            val walletInfo = WalletInfo(
                address = walletData.address,
                encryptedPrivateKey = encryptedPrivateKey,
                name = walletData.name,
                mnemonic = walletData.mnemonic,
                isDefault = wallets.isEmpty() // First wallet is default
            )

            wallets.add(walletInfo)

            // Save to encrypted preferences
            val walletsJson = gson.toJson(wallets)
            encryptedPrefs.edit().putString(KEY_WALLETS, walletsJson).apply()

            Log.d(TAG, "Wallet saved: ${walletData.address}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving wallet", e)
            throw e
        }
    }

    /**
     * Check if any wallets exist
     */
    fun hasWallets(): Boolean {
        return getAllWallets().isNotEmpty()
    }

    /**
     * Get wallet balance (mock implementation)
     */
    suspend fun getWalletBalance(address: String): BigInteger = withContext(Dispatchers.IO) {
        try {
            // TODO: Implement actual blockchain balance checking
            // For now, return a mock balance
            BigInteger.ZERO
        } catch (e: Exception) {
            Log.e(TAG, "Error getting balance for $address", e)
            BigInteger.ZERO
        }
    }

    /**
     * Set selected wallet
     */
    fun setSelectedWallet(address: String) {
        encryptedPrefs.edit().putString(KEY_SELECTED_WALLET, address).apply()
    }

    /**
     * Get selected wallet address
     */
    fun getSelectedWalletAddress(): String? {
        return encryptedPrefs.getString(KEY_SELECTED_WALLET, null)
    }

    /**
     * Set default wallet
     */
    fun setDefaultWallet(address: String) {
        encryptedPrefs.edit().putString(KEY_DEFAULT_WALLET, address).apply()
    }

    /**
     * Get default wallet address
     */
    fun getDefaultWalletAddress(): String? {
        return encryptedPrefs.getString(KEY_DEFAULT_WALLET, null)
    }

    /**
     * Update wallet name
     */
    fun updateWalletName(address: String, newName: String): Boolean {
        try {
            val wallets = getAllWallets().toMutableList()
            val walletIndex = wallets.indexOfFirst { it.address.equals(address, ignoreCase = true) }

            if (walletIndex != -1) {
                wallets[walletIndex] = wallets[walletIndex].copy(name = newName)
                val walletsJson = gson.toJson(wallets)
                encryptedPrefs.edit().putString(KEY_WALLETS, walletsJson).apply()
                return true
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error updating wallet name", e)
            return false
        }
    }

    /**
     * Delete wallet
     */
    fun deleteWallet(address: String): Boolean {
        try {
            val wallets = getAllWallets().toMutableList()
            val removed = wallets.removeAll { it.address.equals(address, ignoreCase = true) }

            if (removed) {
                val walletsJson = gson.toJson(wallets)
                encryptedPrefs.edit().putString(KEY_WALLETS, walletsJson).apply()

                // Clear selection if deleted wallet was selected
                if (getSelectedWalletAddress().equals(address, ignoreCase = true)) {
                    encryptedPrefs.edit().remove(KEY_SELECTED_WALLET).apply()
                }

                return true
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting wallet", e)
            return false
        }
    }

    /**
     * Encrypt data with PIN
     */
    private fun encryptData(data: String, pin: String): String {
        try {
            val key = generateKeyFromPin(pin)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting data", e)
            throw e
        }
    }

    /**
     * Decrypt data with PIN
     */
    private fun decryptData(encryptedData: String, pin: String): String {
        try {
            val key = generateKeyFromPin(pin)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
            return String(decryptedBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting data", e)
            throw e
        }
    }

    /**
     * Generate AES key from PIN
     */
    private fun generateKeyFromPin(pin: String): SecretKeySpec {
        val keyBytes = ByteArray(16) // 128-bit key
        val pinBytes = pin.toByteArray()

        // Simple key derivation - in production, use a proper KDF like PBKDF2
        for (i in keyBytes.indices) {
            keyBytes[i] = if (i < pinBytes.size) pinBytes[i] else 0
        }

        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Generate a simple mnemonic (12 words)
     */
    private fun generateMnemonic(): String {
        // Simple word list for demo - in production, use BIP39 standard
        val words = listOf(
            "abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract",
            "absurd", "abuse", "access", "accident", "account", "accuse", "achieve", "acid"
        )

        val random = SecureRandom()
        return (1..12).map { words[random.nextInt(words.size)] }.joinToString(" ")
    }
}