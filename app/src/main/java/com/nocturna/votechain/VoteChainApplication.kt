//package com.nocturna.votechain
//
//import android.app.Application
//import android.util.Log
//import com.nocturna.votechain.blockchain.BlockchainManager
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.launch
//
///**
// * Custom Application class to initialize blockchain connection
// * Make sure to add this to AndroidManifest.xml with android:name=".VoteChainApplication"
// */
//class VoteChainApplication : Application() {
//    private val TAG = "VoteChainApplication"
//
//    // Application scope
//    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
//
//    override fun onCreate() {
//        super.onCreate()
//
//        // Initialize blockchain connection in background
//        initializeBlockchain()
//    }
//
//    private fun initializeBlockchain() {
//        Log.d(TAG, "Initializing blockchain connection...")
//
//        applicationScope.launch(Dispatchers.IO) {
//            try {
//                // Get Web3j instance to initialize it
//                val web3j = BlockchainManager.getWeb3jInstance()
//
//                // Test connection
//                val isConnected = BlockchainManager.isConnected()
//                Log.d(TAG, "Blockchain connection status: ${if (isConnected) "CONNECTED" else "FAILED"}")
//
//                if (isConnected) {
//                    // Get client version to verify connection details
//                    val clientVersion = web3j.web3ClientVersion().send().web3ClientVersion
//                    Log.d(TAG, "Connected to Ethereum node: $clientVersion")
//                } else {
//                    Log.w(TAG, "Unable to connect to Ethereum node. Some blockchain features may not work.")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error initializing blockchain connection: ${e.message}", e)
//            }
//        }
//    }
//
//    override fun onTerminate() {
//        // Shut down Web3j connection
//        try {
//            BlockchainManager.shutdown()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error shutting down blockchain connection: ${e.message}", e)
//        }
//
//        super.onTerminate()
//    }
//}