package com.nocturna.votechain.data.model

data class UserWalletInfo(
    val address: String,
    val encryptedPrivateKey: String,
    val name: String = "Wallet",
    val mnemonic: String? = null,
    val isDefault: Boolean = false
)

// Keep existing for backward compatibility
data class VoterWalletInfo(
    val balance: String,
    val privateKey: String,
    val publicKey: String
)