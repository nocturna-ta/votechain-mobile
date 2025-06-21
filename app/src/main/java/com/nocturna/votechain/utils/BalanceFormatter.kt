package com.nocturna.votechain.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

/**
 * Utility class for formatting ETH balance and other blockchain-related values
 */
object BalanceFormatter {

    /**
     * Format ETH balance with proper decimal places
     * @param balance Balance as string from blockchain
     * @param decimalPlaces Number of decimal places to show (default: 6)
     * @return Formatted balance string
     */
    fun formatETHBalance(balance: String, decimalPlaces: Int = 6): String {
        return try {
            val balanceDecimal = BigDecimal(balance)
            val formatter = DecimalFormat().apply {
                maximumFractionDigits = decimalPlaces
                minimumFractionDigits = 2
                roundingMode = RoundingMode.DOWN
                isGroupingUsed = false
            }
            formatter.format(balanceDecimal)
        } catch (e: Exception) {
            "0.000000"
        }
    }

    /**
     * Format ETH balance with currency symbol
     * @param balance Balance as string
     * @param showFullPrecision Whether to show full precision or abbreviated
     * @return Formatted balance with ETH symbol
     */
    fun formatETHBalanceWithSymbol(balance: String, showFullPrecision: Boolean = true): String {
        return if (showFullPrecision) {
            "${formatETHBalance(balance)} ETH"
        } else {
            "${formatETHBalance(balance, 4)} ETH"
        }
    }

    /**
     * Format balance for display in lists (abbreviated)
     * @param balance Balance as string
     * @return Abbreviated balance for list display
     */
    fun formatBalanceForList(balance: String): String {
        return try {
            val balanceDecimal = BigDecimal(balance)
            when {
                balanceDecimal >= BigDecimal("1000") -> {
                    val abbreviated = balanceDecimal.divide(BigDecimal("1000"), 2, RoundingMode.DOWN)
                    "${abbreviated}K ETH"
                }
                balanceDecimal >= BigDecimal("1") -> {
                    "${formatETHBalance(balance, 2)} ETH"
                }
                else -> {
                    "${formatETHBalance(balance, 4)} ETH"
                }
            }
        } catch (e: Exception) {
            "0.00 ETH"
        }
    }

    /**
     * Check if balance is considered "low" (less than 0.01 ETH)
     * @param balance Balance as string
     * @return True if balance is low
     */
    fun isLowBalance(balance: String): Boolean {
        return try {
            val balanceDecimal = BigDecimal(balance)
            balanceDecimal < BigDecimal("0.01")
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Check if balance is zero
     * @param balance Balance as string
     * @return True if balance is zero
     */
    fun isZeroBalance(balance: String): Boolean {
        return try {
            val balanceDecimal = BigDecimal(balance)
            balanceDecimal.compareTo(BigDecimal.ZERO) == 0
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Format transaction amount for display
     * @param amount Transaction amount as string
     * @param isIncoming Whether this is an incoming transaction
     * @return Formatted amount with +/- prefix
     */
    fun formatTransactionAmount(amount: String, isIncoming: Boolean = true): String {
        val formattedAmount = formatETHBalance(amount, 6)
        val prefix = if (isIncoming) "+" else "-"
        return "$prefix$formattedAmount ETH"
    }

    /**
     * Parse balance string to BigDecimal safely
     * @param balance Balance as string
     * @return BigDecimal or zero if parsing fails
     */
    fun parseBalance(balance: String): BigDecimal {
        return try {
            BigDecimal(balance)
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    /**
     * Format USD equivalent (if you have USD conversion)
     * @param ethBalance ETH balance as string
     * @param ethToUsdRate Current ETH to USD rate
     * @return Formatted USD equivalent
     */
    fun formatUSDEquivalent(ethBalance: String, ethToUsdRate: Double): String {
        return try {
            val balanceDecimal = BigDecimal(ethBalance)
            val usdValue = balanceDecimal.multiply(BigDecimal(ethToUsdRate))
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            formatter.format(usdValue)
        } catch (e: Exception) {
            "$0.00"
        }
    }

    /**
     * Truncate address for display (show first 6 and last 4 characters)
     * @param address Full ethereum address
     * @return Truncated address like 0x1234...abcd
     */
    fun truncateAddress(address: String): String {
        return if (address.length >= 10) {
            "${address.take(6)}...${address.takeLast(4)}"
        } else {
            address
        }
    }

    /**
     * Truncate private key for display (show first 8 and last 8 characters)
     * @param privateKey Full private key
     * @return Truncated private key
     */
    fun truncatePrivateKey(privateKey: String): String {
        return if (privateKey.length >= 16) {
            "${privateKey.take(8)}...${privateKey.takeLast(8)}"
        } else {
            privateKey
        }
    }

    /**
     * Format gas price for display
     * @param gasPrice Gas price in Gwei
     * @return Formatted gas price
     */
    fun formatGasPrice(gasPrice: String): String {
        return try {
            val gasPriceDecimal = BigDecimal(gasPrice)
            "${gasPriceDecimal.setScale(2, RoundingMode.HALF_UP)} Gwei"
        } catch (e: Exception) {
            "0.00 Gwei"
        }
    }

    /**
     * Format time ago for transactions
     * @param timestamp Transaction timestamp in milliseconds
     * @return Human readable time ago string
     */
    fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            diff < 2592000_000 -> "${diff / 86400_000}d ago"
            else -> {
                val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                formatter.format(java.util.Date(timestamp))
            }
        }
    }
}