//package com.nocturna.votechain.test
//
//import android.util.Log
//import com.nocturna.votechain.data.network.NetworkClient
//import com.nocturna.votechain.data.network.RegisterRequest
//import com.nocturna.votechain.data.repository.UserRepository
//import kotlinx.coroutines.runBlocking
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.Date
//import android.content.Context
//import android.net.Uri
//
///**
// * Simple test class to verify API functionality
// * This is not a unit test but a utility to help verify the API integration
// *
// * How to use:
// * 1. Create an instance of this class in any activity or fragment
// * 2. Call testApiRegistrationWithKtp() with a KTP file URI to test the API
// * 3. Check logcat for results
// */
//class ApiImplementationTest(private val context: Context) {
//
//    private val userRepository = UserRepository(context)
//    private val TAG = "API_TEST"
//
//    /**
//     * Test the registration API with KTP file upload
//     * This needs to be called from an Activity or Fragment where a sample file can be provided
//     * @param ktpFileUri URI pointing to a KTP file
//     * @return true if successful, false otherwise
//     */
//    fun testApiRegistrationWithKtp(ktpFileUri: Uri): Boolean = runBlocking {
//        try {
//            // Create sample data with timestamp to make it unique
//            val timestamp = System.currentTimeMillis().toString().takeLast(6)
//            val email = "testuser$timestamp@example.com"
//
//            Log.d(TAG, "Testing registration with KTP upload, email: $email")
//
//            val result = userRepository.registerUserWithKtp(
//                nik = "1234567890$timestamp",
//                fullName = "Test User",
//                email = email,
//                password = "password123",
//                birthPlace = "Test City",
//                birthDate = formatDate(Date()),
//                address = "123 Test Street",
//                province = "Test Province",
//                region = "Test Region",
//                gender = "male",
//                username = "", // Empty username
//                role = "voter", // Make sure role is provided
//                ktpFileUri = ktpFileUri
//            )
//
//            // Print result to logcat
//            result.fold(
//                onSuccess = { response ->
//                    Log.d(TAG, "API Registration with KTP Success: $response")
//                    return@runBlocking true
//                },
//                onFailure = { exception ->
//                    Log.e(TAG, "API Registration with KTP Failed: ${exception.message}")
//                    return@runBlocking false
//                }
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "API Test Exception: ${e.message}")
//            e.printStackTrace()
//            return@runBlocking false
//        }
//    }
//
//    /**
//     * Test with hardcoded values and KTP file (useful for direct debugging)
//     * @param ktpFileUri URI pointing to a KTP file
//     */
//    fun testWithHardcodedValuesAndKtp(ktpFileUri: Uri): Boolean = runBlocking {
//        try {
//            val timestamp = System.currentTimeMillis().toString().takeLast(6)
//            val email = "testuser$timestamp@example.com"
//
//            Log.d(TAG, "Testing with hardcoded values and KTP - email: $email")
//
//            val result = userRepository.registerUserWithKtp(
//                nik = "1234567890123456",
//                fullName = "Test User",
//                email = email,
//                password = "password123",
//                birthPlace = "Test City",
//                birthDate = "2000-01-01", // Already in yyyy-MM-dd format
//                address = "123 Test Street",
//                province = "Test Province",
//                region = "Test Region",
//                gender = "male",
//                username = "", // Empty username
//                role = "voter", // Include role
//                ktpFileUri = ktpFileUri
//            )
//
//            result.fold(
//                onSuccess = { response ->
//                    Log.d(TAG, "Hardcoded test with KTP success: $response")
//                    return@runBlocking true
//                },
//                onFailure = { exception ->
//                    Log.e(TAG, "Hardcoded test with KTP failed: ${exception.message}")
//                    return@runBlocking false
//                }
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "Hardcoded test exception: ${e.message}")
//            e.printStackTrace()
//            return@runBlocking false
//        }
//    }
//
//    /**
//     * Format date in API format (yyyy-MM-dd)
//     */
//    private fun formatDate(date: Date): String {
//        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        return formatter.format(date)
//    }
//
//    /**
//     * Get the API base URL
//     * @return the base URL
//     */
//    fun getBaseUrl(): String {
//        return NetworkClient.BASE_URL
//    }
//}