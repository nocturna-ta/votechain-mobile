package com.nocturna.votechain.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.nocturna.votechain.data.network.ApiResponse
import com.nocturna.votechain.data.network.UserRegistrationData
import java.security.SecureRandom
import org.web3j.crypto.Keys
import org.web3j.crypto.ECKeyPair
import org.web3j.utils.Numeric

/**
 * Extended UserRepository that supports voter address generation
 */
class EnhancedUserRepository(private val context: Context) {
    private val userRepository = UserRepository(context)
    private val TAG = "EnhancedUserRepository"

    /**
     * Generate an Ethereum address for voter
     * @return The generated Ethereum address with 0x prefix
     */
    fun generateEthereumAddress(): String {
        try {
            // Generate random private key
            val privateKeyBytes = ByteArray(32)
            SecureRandom().nextBytes(privateKeyBytes)

            // Create ECKeyPair from private key
            val privateKey = Numeric.toBigInt(privateKeyBytes)
            val keyPair = ECKeyPair.create(privateKey)

            // Get Ethereum address from key pair
            return "0x" + Keys.getAddress(keyPair)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Ethereum address: ${e.message}", e)
            // Return a placeholder in case of error
            return "0x0000000000000000000000000000000000000000"
        }
    }

    /**
     * Register a new user with KTP file and automatically generated voter address
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
        ktpFileUri: Uri? = null
    ): Result<ApiResponse<UserRegistrationData>> {
        // Generate Ethereum address for voter_address
        val voterAddress = generateEthereumAddress()
        Log.d(TAG, "Using voter address: $voterAddress")

        // Call the original repository with the generated address
        return userRepository.registerUser(
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
            voterAddress = voterAddress, // Pass the generated address
            ktpFileUri = ktpFileUri
        )
    }
}