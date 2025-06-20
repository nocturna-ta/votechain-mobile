package com.nocturna.votechain.data.model

import java.math.BigInteger

data class WalletData(
    val address: String,
    val privateKey: String,
    val publicKey: String,
    val balance: BigInteger,
    val name: String,
    val mnemonic: String? = null
)