package com.nocturna.votechain.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.UserRegistrationData
import com.nocturna.votechain.data.model.WalletData
import com.nocturna.votechain.data.storage.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.security.SecureRandom

/**
 * Enhanced UserRepository yang mendukung generasi voter address dengan pembuatan wallet lengkap
 */
class EnhancedUserRepository(private val context: Context) {
    private val userRepository = UserRepository(context)
    private val walletManager = WalletManager.getInstance(context)
    private val TAG = "EnhancedUserRepository"

    // SharedPreferences untuk menyimpan mapping registrasi-wallet
    private val PREFS_NAME = "VoteChainRegistration"
    private val KEY_REGISTRATION_WALLET_PREFIX = "reg_wallet_"
    private val KEY_USER_EMAIL_PREFIX = "user_email_"

    // Encrypted SharedPreferences untuk penyimpanan aman
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Register user baru dengan file KTP dan wallet yang otomatis dibuat
     */
    suspend fun registerWithVoterAddress(
        email: String,
        password: String,
        nik: String,
        fullName: String,
        gender: String,
        birthPlace: String,
        birthDate: String,
        residentialAddress: String,
        region: String,
        role: String = "voter",
        ktpFileUri: Uri?
    ): Result<ApiResponse<UserRegistrationData>> {
        return try {
            Log.d(TAG, "Starting registration with wallet generation for: $email")

            // Step 1: Buat wallet terlebih dahulu (sebelum panggilan API)
            val walletData = createRegistrationWallet(email, nik, fullName)
            val voterAddress = walletData.address

            Log.d(TAG, "Generated voter address: $voterAddress")

            // Step 2: Simpan mapping wallet-registrasi
            storeRegistrationWalletMapping(email, nik, voterAddress, walletData)

            // Step 3: Panggil repository asli dengan address yang dibuat
            val result = userRepository.registerUser(
                email = email,
                password = password,
                nik = nik,
                fullName = fullName,
                gender = gender,
                birthPlace = birthPlace,
                birthDate = birthDate,
                residentialAddress = residentialAddress,
                region = region,
                role = role,
                voterAddress = voterAddress,
                ktpFileUri = ktpFileUri
            )

            // Step 4: Tangani hasil registrasi
            result.fold(
                onSuccess = { response ->
                    Log.d(TAG, "Registration API successful, wallet created and linked")

                    // Tambahkan informasi wallet ke response jika diperlukan
                    val updatedResponse = response.copy(
                        data = response.data?.copy(
                            id = voterAddress // Gunakan wallet address sebagai ID
                        )
                    )

                    // Tandai wallet sebagai terverifikasi setelah registrasi berhasil
                    markWalletAsVerified(voterAddress)

                    Result.success(updatedResponse)
                },
                onFailure = { exception ->
                    Log.e(TAG, "Registration failed, cleaning up wallet", exception)

                    // Bersihkan wallet jika registrasi gagal
                    cleanupFailedRegistration(email, voterAddress)

                    Result.failure(exception)
                }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Registration with wallet creation failed", e)
            Result.failure(e)
        }
    }

    /**
     * Buat wallet untuk registrasi
     */
    private suspend fun createRegistrationWallet(
        email: String,
        nik: String,
        fullName: String
    ): WalletData = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creating wallet for registration")

            // Generate keypair
            val random = SecureRandom()
            val privateKeyBytes = ByteArray(32)
            random.nextBytes(privateKeyBytes)

            val privateKeyBigInt = Numeric.toBigInt(privateKeyBytes)
            val keyPair = ECKeyPair.create(privateKeyBigInt)

            // Generate address
            val address = "0x" + Keys.getAddress(keyPair)
            val privateKeyHex = Numeric.toHexStringNoPrefix(keyPair.privateKey)
            val publicKeyHex = Numeric.toHexStringNoPrefix(keyPair.publicKey)

            // Create wallet data
            val walletData = WalletData(
                address = address,
                privateKey = privateKeyHex,
                publicKey = publicKeyHex,
                balance = BigInteger.ZERO,
                name = "VoteChain Wallet - $fullName",
                mnemonic = null,
                isDefault = true,
                createdAt = System.currentTimeMillis()
            )

            // Simpan wallet menggunakan WalletManager
            walletManager.createNewWallet(
                pin = nik, // Use NIK as PIN instead of 'password'
                walletName = walletData.name
            )

            Log.d(TAG, "Wallet created successfully: $address")
            walletData

        } catch (e: Exception) {
            Log.e(TAG, "Error creating registration wallet", e)
            throw e
        }
    }

    /**
     * Simpan mapping registrasi-wallet
     */
    private fun storeRegistrationWalletMapping(
        email: String,
        nik: String,
        walletAddress: String,
        walletData: WalletData
    ) {
        try {
            encryptedPrefs.edit().apply {
                // Simpan mapping email ke wallet address
                putString("${KEY_REGISTRATION_WALLET_PREFIX}$email", walletAddress)

                // Simpan mapping NIK ke wallet address
                putString("${KEY_REGISTRATION_WALLET_PREFIX}$nik", walletAddress)

                // Simpan informasi user
                putString("${KEY_USER_EMAIL_PREFIX}$walletAddress", email)

                // Simpan timestamp registrasi
                putLong("registration_time_$walletAddress", System.currentTimeMillis())

                // Simpan status verifikasi (awalnya false)
                putBoolean("wallet_verified_$walletAddress", false)

                apply()
            }

            Log.d(TAG, "Registration wallet mapping stored successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error storing registration wallet mapping", e)
        }
    }

    /**
     * Tandai wallet sebagai terverifikasi
     */
    private fun markWalletAsVerified(walletAddress: String) {
        try {
            encryptedPrefs.edit().apply {
                putBoolean("wallet_verified_$walletAddress", true)
                putLong("verification_time_$walletAddress", System.currentTimeMillis())
                apply()
            }

            Log.d(TAG, "Wallet marked as verified: $walletAddress")

        } catch (e: Exception) {
            Log.e(TAG, "Error marking wallet as verified", e)
        }
    }

    /**
     * Bersihkan registrasi yang gagal
     */
    private suspend fun cleanupFailedRegistration(email: String, walletAddress: String) {
        try {
            Log.d(TAG, "Cleaning up failed registration for: $email")

            // Hapus wallet dari WalletManager
            walletManager.deleteWallet(walletAddress)

            // Hapus mapping dari SharedPreferences
            encryptedPrefs.edit().apply {
                remove("${KEY_REGISTRATION_WALLET_PREFIX}$email")
                remove("${KEY_USER_EMAIL_PREFIX}$walletAddress")
                remove("registration_time_$walletAddress")
                remove("wallet_verified_$walletAddress")
                remove("verification_time_$walletAddress")
                apply()
            }

            Log.d(TAG, "Cleanup completed for failed registration")

        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    /**
     * Dapatkan wallet address berdasarkan email
     */
    fun getWalletAddressByEmail(email: String): String? {
        return encryptedPrefs.getString("${KEY_REGISTRATION_WALLET_PREFIX}$email", null)
    }

    /**
     * Dapatkan wallet address berdasarkan NIK
     */
    fun getWalletAddressByNik(nik: String): String? {
        return encryptedPrefs.getString("${KEY_REGISTRATION_WALLET_PREFIX}$nik", null)
    }

    /**
     * Dapatkan email berdasarkan wallet address
     */
    fun getEmailByWalletAddress(walletAddress: String): String? {
        return encryptedPrefs.getString("${KEY_USER_EMAIL_PREFIX}$walletAddress", null)
    }

    /**
     * Cek apakah wallet sudah terverifikasi
     */
    fun isWalletVerified(walletAddress: String): Boolean {
        return encryptedPrefs.getBoolean("wallet_verified_$walletAddress", false)
    }

    /**
     * Dapatkan semua wallet yang terdaftar
     */
    fun getAllRegisteredWallets(): List<String> {
        val wallets = mutableListOf<String>()
        val allPrefs = encryptedPrefs.all

        for ((key, value) in allPrefs) {
            if (key.startsWith(KEY_REGISTRATION_WALLET_PREFIX) && key.contains("@")) {
                // Key yang berisi email (bukan NIK)
                wallets.add(value as String)
            }
        }

        return wallets.distinct()
    }

    /**
     * Dapatkan informasi registrasi lengkap berdasarkan wallet address
     */
    fun getRegistrationInfo(walletAddress: String): RegistrationInfo? {
        val email = getEmailByWalletAddress(walletAddress)
        val registrationTime = encryptedPrefs.getLong("registration_time_$walletAddress", 0L)
        val isVerified = isWalletVerified(walletAddress)
        val verificationTime = encryptedPrefs.getLong("verification_time_$walletAddress", 0L)

        return if (email != null) {
            RegistrationInfo(
                email = email,
                walletAddress = walletAddress,
                registrationTime = registrationTime,
                isVerified = isVerified,
                verificationTime = if (verificationTime > 0) verificationTime else null
            )
        } else null
    }

    /**
     * Verifikasi email dan aktifkan wallet
     */
    suspend fun verifyEmailAndActivateWallet(
        email: String,
        verificationToken: String
    ): Result<ApiResponse<Any>> {
        return try {
            Log.d(TAG, "Verifying email and activating wallet for: $email")

            // Panggil API verifikasi email
            val result = userRepository.verifyEmail(verificationToken)

            result.fold(
                onSuccess = { response ->
                    if (response.code == 0) {
                        // Jika verifikasi berhasil, aktifkan wallet
                        val walletAddress = getWalletAddressByEmail(email)
                        walletAddress?.let { address ->
                            markWalletAsVerified(address)
                            Log.d(TAG, "Email verified and wallet activated: $address")
                        }
                    }
                    Result.success(response)
                },
                onFailure = { exception ->
                    Log.e(TAG, "Email verification failed", exception)
                    Result.failure(exception)
                }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error during email verification", e)
            Result.failure(e)
        }
    }

    /**
     * Generate mnemonic untuk wallet (opsional)
     */
    private fun generateMnemonic(): String {
        val words = listOf(
            "abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract",
            "absurd", "abuse", "access", "accident", "account", "accuse", "achieve", "acid",
            "acoustic", "acquire", "across", "act", "action", "actor", "actress", "actual",
            // Tambahkan lebih banyak kata sesuai standar BIP39
        )

        val random = SecureRandom()
        val selectedWords = mutableListOf<String>()

        repeat(12) {
            selectedWords.add(words[random.nextInt(words.size)])
        }

        return selectedWords.joinToString(" ")
    }

    /**
     * Ekspor wallet data untuk backup
     */
    suspend fun exportWalletForBackup(walletAddress: String, pin: String): WalletBackupData? {
        return try {
            val registrationInfo = getRegistrationInfo(walletAddress)
            val walletData = walletManager.loadWallet(walletAddress, pin)

            if (registrationInfo != null && walletData != null) {
                WalletBackupData(
                    address = walletAddress,
                    email = registrationInfo.email,
                    isVerified = registrationInfo.isVerified,
                    createdAt = registrationInfo.registrationTime,
                    walletName = walletData.name
                )
            } else {
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error exporting wallet backup data", e)
            null
        }
    }

    /**
     * Hapus semua data registrasi (untuk keperluan logout/reset)
     */
    fun clearAllRegistrationData() {
        try {
            encryptedPrefs.edit().clear().apply()
            Log.d(TAG, "All registration data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing registration data", e)
        }
    }
}

/**
 * Data class untuk informasi registrasi
 */
data class RegistrationInfo(
    val email: String,
    val walletAddress: String,
    val registrationTime: Long,
    val isVerified: Boolean,
    val verificationTime: Long? = null
)

/**
 * Data class untuk backup wallet
 */
data class WalletBackupData(
    val address: String,
    val email: String,
    val isVerified: Boolean,
    val createdAt: Long,
    val walletName: String
)