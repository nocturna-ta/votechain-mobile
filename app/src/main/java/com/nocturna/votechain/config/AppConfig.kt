//package com.nocturna.votechain.config
//
//import android.content.Context
//import android.content.SharedPreferences
//import android.util.Log
//
///**
// * Application configuration class that centralizes all network endpoints
// * and allows for runtime configuration changes
// */
//object AppConfig {
//    private const val TAG = "AppConfig"
//    private const val PREFS_NAME = "VoteChainConfig"
//
//    // Default values
//    private const val DEFAULT_REST_API_URL = "https://2e81-36-69-142-17.ngrok-free.app"
//    private const val DEFAULT_GRPC_HOST = "2e81-36-69-142-17.ngrok-free.app"  // Updated from hard-coded IP
//    private const val DEFAULT_GRPC_PORT = 443  // Updated from 35000 to standard HTTPS port
//    private const val DEFAULT_GRPC_USE_TLS = true
//
//    // SharedPreferences keys
//    private const val KEY_REST_API_URL = "rest_api_url"
//    private const val KEY_GRPC_HOST = "grpc_host"
//    private const val KEY_GRPC_PORT = "grpc_port"
//    private const val KEY_GRPC_USE_TLS = "grpc_use_tls"
//
//    private var prefs: SharedPreferences? = null
//
//    /**
//     * Initialize the configuration with application context
//     */
//    fun initialize(context: Context) {
//        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        Log.d(TAG, "AppConfig initialized with REST URL: $restApiUrl, gRPC: $grpcHost:$grpcPort (TLS: $grpcUseTls)")
//    }
//
//    /**
//     * REST API base URL
//     */
//    val restApiUrl: String
//        get() = prefs?.getString(KEY_REST_API_URL, DEFAULT_REST_API_URL) ?: DEFAULT_REST_API_URL
//
//    /**
//     * gRPC server host
//     */
//    val grpcHost: String
//        get() = prefs?.getString(KEY_GRPC_HOST, DEFAULT_GRPC_HOST) ?: DEFAULT_GRPC_HOST
//
//    /**
//     * gRPC server port
//     */
//    val grpcPort: Int
//        get() = prefs?.getInt(KEY_GRPC_PORT, DEFAULT_GRPC_PORT) ?: DEFAULT_GRPC_PORT
//
//    /**
//     * Whether to use TLS for gRPC connections
//     */
//    val grpcUseTls: Boolean
//        get() = prefs?.getBoolean(KEY_GRPC_USE_TLS, DEFAULT_GRPC_USE_TLS) ?: DEFAULT_GRPC_USE_TLS
//
//    /**
//     * Update the REST API URL
//     */
//    fun setRestApiUrl(url: String) {
//        prefs?.edit()?.putString(KEY_REST_API_URL, url)?.apply()
//        Log.d(TAG, "REST API URL updated to: $url")
//    }
//
//    /**
//     * Update the gRPC server host
//     */
//    fun setGrpcHost(host: String) {
//        prefs?.edit()?.putString(KEY_GRPC_HOST, host)?.apply()
//        Log.d(TAG, "gRPC host updated to: $host")
//    }
//
//    /**
//     * Update the gRPC server port
//     */
//    fun setGrpcPort(port: Int) {
//        prefs?.edit()?.putInt(KEY_GRPC_PORT, port)?.apply()
//        Log.d(TAG, "gRPC port updated to: $port")
//    }
//
//    /**
//     * Update whether to use TLS for gRPC connections
//     */
//    fun setGrpcUseTls(useTls: Boolean) {
//        prefs?.edit()?.putBoolean(KEY_GRPC_USE_TLS, useTls)?.apply()
//        Log.d(TAG, "gRPC TLS setting updated to: $useTls")
//    }
//
//    /**
//     * Get the full gRPC connection address with protocol
//     */
//    val grpcAddress: String
//        get() {
//            val protocol = if (grpcUseTls) "https" else "http"
//            return "$protocol://$grpcHost:$grpcPort"
//        }
//
//    /**
//     * Reset all settings to defaults
//     */
//    fun resetToDefaults() {
//        prefs?.edit()?.apply {
//            putString(KEY_REST_API_URL, DEFAULT_REST_API_URL)
//            putString(KEY_GRPC_HOST, DEFAULT_GRPC_HOST)
//            putInt(KEY_GRPC_PORT, DEFAULT_GRPC_PORT)
//            putBoolean(KEY_GRPC_USE_TLS, DEFAULT_GRPC_USE_TLS)
//            apply()
//        }
//        Log.d(TAG, "All settings reset to defaults")
//    }
//}
