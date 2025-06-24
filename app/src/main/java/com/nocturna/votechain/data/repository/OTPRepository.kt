package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.model.OTPGenerateRequest
import com.nocturna.votechain.data.model.OTPGenerateResponse
import com.nocturna.votechain.data.model.OTPVerifyRequest
import com.nocturna.votechain.data.model.OTPVerifyResponse
import com.nocturna.votechain.data.model.VoterData
import com.nocturna.votechain.data.network.NetworkClient
import com.nocturna.votechain.data.network.NetworkClient.otpApiService
import com.nocturna.votechain.data.network.OTPApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Repository for handling OTP verification operations
 */
class OTPRepository(private val context: Context) {
    private val TAG = "OTPRepository"
    private val PREFS_NAME = "VoteChainPrefs"
    private val KEY_OTP_TOKEN = "otp_token"
    private val voterRepository = VoterRepository(context)

    /**
     * Generate OTP for voting verification
     */
    fun generateVotingOTP(categoryId: String): Flow<Result<OTPGenerateResponse>> = flow {
        try {
            Log.d(TAG, "Generating OTP for category: $categoryId")

            val token = getStoredToken()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("Authentication token not found")))
                return@flow
            }

            val voterResult = voterRepository.fetchVoterData(token)
            voterResult.fold(
                onSuccess = { voterData ->
                    val request = OTPGenerateRequest(
                        phone_number = "+6285722663467",
                        purpose = "vote_cast",
                        voter_id = voterData.id
                    )

                    Log.d(TAG, "Generating OTP with request: phone=${request.phone_number}, voter_id=${request.voter_id}")
                    val response = NetworkClient.otpApiService.generateOTP("Bearer $token", request)

                    if (response.isSuccessful) {
                        response.body()?.let { otpResponse ->
                            // Use helper function for cleaner code
                            if (otpResponse.isSuccessful()) {
                                Log.d(TAG, "OTP operation successful for voter: ${voterData.id}")

                                // Handle specific scenarios
                                if (otpResponse.isOTPAlreadyExists()) {
                                    Log.d(TAG, "OTP already exists and is still valid")
                                } else {
                                    Log.d(TAG, "New OTP generated successfully")
                                }

                                emit(Result.success(otpResponse))
                            } else {
                                val errorMsg = otpResponse.getErrorMessage()
                                Log.e(TAG, "OTP generation failed: $errorMsg")
                                emit(Result.failure(Exception(errorMsg)))
                            }
                        } ?: run {
                            Log.e(TAG, "Empty response body from OTP API")
                            emit(Result.failure(Exception("Empty response from server")))
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "OTP API failed with code: ${response.code()}, body: $errorBody")
                        emit(Result.failure(Exception("Failed to generate OTP: ${response.code()}")))
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to fetch voter data: ${error.message}")
                    emit(Result.failure(Exception("Failed to fetch voter data: ${error.message}")))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during OTP generation", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Verify OTP code
     */
    fun verifyVotingOTP(categoryId: String, otpCode: String): Flow<Result<OTPVerifyResponse>> = flow {
        try {
            Log.d(TAG, "Verifying OTP code for category: $categoryId")

            val token = getStoredToken()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("Authentication token not found")))
                return@flow
            }

            val voterResult = voterRepository.fetchVoterData(token)
            voterResult.fold(
                onSuccess = { voterData ->
                    val request = OTPVerifyRequest(
                        code = otpCode,
                        purpose = "vote_cast",
                        voter_id = voterData.id
                    )

                    Log.d(TAG, "Verifying OTP with voter_id: ${voterData.id}")
                    val response = NetworkClient.otpApiService.verifyOTP("Bearer $token", request)

                    if (response.isSuccessful) {
                        response.body()?.let { verifyResponse ->
                            // Use helper function for cleaner code
                            if (verifyResponse.isVerificationSuccessful()) {
                                Log.d(TAG, "OTP verification successful")
                                // Store OTP token for voting process
                                storeOTPToken(verifyResponse.data!!.otp_token)
                                emit(Result.success(verifyResponse))
                            } else {
                                val errorMsg = verifyResponse.getErrorMessage()
                                Log.e(TAG, "OTP verification failed: $errorMsg")
                                emit(Result.failure(Exception(errorMsg)))
                            }
                        } ?: run {
                            Log.e(TAG, "Empty response body from OTP verify API")
                            emit(Result.failure(Exception("Empty response from server")))
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "OTP verify API failed with code: ${response.code()}, body: $errorBody")
                        emit(Result.failure(Exception("OTP verification failed: ${response.code()}")))
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to fetch voter data for verification: ${error.message}")
                    emit(Result.failure(Exception("Failed to verify voter data: ${error.message}")))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during OTP verification", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)


    /**
     * Resend OTP for voting verification - FIXED VERSION
     */
    fun resendVotingOTP(categoryId: String): Flow<Result<OTPGenerateResponse>> = flow {
        try {
            Log.d(TAG, "Resending OTP for category: $categoryId")

            val token = getStoredToken()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("Authentication token not found")))
                return@flow
            }

            val voterResult = voterRepository.fetchVoterData(token)
            voterResult.fold(
                onSuccess = { voterData ->
                    val request = OTPGenerateRequest(
                        phone_number = "+6285722663467",
                        purpose = "vote_cast",
                        voter_id = voterData.id
                    )

                    val response = otpApiService.resendOTP("Bearer $token", request)

                    if (response.isSuccessful) {
                        response.body()?.let { otpResponse ->
                            // FIX: Accept HTTP status codes (200-299) and internal success code (0)
                            if (otpResponse.data != null && (otpResponse.code in 200..299 || otpResponse.code == 0)) {
                                Log.d(TAG, "OTP resent successfully for voter: ${voterData.id}")
                                Log.d(TAG, "Resend Status: ${otpResponse.data.message}")
                                emit(Result.success(otpResponse))
                            } else {
                                val errorMsg = otpResponse.error?.error_message ?: "Failed to resend OTP"
                                Log.e(TAG, "OTP resend failed: $errorMsg")
                                emit(Result.failure(Exception(errorMsg)))
                            }
                        } ?: run {
                            Log.e(TAG, "Empty response body from OTP resend API")
                            emit(Result.failure(Exception("Empty response from server")))
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "OTP resend API failed with code: ${response.code()}, body: $errorBody")
                        emit(Result.failure(Exception("API Error: ${response.code()} - $errorBody")))
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to fetch voter data for resend: ${error.message}")
                    emit(Result.failure(Exception("Failed to fetch voter data: ${error.message}")))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during OTP resend", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

//    /**
//     * Verify OTP for voting
//     */
//    fun verifyVotingOTP(voterData: VoterData, otpCode: String): Flow<Result<OTPVerifyResponse>> = flow {
//        try {
//            Log.d(TAG, "Verifying OTP for voting - voter_id: ${voterData.id}")
//
//            val token = getStoredToken()
//            if (token.isNullOrEmpty()) {
//                emit(Result.failure(Exception("Authentication token not found")))
//                return@flow
//            }
//
//            // Get voter data for verification
//            val voterResult = voterRepository.fetchVoterData(token)
//
//            voterResult.fold(
//                onSuccess = { voterData ->
//                    val request = OTPVerifyRequest(
//                        code = otpCode,
//                        purpose = "vote_cast",
//                        voter_id = voterData.id
//                    )
//
//                    Log.d(TAG, "Verifying OTP with voter_id: ${voterData.id}")
//
//                    val response = NetworkClient.otpApiService.verifyOTP("Bearer $token", request)
//
//                    if (response.isSuccessful) {
//                        response.body()?.let { verifyResponse ->
//                            if (verifyResponse.code == 0 && verifyResponse.data?.is_valid == true) {
//                                Log.d(TAG, "OTP verification successful")
//                                // Store OTP token for voting process
//                                storeOTPToken(verifyResponse.data.otp_token)
//                                emit(Result.success(verifyResponse))
//                            } else {
//                                val errorMsg = verifyResponse.error?.error_message ?:
//                                verifyResponse.data?.message ?: "Invalid OTP code"
//                                Log.e(TAG, "OTP verification failed: $errorMsg")
//                                emit(Result.failure(Exception(errorMsg)))
//                            }
//                        } ?: run {
//                            Log.e(TAG, "Empty response body from OTP verify API")
//                            emit(Result.failure(Exception("Empty response from server")))
//                        }
//                    } else {
//                        val errorBody = response.errorBody()?.string()
//                        Log.e(TAG, "OTP verify API failed with code: ${response.code()}, body: $errorBody")
//                        emit(Result.failure(Exception("OTP verification failed: ${response.code()}")))
//                    }
//                },
//                onFailure = { error ->
//                    Log.e(TAG, "Failed to fetch voter data for verification: ${error.message}")
//                    emit(Result.failure(Exception("Failed to verify voter data: ${error.message}")))
//                }
//            )
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Exception during OTP verification", e)
//            emit(Result.failure(e))
//        }
//    }.flowOn(Dispatchers.IO)

    /**
     * Get stored authentication token
     */
    private fun getStoredToken(): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
            ?: sharedPreferences.getString("user_token", null)
    }

    /**
     * Store OTP token for voting process
     */
    private fun storeOTPToken(token: String) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(KEY_OTP_TOKEN, token).apply()
        Log.d(TAG, "OTP token stored successfully")
    }

    /**
     * Get stored OTP token
     */
    fun getStoredOTPToken(): String? {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_OTP_TOKEN, null)
    }

    /**
     * Clear stored OTP token
     */
    fun clearOTPToken() {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().remove(KEY_OTP_TOKEN).apply()
        Log.d(TAG, "OTP token cleared")
    }
}