package com.devissvtr.wallet

import android.app.Application
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
import java.math.BigInteger

class MyApp : Application() {
    lateinit var web3: Web3j
    var selectedCredentials: org.web3j.crypto.Credentials? = null
    var password: String = ""
    var contractAddress = ""
    val gasPrice = BigInteger.valueOf(20000000000L)
    val gasLimit = BigInteger.valueOf(6721975L)

    override fun onCreate() {
        super.onCreate()
        web3 = Web3j.build(HttpService("http://192.168.1.100:7545"))
    }

    fun getGasProvider(): StaticGasProvider{
        return StaticGasProvider(gasPrice, gasLimit)
    }
}