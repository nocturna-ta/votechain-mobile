//package com.devissvtr.wallet
//
//import org.web3j.crypto.Bip32ECKeyPair
//import org.web3j.crypto.Keys
//import org.web3j.crypto.MnemonicUtils
//import org.web3j.utils.Numeric
//import java.security.SecureRandom
//
//object WalletUtils {
//    data class WalletData(val mnemonic: String, val privateKey: String, val address: String)
//
//    fun generateNewWallet(): WalletData {
//        val entropy = SecureRandom().generateSeed(16)
//        val mnemonic = MnemonicUtils.generateMnemonic(entropy)
//        val seed = MnemonicUtils.generateSeed(mnemonic, "")
//        val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
//        val path = intArrayOf(
//            44 or Bip32ECKeyPair.HARDENED_BIT,
//            60 or Bip32ECKeyPair.HARDENED_BIT,
//            0 or Bip32ECKeyPair.HARDENED_BIT,
//            0,
//            0
//        )
//        val keyPair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)
//        val privateKey = Numeric.toHexStringNoPrefix(keyPair.privateKey)
//        val address = "0x" + Keys.getAddress(keyPair.publicKey)
//        return WalletData(mnemonic, privateKey, address)
//    }
//}