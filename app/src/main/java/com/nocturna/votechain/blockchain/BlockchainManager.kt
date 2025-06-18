package com.nocturna.votechain.blockchain

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.security.SecureRandom

/**
 * Singleton to manage Web3j connections and blockchain operations
 */
object BlockchainManager {
    private const val TAG = "BlockchainManager"

    private val web3j: Web3j by lazy {
        val nodeUrl = "https://d4bb-103-233-100-204.ngrok-free.app"
        Log.d(TAG, "Initializing Web3j connection to $nodeUrl")
        Web3j.build(HttpService(nodeUrl))
    }

    /**
     * Check if Web3j is connected to the Ethereum node
     * @return true if connected, false otherwise
     */
    suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            val clientVersion = web3j.web3ClientVersion().send()
            Log.d(TAG, "Node client version: ${clientVersion.web3ClientVersion}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to Ethereum node: ${e.message}", e)
            false
        }
    }

    /**
     * Generate a new Ethereum address
     * @return The generated address with 0x prefix
     */
    fun generateAddress(): String {
        try {
            // Generate random private key
            val privateKeyBytes = ByteArray(32)
            SecureRandom().nextBytes(privateKeyBytes)

            // Create ECKeyPair from private key
            val privateKey = Numeric.toBigInt(privateKeyBytes)
            val keyPair = ECKeyPair.create(privateKey)

            // Get Ethereum address from key pair
            val address = "0x" + Keys.getAddress(keyPair)
            Log.d(TAG, "Generated new Ethereum address: $address")
            return address
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Ethereum address: ${e.message}", e)
            // Return a placeholder in case of error
            return "0x0000000000000000000000000000000000000000"
        }
    }

    /**
     * Get account balance from the blockchain
     * @param address Ethereum address to check
     * @return Balance in ETH as a string with 8 decimal places
     */
    suspend fun getAccountBalance(address: String): String = withContext(Dispatchers.IO) {
        try {
            val balanceWei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
            val balanceEth = Convert.fromWei(balanceWei.toString(), Convert.Unit.ETHER)

            // Format to 8 decimal places
            return@withContext String.format("%.8f", balanceEth.toDouble())
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching balance for $address: ${e.message}", e)
            return@withContext "0.00000000"
        }
    }

    /**
     * Fund a newly created voter address with a small amount of ETH
     * Note: This requires an account with funds on the local node
     * @param voterAddress Address to fund
     * @return Transaction hash if successful, empty string if failed
     */
    suspend fun fundVoterAddress(voterAddress: String): String = withContext(Dispatchers.IO) {
        try {
            // This would normally use the admin/funder private key
            // For demo purposes, we'll use the first account from Ganache
            val funderCredentials = Credentials.create("0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d")

            // Send 0.01 ETH to the new voter
            val gasPrice = web3j.ethGasPrice().send().gasPrice
            val gasLimit = BigInteger.valueOf(21000)
            val value = Convert.toWei("0.01", Convert.Unit.ETHER).toBigInteger()

            val transaction = web3j.ethSendTransaction(
                org.web3j.protocol.core.methods.request.Transaction.createEtherTransaction(
                    funderCredentials.address,
                    null,
                    gasPrice,
                    gasLimit,
                    voterAddress,
                    value
                )
            ).send()

            if (transaction.hasError()) {
                Log.e(TAG, "Error funding address: ${transaction.error.message}")
                return@withContext ""
            }

            val txHash = transaction.transactionHash
            Log.d(TAG, "Funded $voterAddress with 0.01 ETH, tx hash: $txHash")
            return@withContext txHash
        } catch (e: Exception) {
            Log.e(TAG, "Exception funding address: ${e.message}", e)
            return@withContext ""
        }
    }
}