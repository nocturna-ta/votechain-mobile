package com.devissvtr.wallet

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

object CryptoUtils{
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val CHECK_STRING = ":OK" // For password verification

    fun encrypt(text: String, password: String): String{
        val keyBytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal((text + CHECK_STRING).toByteArray())
        val ivAndEncrypted = iv + encrypted
        return Base64.getEncoder().encodeToString(ivAndEncrypted)
    }

    fun decrypt(encrypted: String, password: String): String{
        val keyBytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val ivAndEncrypted = Base64.getDecoder().decode(encrypted)
        val iv = ivAndEncrypted.sliceArray(0..15)
        val encryptedBytes = ivAndEncrypted.sliceArray(16 until ivAndEncrypted.size)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        val decrypted = cipher.doFinal(encryptedBytes)
        return String(decrypted)
    }
    private fun isValidDecryption(decrypted: String): Boolean {
        return decrypted.endsWith(CHECK_STRING)
    }

    fun extractPrivateKey(decrypted: String): String {
        return if (isValidDecryption(decrypted)) {
            decrypted.substring(0, decrypted.length - CHECK_STRING.length)
        } else {
            throw IllegalArgumentException("Invalid decryption")
        }
    }
}