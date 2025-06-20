package com.nocturna.votechain.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.nocturna.votechain.blockchain.BlockchainManager
import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.UserRegistrationData
import com.nocturna.votechain.security.CryptoKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enhanced UserRepository yang mengintegrasikan:
 * 1. Secure local key generation (CryptoKeyManager)
 * 2. Blockchain integration (BlockchainManager)
 * 3. Server registration (UserRepository)
 */
class IntegratedEnhancedUserRepository(private val context: Context) {
    private val userRepository = UserRepository(context)
    private val cryptoKeyManager = CryptoKeyManager(context)
    private val voterRepository = VoterRepository(context)
    private val TAG = "IntegratedEnhancedUserRepository"

    /**
     * Register user dengan secure key generation + optional blockchain integration
     */
    suspend fun registerWithFullIntegration(
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
        ktpFileUri: Uri? = null
    ): Result<RegistrationResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting full integration registration for: $email")

            // Step 1: Generate secure key pair locally (independent dari blockchain)
            val keyPairInfo = cryptoKeyManager.generateKeyPair()
            Log.d(TAG, "✅ Generated secure key pair locally")
            Log.d(TAG, "   Voter Address: ${keyPairInfo.voterAddress}")

            // Step 2: Store keys securely di Android Keystore
            cryptoKeyManager.storeKeyPair(keyPairInfo)
            Log.d(TAG, "✅ Keys stored securely in Android Keystore")

            // Step 3: Optional blockchain integration (non-blocking)
            val blockchainResult = tryBlockchainIntegration(keyPairInfo.voterAddress)

            // Step 4: Register user di server dengan generated voter address
            val registrationResult = userRepository.registerUser(
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
                voterAddress = keyPairInfo.voterAddress,
                ktpFileUri = ktpFileUri
            )

            registrationResult.fold(
                onSuccess = { response ->
                    // Step 5: Store voter data locally
                    storeVoterKeysLocally(nik, fullName, keyPairInfo)

                    val result = RegistrationResult(
                        serverResponse = response,
                        keyPairInfo = keyPairInfo,
                        blockchainIntegration = blockchainResult,
                        isSuccess = true
                    )

                    Log.d(TAG, "✅ Registration completed successfully")
                    Result.success(result)
                },
                onFailure = { exception ->
                    Log.e(TAG, "❌ Server registration failed: ${exception.message}")

                    // Cleanup generated keys jika server registration gagal
                    cryptoKeyManager.clearStoredKeys()

                    Result.failure(exception)
                }
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error during registration", e)

            // Cleanup keys on any error
            try {
                cryptoKeyManager.clearStoredKeys()
            } catch (cleanupException: Exception) {
                Log.e(TAG, "Failed to cleanup keys", cleanupException)
            }

            Result.failure(e)
        }
    }

    /**
     * Optional blockchain integration - tidak akan block registration jika gagal
     */
    private suspend fun tryBlockchainIntegration(voterAddress: String): BlockchainIntegrationResult {
        return try {
            Log.d(TAG, "🔗 Attempting blockchain integration...")

            // Check blockchain connection
            val isConnected = withContext(Dispatchers.IO) {
                BlockchainManager.isConnected()
            }

            if (isConnected) {
                Log.d(TAG, "✅ Blockchain connected")

                // Try to fund the address
                val fundingResult = tryFundingAddress(voterAddress)

                // Try to register address on blockchain (jika ada method tersebut)
                val registrationTxHash = tryRegisterOnBlockchain(voterAddress)

                BlockchainIntegrationResult(
                    isConnected = true,
                    fundingTxHash = fundingResult,
                    registrationTxHash = registrationTxHash,
                    isSuccess = fundingResult.isNotEmpty() || registrationTxHash.isNotEmpty()
                )
            } else {
                Log.w(TAG, "⚠️ Blockchain not connected, skipping integration")
                BlockchainIntegrationResult(
                    isConnected = false,
                    fundingTxHash = "",
                    registrationTxHash = "",
                    isSuccess = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Blockchain integration failed (non-critical): ${e.message}")
            BlockchainIntegrationResult(
                isConnected = false,
                fundingTxHash = "",
                registrationTxHash = "",
                isSuccess = false,
                error = e.message
            )
        }
    }

    /**
     * Try to fund the voter address dengan ETH untuk transaction fees
     */
    private suspend fun tryFundingAddress(voterAddress: String): String {
        return try {
            val txHash = withContext(Dispatchers.IO) {
                BlockchainManager.fundVoterAddress(voterAddress)
            }

            if (txHash.isNotEmpty()) {
                Log.d(TAG, "✅ Address funded successfully: $txHash")
            } else {
                Log.w(TAG, "⚠️ Address funding returned empty hash")
            }

            txHash
        } catch (e: Exception) {
            Log.e(TAG, "❌ Address funding failed: ${e.message}")
            ""
        }
    }

    /**
     * Try to register address on blockchain smart contract (optional)
     */
    private suspend fun tryRegisterOnBlockchain(voterAddress: String): String {
        return try {
            // Implementasi spesifik untuk registrasi di smart contract
            // Ini tergantung pada smart contract yang digunakan

            Log.d(TAG, "🔗 Registering address on blockchain: $voterAddress")

            // Placeholder - implementasi actual tergantung smart contract
            // val txHash = BlockchainManager.registerVoterOnContract(voterAddress)
            val txHash = "" // Untuk sementara kosong

            if (txHash.isNotEmpty()) {
                Log.d(TAG, "✅ Address registered on blockchain: $txHash")
            }

            txHash
        } catch (e: Exception) {
            Log.e(TAG, "❌ Blockchain registration failed: ${e.message}")
            ""
        }
    }

    /**
     * Store voter keys locally untuk quick access
     */
    private fun storeVoterKeysLocally(
        nik: String,
        fullName: String,
        keyPairInfo: CryptoKeyManager.KeyPairInfo
    ) {
        try {
            val sharedPreferences = context.getSharedPreferences("VoteChainPrefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("voter_nik", nik)
                putString("voter_full_name", fullName)
                putString("voter_public_key", keyPairInfo.publicKey)
                putString("voter_address", keyPairInfo.voterAddress)
                putBoolean("voter_has_voted", false)
                // Note: Private key TIDAK disimpan di sini, hanya di secure Android Keystore
                apply()
            }
            Log.d(TAG, "✅ Voter data stored locally (private key remains in secure keystore)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to store voter data locally", e)
        }
    }

    /**
     * Get registration result dengan semua informasi
     */
    fun getRegistrationSummary(): RegistrationSummary? {
        return try {
            if (!cryptoKeyManager.hasStoredKeyPair()) {
                return null
            }

            RegistrationSummary(
                voterAddress = cryptoKeyManager.getVoterAddress() ?: "",
                publicKey = cryptoKeyManager.getPublicKey() ?: "",
                hasPrivateKey = cryptoKeyManager.getPrivateKey() != null,
                isKeysValid = voterRepository.validateStoredData()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting registration summary", e)
            null
        }
    }

    /**
     * Check blockchain connection status
     */
    suspend fun checkBlockchainConnection(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                BlockchainManager.isConnected()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking blockchain connection", e)
            false
        }
    }

    /**
     * Manual blockchain integration untuk user yang sudah terdaftar
     */
    suspend fun retryBlockchainIntegration(): Result<BlockchainIntegrationResult> {
        return try {
            val voterAddress = cryptoKeyManager.getVoterAddress()
            if (voterAddress.isNullOrEmpty()) {
                return Result.failure(Exception("No voter address found"))
            }

            val result = tryBlockchainIntegration(voterAddress)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== Data Classes untuk Result =====

    /**
     * Comprehensive registration result
     */
    data class RegistrationResult(
        val serverResponse: ApiResponse<UserRegistrationData>,
        val keyPairInfo: CryptoKeyManager.KeyPairInfo,
        val blockchainIntegration: BlockchainIntegrationResult,
        val isSuccess: Boolean
    )

    /**
     * Blockchain integration result
     */
    data class BlockchainIntegrationResult(
        val isConnected: Boolean,
        val fundingTxHash: String,
        val registrationTxHash: String,
        val isSuccess: Boolean,
        val error: String? = null
    )

    /**
     * Registration summary untuk UI
     */
    data class RegistrationSummary(
        val voterAddress: String,
        val publicKey: String,
        val hasPrivateKey: Boolean,
        val isKeysValid: Boolean
    )

    // ===== Delegation Methods =====

    /**
     * Delegate methods ke CryptoKeyManager dan VoterRepository
     */
    fun getPrivateKey(): String? = cryptoKeyManager.getPrivateKey()
    fun getPublicKey(): String? = cryptoKeyManager.getPublicKey()
    fun getVoterAddress(): String? = cryptoKeyManager.getVoterAddress()
    fun hasStoredKeys(): Boolean = cryptoKeyManager.hasStoredKeyPair()
    fun validateStoredKeys(): Boolean = voterRepository.validateStoredData()

    fun getWalletInfo(): com.nocturna.votechain.data.model.WalletInfo = voterRepository.getWalletInfo()
    fun getWalletInfoWithPrivateKey(): com.nocturna.votechain.data.model.WalletInfo = voterRepository.getWalletInfoWithPrivateKey()

    fun clearAllKeys() {
        cryptoKeyManager.clearStoredKeys()
        voterRepository.clearVoterData()
    }
}