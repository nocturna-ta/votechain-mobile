package com.devissvtr.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.devissvtr.wallet.databinding.ActivityAccountBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigInteger
import java.util.Locale

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private lateinit var web3j: Web3j
    private var nik: String = "0987654321"
    private var privateKey: String = ""
    private var publicKey: String = ""
    private var balance: BigInteger = BigInteger.ZERO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        web3j = Web3j.build(HttpService("http://10.0.2.2:7545"))

        privateKey = intent.getStringExtra("private_key") ?: ""

        binding.tvFieldPrivateKey.text = "••••••••"

        var isPrivateKeyVisible = false
        binding.ivShowHidePrivateKey.setOnClickListener {
            if (isPrivateKeyVisible) {
                binding.tvFieldPrivateKey.text = "••••••••"
                binding.ivShowHidePrivateKey.setImageResource(R.drawable.ic_eye_off)
                isPrivateKeyVisible = false
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Displaying your private key is a security risk. Ensure no one else can see your screen.")
                    .setPositiveButton("Show") { _, _ ->
                        binding.tvFieldPrivateKey.text = privateKey
                        binding.ivShowHidePrivateKey.setImageResource(R.drawable.ic_eye_on)
                        isPrivateKeyVisible = true
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()
            }
        }

        if (privateKey.isEmpty()) {
            Toast.makeText(this, "No private key provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val credentials = Credentials.create(privateKey)
        publicKey = credentials.address


        fetchBalance()

        binding.tvFieldNik.text = nik
        binding.tvFieldPrivateKey.text = "********"
        binding.tvFieldPublicKey.text = publicKey

        binding.ivCopy.setOnClickListener {
            copyClipboard(publicKey)
        }

        binding.ivBack.setOnClickListener {
            navigatePreviousPage()
        }
    }

    private fun fetchBalance() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val balanceWei = web3j.ethGetBalance(publicKey, DefaultBlockParameterName.LATEST).send().balance
                balance = balanceWei
                val balanceEth = Convert.fromWei(balanceWei.toString(), Convert.Unit.ETHER).toString()
                withContext(Dispatchers.Main) {
                    binding.tvFieldBalance.text = String.format(Locale.getDefault(), "%.8f ETH", balanceEth.toDoubleOrNull() ?: 0.0)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AccountActivity, "Error fetching balance: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun copyClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Public Key", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Public Key copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun navigatePreviousPage() {
        val intent = Intent(this, PasswordActivity::class.java)
        startActivity(intent)
        finish()
    }
}