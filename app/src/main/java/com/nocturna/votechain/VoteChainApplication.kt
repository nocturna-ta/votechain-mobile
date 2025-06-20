package com.nocturna.votechain

import android.app.Application
import android.util.Log
import com.nocturna.votechain.blockchain.BlockchainManager
import com.nocturna.votechain.data.network.ElectionNetworkClient
import com.nocturna.votechain.data.network.NetworkClient
import com.nocturna.votechain.security.CryptoKeyManager
import com.nocturna.votechain.utils.CoilAuthHelper
import com.nocturna.votechain.utils.LanguageManager
import com.nocturna.votechain.utils.ThemeManager
import com.nocturna.votechain.utils.TokenManager
import com.nocturna.votechain.utils.TokenSyncUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

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

        Log.d("VoteChainApp", "Application initialized")

        // Initialize CryptoKeyManager BouncyCastle provider
        try {
            CryptoKeyManager.initializeBouncyCastle()
        } catch (e: Exception) {
            Log.e("VoteChainApp", "Failed to initialize BouncyCastle", e)
        }

        // Register BouncyCastle provider
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }

        // Initialize language manager
        initializeLanguageManager()

        // Initialize theme manager
        initializeThemeManager()

        // Initialize NetworkClient with application context
        NetworkClient.initialize(this)
        Log.d(TAG, "Application initialized with NetworkClient")

        // Initialize the ElectionNetworkClient with application context
        initializeElectionNetworkClient()

        // Validate and sync tokens
        val tokenManager = TokenManager(this)
        TokenSyncUtil.validateAndSyncTokens(this, tokenManager)

    }

    private fun initializeElectionNetworkClient() {
        // First initialize with application context
        ElectionNetworkClient.initialize(this)

        // Check if there's a token already stored
        val hasToken = ElectionNetworkClient.hasValidToken()
        Log.i(TAG, "ElectionNetworkClient initialized with token status: ${if (hasToken) "Token available" else "No token"}")

        // If there's no token and you have a default/demo token, you can set it here
        if (!hasToken) {
            // This is a placeholder for a potential demo token for testing
            // In a real app, you would typically get this from a login process
            val demoToken = getDemoTokenIfAvailable()
            if (demoToken.isNotEmpty()) {
                Log.i(TAG, "Setting demo token for ElectionNetworkClient")
                ElectionNetworkClient.saveUserToken(demoToken)
            }
        }
    }

    /**
     * Get a demo token if available (for testing purposes)
     * In a real app, this would come from a login process
     */
    private fun getDemoTokenIfAvailable(): String {
        // This is where you could provide a demo/default token for testing
        // For security reasons, don't hard-code real tokens in production code
        // This should be replaced with a proper authentication flow
        return "" // Return empty string for now - replace with actual token if needed
    }

    private fun initializeLanguageManager() {
        // Initialize with saved language preference (defaults to Indonesian)
        LanguageManager.initialize(this)
        Log.d(TAG, "Language initialized to: ${LanguageManager.getLanguage(this)}")
    }

    private fun initializeThemeManager() {
        // Initialize with saved theme preference (defaults to Light Mode)
        ThemeManager.initialize(this)
        Log.d(TAG, "Theme initialized to: ${ThemeManager.getTheme(this)}")
    }
}