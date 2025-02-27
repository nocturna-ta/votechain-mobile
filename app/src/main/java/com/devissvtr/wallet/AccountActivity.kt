package com.devissvtr.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devissvtr.wallet.databinding.ActivityAccountBinding
import java.util.Locale

class AccountActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAccountBinding
    private var balance : Double = 0.0
    private var nik : String = ""
    private var privateKey : String = ""
    private var publicKey : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set Field
        balance = 99.00023456789876
        nik = "0987654321"
        privateKey = "00POIUY"
        publicKey = "11QWERT"

        val formattedBalance = String.format(Locale.getDefault(),"%.8f", balance)
        binding.tvFieldBalance.text = formattedBalance
        binding.tvFieldNik.text = nik
        binding.tvFieldPrivateKey.text = privateKey
        binding.tvFieldPublicKey.text = publicKey

        binding.ivCopy.setOnClickListener {
            copyClipboard(publicKey)
        }

        binding.ivBack.setOnClickListener {
            navigatePreviousPage()
        }
    }

    private fun copyClipboard(text : String) {
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