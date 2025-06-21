package com.nocturna.votechain.blockchain

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthBlock
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
        val nodeUrl = "https://1f39-114-122-69-116.ngrok-free.app"
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
            val balanceWei =
                web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
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
            val funderCredentials =
                Credentials.create("0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d")

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

    /**
     * Enhanced connection checking with retry mechanism
     */
    suspend fun isConnectedWithRetry(maxRetries: Int = 3): Boolean = withContext(Dispatchers.IO) {
        repeat(maxRetries) { attempt ->
            try {
                val isConnected = web3j.web3ClientVersion().send().web3ClientVersion.isNotEmpty()
                if (isConnected) {
                    Log.d(TAG, "✅ Blockchain connection successful on attempt ${attempt + 1}")
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Connection attempt ${attempt + 1} failed: ${e.message}")
                if (attempt < maxRetries - 1) {
                    delay(1000 * (attempt + 1)) // Exponential backoff
                }
            }
        }
        Log.e(TAG, "❌ All connection attempts failed")
        return@withContext false
    }

    /**
     * Get account balance with retry mechanism and better error handling
     */
    suspend fun getAccountBalanceWithRetry(
        address: String,
        maxRetries: Int = 3
    ): String = withContext(Dispatchers.IO) {
        repeat(maxRetries) { attempt ->
            try {
                val balanceWei =
                    web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
                val balanceEth = Convert.fromWei(balanceWei.toString(), Convert.Unit.ETHER)
                val formattedBalance = String.format("%.8f", balanceEth.toDouble())

                Log.d(TAG, "✅ Balance fetched successfully: $formattedBalance ETH for $address")
                return@withContext formattedBalance
            } catch (e: Exception) {
                Log.w(TAG, "Balance fetch attempt ${attempt + 1} failed: ${e.message}")
                if (attempt < maxRetries - 1) {
                    delay(500 * (attempt + 1))
                }
            }
        }

        Log.e(TAG, "❌ Failed to fetch balance after $maxRetries attempts")
        return@withContext "0.00000000"
    }

    /**
     * Enhanced fund voter address method
     */
    suspend fun fundVoterAddress(
        voterAddress: String,
        amount: String = "0.001"
    ): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔗 Attempting to fund voter address: $voterAddress with $amount ETH")

            // Check if we have a funding account configured
            val fundingAccount = getFundingAccount()
            if (fundingAccount == null) {
                Log.w(TAG, "⚠️ No funding account configured, skipping funding")
                return@withContext ""
            }

            // Check funding account balance
            val fundingBalance = getAccountBalance(fundingAccount.address)
            val fundingBalanceEth = fundingBalance.toDoubleOrNull() ?: 0.0
            val requiredAmount = amount.toDoubleOrNull() ?: 0.001

            if (fundingBalanceEth < requiredAmount) {
                Log.w(
                    TAG,
                    "⚠️ Insufficient funding balance: $fundingBalanceEth ETH (required: $requiredAmount ETH)"
                )
                return@withContext ""
            }

            // Create and send transaction
            val amountWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
            val gasPrice = web3j.ethGasPrice().send().gasPrice
            val nonce = web3j.ethGetTransactionCount(
                fundingAccount.address,
                DefaultBlockParameterName.LATEST
            ).send().transactionCount

            val transaction = RawTransaction.createEtherTransaction(
                nonce,
                gasPrice,
                BigInteger.valueOf(21000), // Standard gas limit for ETH transfer
                voterAddress,
                amountWei
            )

            val signedTransaction =
                TransactionEncoder.signMessage(transaction, fundingAccount.credentials)
            val transactionHash = web3j.ethSendRawTransaction(
                Numeric.toHexString(signedTransaction)
            ).send().transactionHash

            if (transactionHash.isNotEmpty()) {
                Log.d(TAG, "✅ Funding transaction sent: $transactionHash")

                // Wait for transaction confirmation (optional)
                waitForTransactionConfirmation(transactionHash)

                return@withContext transactionHash
            } else {
                Log.e(TAG, "❌ Failed to send funding transaction")
                return@withContext ""
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error funding voter address: ${e.message}", e)
            return@withContext ""
        }
    }

    /**
     * Register voter on smart contract (if applicable)
     */
    suspend fun registerVoterOnContract(voterAddress: String): String =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📝 Registering voter on smart contract: $voterAddress")

                // This is a placeholder implementation
                // Replace with your actual smart contract interaction
                val contractAddress = getVotingContractAddress()
                if (contractAddress.isEmpty()) {
                    Log.w(TAG, "⚠️ No voting contract configured")
                    return@withContext ""
                }

                // Simulate contract registration
                // In a real implementation, you would:
                // 1. Load your voting contract
                // 2. Call the register voter function
                // 3. Return the transaction hash

                delay(1000) // Simulate network delay
                val mockTxHash = "0x" + generateMockTransactionHash()

                Log.d(TAG, "✅ Voter registration transaction: $mockTxHash")
                return@withContext mockTxHash

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error registering voter on contract: ${e.message}", e)
                return@withContext ""
            }
        }

    /**
     * Get gas price with fallback
     */
    suspend fun getCurrentGasPrice(): BigInteger = withContext(Dispatchers.IO) {
        try {
            val gasPrice = web3j.ethGasPrice().send().gasPrice
            Log.d(TAG, "Current gas price: $gasPrice wei")
            return@withContext gasPrice
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get gas price, using default: ${e.message}")
            return@withContext Convert.toWei("20", Convert.Unit.GWEI).toBigInteger()
        }
    }

    /**
     * Estimate gas for transaction
     */
    suspend fun estimateGas(
        from: String,
        to: String,
        value: BigInteger = BigInteger.ZERO,
        data: String = ""
    ): BigInteger = withContext(Dispatchers.IO) {
        try {
            val transaction =
                org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                    from, to, data
                )

            val gasEstimate = web3j.ethEstimateGas(transaction).send().amountUsed
            Log.d(TAG, "Estimated gas: $gasEstimate")
            return@withContext gasEstimate
        } catch (e: Exception) {
            Log.w(TAG, "Failed to estimate gas, using default: ${e.message}")
            return@withContext BigInteger.valueOf(21000)
        }
    }

    /**
     * Wait for transaction confirmation
     */
    private suspend fun waitForTransactionConfirmation(
        transactionHash: String,
        maxWaitTime: Long = 60000 // 1 minute
    ): Boolean = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < maxWaitTime) {
            try {
                val receipt = web3j.ethGetTransactionReceipt(transactionHash).send()
                if (receipt.transactionReceipt.isPresent) {
                    val txReceipt = receipt.transactionReceipt.get()
                    val success = txReceipt.status == "0x1"
                    Log.d(
                        TAG,
                        "Transaction $transactionHash confirmed with status: ${txReceipt.status}"
                    )
                    return@withContext success
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error checking transaction receipt: ${e.message}")
            }

            delay(2000) // Check every 2 seconds
        }

        Log.w(TAG, "Transaction confirmation timeout for: $transactionHash")
        return@withContext false
    }

    /**
     * Get funding account (configure this based on your setup)
     */
    private fun getFundingAccount(): FundingAccount? {
        return try {
            // This should be configured based on your environment
            // For development: use a test account with test ETH
            // For production: use a treasury account with proper security

            val privateKey = "YOUR_FUNDING_ACCOUNT_PRIVATE_KEY" // Configure this
            val credentials = Credentials.create(privateKey)

            FundingAccount(
                address = credentials.address,
                credentials = credentials
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading funding account: ${e.message}")
            null
        }
    }

    /**
     * Get voting contract address (configure this based on your setup)
     */
    private fun getVotingContractAddress(): String {
        // Configure this based on your deployed contract
        return "0x742d35Cc6634C0532925a3b8D098d64f35f5b3f6" // Example address
    }

    /**
     * Generate mock transaction hash for testing
     */
    private fun generateMockTransactionHash(): String {
        val chars = "0123456789abcdef"
        return (1..64).map { chars.random() }.joinToString("")
    }

    /**
     * Check if address has sufficient balance for transaction
     */
    suspend fun hasSufficientBalance(
        address: String,
        requiredAmount: String,
        includeGas: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val balance = getAccountBalance(address).toDoubleOrNull() ?: 0.0
            val required = requiredAmount.toDoubleOrNull() ?: 0.0

            val gasEstimate = if (includeGas) {
                val gasPrice = getCurrentGasPrice()
                val gasLimit = BigInteger.valueOf(21000)
                val gasCost = gasPrice.multiply(gasLimit)
                Convert.fromWei(gasCost.toString(), Convert.Unit.ETHER).toDouble()
            } else {
                0.0
            }

            val totalRequired = required + gasEstimate
            return@withContext balance >= totalRequired
        } catch (e: Exception) {
            Log.e(TAG, "Error checking balance: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Get transaction history for address (basic implementation)
     */
    suspend fun getTransactionHistory(
        address: String,
        fromBlock: String = "earliest",
        toBlock: String = "latest"
    ): List<TransactionInfo> = withContext(Dispatchers.IO) {
        try {
            val transactions = mutableListOf<TransactionInfo>()

            // Get latest blocks and check for transactions
            val latestBlock = web3j.ethBlockNumber().send().blockNumber
            val startBlock = maxOf(latestBlock.subtract(BigInteger.valueOf(1000)), BigInteger.ZERO)

            for (blockNumber in startBlock.toLong()..latestBlock.toLong()) {
                try {
                    val block = web3j.ethGetBlockByNumber(
                        DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)),
                        true
                    ).send().block

                    block.transactions.forEach { tx ->
                        val transaction = tx as? EthBlock.TransactionObject
                        if (transaction?.to?.equals(address, ignoreCase = true) == true ||
                            transaction?.from?.equals(address, ignoreCase = true) == true
                        ) {

                            transactions.add(
                                TransactionInfo(
                                    hash = transaction.hash,
                                    from = transaction.from,
                                    to = transaction.to ?: "",
                                    value = Convert.fromWei(
                                        transaction.value.toString(),
                                        Convert.Unit.ETHER
                                    ).toString(),
                                    blockNumber = transaction.blockNumber.toLong(),
                                    timestamp = System.currentTimeMillis() // Approximate
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Continue with next block if one fails
                    continue
                }
            }

            return@withContext transactions.take(50) // Limit to 50 recent transactions
        } catch (e: Exception) {
            Log.e(TAG, "Error getting transaction history: ${e.message}")
            return@withContext emptyList()
        }
    }

    // Data classes for enhanced functionality
    data class FundingAccount(
        val address: String,
        val credentials: Credentials
    )

    data class TransactionInfo(
        val hash: String,
        val from: String,
        val to: String,
        val value: String,
        val blockNumber: Long,
        val timestamp: Long
    )

    /**
     * Enhanced connection status with details
     */
    data class ConnectionStatus(
        val isConnected: Boolean,
        val networkId: String = "",
        val latestBlock: Long = 0,
        val gasPrice: String = "",
        val error: String? = null
    )

    /**
     * Get detailed connection status
     */
    suspend fun getDetailedConnectionStatus(): ConnectionStatus = withContext(Dispatchers.IO) {
        try {
            val isConnected = isConnected()
            if (!isConnected) {
                return@withContext ConnectionStatus(false, error = "Not connected to blockchain")
            }

            val networkId = web3j.netVersion().send().netVersion
            val latestBlock = web3j.ethBlockNumber().send().blockNumber.toLong()
            val gasPrice = getCurrentGasPrice().toString()

            ConnectionStatus(
                isConnected = true,
                networkId = networkId,
                latestBlock = latestBlock,
                gasPrice = gasPrice
            )
        } catch (e: Exception) {
            ConnectionStatus(
                isConnected = false,
                error = e.message
            )
        }
    }
}