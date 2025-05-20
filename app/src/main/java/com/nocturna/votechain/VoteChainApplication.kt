package com.nocturna.votechain

import android.app.Application
import android.util.Log
import com.nocturna.votechain.blockchain.BlockchainManager
import com.nocturna.votechain.utils.LanguageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Custom Application class to initialize blockchain connection and language manager
 * Make sure to add this to AndroidManifest.xml with android:name=".VoteChainApplication"
 */
class VoteChainApplication : Application() {
    private val TAG = "VoteChainApplication"

    // Application scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Initialize language manager
        initializeLanguageManager()
    }

    private fun initializeLanguageManager() {
        // Initialize with saved language preference (defaults to Indonesian)
        LanguageManager.initialize(this)
        Log.d(TAG, "Language initialized to: ${LanguageManager.getLanguage(this)}")
    }
}