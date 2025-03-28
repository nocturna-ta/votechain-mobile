package com.devissvtr.wallet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.devissvtr.wallet.databinding.ActivityPasswordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.utils.Numeric
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class PasswordActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityPasswordBinding
    private var password: String = ""
    private var count : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpClickListeners()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.number0, R.id.number1, R.id.number2, R.id.number3,
            R.id.number4, R.id.number5, R.id.number6, R.id.number7,
            R.id.number8, R.id.number9
                -> {
                handleNumberClick(view as TextView)
            }

            R.id.done -> {
                if (password.length == 4) {
                    handleWalletAccess(password)
                } else {
                   Toast.makeText(this, "Please enter 4 digits", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.delete -> {
                if (password.isNotEmpty()) {
                    password = password.substring(0, password.length - 1)
                    updatePasswordView(count, false)
                    count--
                }
            }
        }
    }

    private fun setUpClickListeners() {
        binding.number0.setOnClickListener(this)
        binding.number1.setOnClickListener(this)
        binding.number2.setOnClickListener(this)
        binding.number3.setOnClickListener(this)
        binding.number4.setOnClickListener(this)
        binding.number5.setOnClickListener(this)
        binding.number6.setOnClickListener(this)
        binding.number7.setOnClickListener(this)
        binding.number8.setOnClickListener(this)
        binding.number9.setOnClickListener(this)
        binding.done.setOnClickListener(this)
        binding.delete.setOnClickListener(this)
    }

    private fun handleNumberClick(view: TextView) {

        if(count < 4){
            password += view.text
            count++
            updatePasswordView(count, true)
        }
    }

    private fun updatePasswordView(position: Int, isEntered: Boolean) {
        val drawable =
            if (isEntered) R.drawable.password_entered else R.drawable.password_not_entered

        when (position) {
            1 -> binding.firstPassword.setImageResource(drawable)
            2 -> binding.secondPassword.setImageResource(drawable)
            3 -> binding.thirdPassword.setImageResource(drawable)
            4 -> binding.fourthPassword.setImageResource(drawable)
        }
    }

    private fun handleWalletAccess(pin: String){
        val prefs = getSharedPreferences("wallet_prefs", MODE_PRIVATE)
        val encryptedData = prefs.getString("encrypted_private_key", null)

        if(encryptedData != null){
                lifecycleScope.launch(Dispatchers.IO) {
                try{
                    val privateKey = decryptPrivateKey(encryptedData, pin)
                    withContext(Dispatchers.Main){
                        proceedToAccountActivity(privateKey)
                    }
                }catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@PasswordActivity, "Incorrect Pin", Toast.LENGTH_SHORT).show()
                        resetPassword()
                    }
                }

            }
        }else{
            lifecycleScope.launch(Dispatchers.IO){
                val privateKey = createNewWallet()
                val encrypted = encryptPrivateKey(privateKey, pin)
                prefs.edit().putString("encrypted_private_key", encrypted).apply()
                withContext(Dispatchers.Main){
                    proceedToAccountActivity(privateKey)
                }
            }
        }

    }

    private fun createNewWallet(): String {
        val entropy = ByteArray(16)
        SecureRandom().nextBytes(entropy)
        val mnemonic = MnemonicUtils.generateMnemonic(entropy)
        val seed = MnemonicUtils.generateSeed(mnemonic, "")
        val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
        val path = intArrayOf(
            44 or Bip32ECKeyPair.HARDENED_BIT,
            60 or Bip32ECKeyPair.HARDENED_BIT,
            0 or Bip32ECKeyPair.HARDENED_BIT,
            0,
            0
        )
        val keyPair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)
        return Numeric.toHexStringWithPrefix(keyPair.privateKey)
    }

    private fun encryptPrivateKey(privateKey: String, pin: String): String {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val key = generateKey(pin, salt)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(privateKey.toByteArray())
        val combined = salt + iv + encrypted
        return Numeric.toHexString(combined)
    }

    private fun decryptPrivateKey(encryptedDataHex: String, pin: String): String {
        val encryptedData = Numeric.hexStringToByteArray(encryptedDataHex)
        if (encryptedData.size < 32) {
            throw IllegalArgumentException("Encrypted data is too short")
        }
        val salt = encryptedData.copyOfRange(0, 16)
        val iv = encryptedData.copyOfRange(16, 32)
        val encrypted = encryptedData.copyOfRange(32, encryptedData.size)
        val key = generateKey(pin, salt)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted)
    }

    private fun generateKey(pin: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(pin.toCharArray(), salt, 10000, 256)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    private fun proceedToAccountActivity(privateKey: String) {
        val intent = Intent(this, AccountActivity::class.java)
        intent.putExtra("private_key", privateKey)
        startActivity(intent)
        finish()
    }

    private fun resetPassword() {
        password = ""
        count = 0
        uncheckPassword()
        binding.tvSubHeaderWallet.text = getString(R.string.subHeaderReset)
    }

    private fun uncheckPassword() {
        updatePasswordView(1, false)
        updatePasswordView(2, false)
        updatePasswordView(3, false)
        updatePasswordView(4, false)
    }
}