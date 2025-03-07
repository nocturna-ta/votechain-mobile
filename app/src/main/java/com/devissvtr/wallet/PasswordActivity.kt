package com.devissvtr.wallet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devissvtr.wallet.databinding.ActivityPasswordBinding

class PasswordActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityPasswordBinding
    private var password: String = ""
    private var count : Int = 0
    private var masterPassword : String = ""
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set Password
        masterPassword = "1234"

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
                if (password == masterPassword) {
                    startActivity(Intent(this, AccountActivity::class.java))
                } else {
                    showWrongMessage()
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
        password += view.text
        count++
        updatePasswordView(count, true)

        if (count == 4) {
            if (password == masterPassword) {
                startActivity(Intent(this, AccountActivity::class.java))
            } else {
                showWrongMessage()
                resetPassword()
            }
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

    private fun showWrongMessage() {
        Toast.makeText(this, "Wrong Password!", Toast.LENGTH_SHORT).show()
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