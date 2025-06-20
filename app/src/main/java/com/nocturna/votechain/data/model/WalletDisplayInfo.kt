package com.nocturna.votechain.data.model

data class WalletDisplayInfo(
    val address: String,
    val name: String,
    val balance: String = "Loading...",
    val isDefault: Boolean = false
)