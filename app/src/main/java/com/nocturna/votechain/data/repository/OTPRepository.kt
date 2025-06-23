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
    private val voterRepository = VoterRepository(context)

    /**
     * Generate OTP for voting verification
     */
    fun generateVotingOTP(categoryId: String): Flow<Result<OTPGenerateResponse>> = flow {
        try {
            Log.d(TAG, "Starting OTP generation process for category: $categoryId")

            val token = getStoredToken()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("Authentication token not found")))
                return@flow
            }

            // Step 1: Fetch voter data from /v1/voter based on logged-in user email
            Log.d(TAG, "Fetching voter data from API...")
            val voterResult = voterRepository.fetchVoterData(token)

            voterResult.fold(
                onSuccess = { voterData ->
                    Log.d(TAG, "Voter data fetched successfully - ID: ${voterData.id}")

                    // Step 2: Generate OTP using voter data
                    val request = OTPGenerateRequest(
                        phone_number = voterData.telephone,
                        purpose = "vote_cast",
                        voter_id = voterData.id
                    )

                    Log.d(TAG, "Generating OTP with request: phone=${request.phone_number}, voter_id=${request.voter_id}")

                    val response = NetworkClient.otpApiService.generateOTP("Bearer $token", request)

                    if (response.isSuccessful) {
                        response.body()?.let { otpResponse ->
                            if (otpResponse.code == 0) {
                                Log.d(TAG, "OTP generated successfully for voter: ${voterData.id}")
                                // Log OTP code for local development (remove in production)
                                Log.d(TAG, "DEBUG - OTP Code: ${otpResponse.code}")

                                emit(Result.success(otpResponse))
                            } else {
                                val errorMsg = otpResponse.error?.error_message ?: "Failed to generate OTP"
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

            // Get voter data for verification
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
                            if (verifyResponse.code == 0 && verifyResponse.data?.is_valid == true) {
                                Log.d(TAG, "OTP verification successful")
                                // Store OTP token for voting process
                                storeOTPToken(verifyResponse.data.otp_token)
                                emit(Result.success(verifyResponse))
                            } else {
                                val errorMsg = verifyResponse.error?.error_message ?: "Invalid OTP code"
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
     * Resend OTP for voting verification
     */
    fun resendVotingOTP(categoryId: String): Flow<Result<OTPGenerateResponse>> = flow {
        try {
            Log.d(TAG, "Resending OTP for category: $categoryId")

            val token = getStoredToken()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("Authentication token not found")))
                return@flow
            }

            val request = OTPGenerateRequest(
                phone_number = voterData.telephone ?: "",
                purpose = "vote_cast",
                voter_id = voterData.id
            )

            val response = otpApiService.resendOTP("Bearer $token", request)

            if (response.isSuccessful) {
                response.body()?.let { otpResponse ->
                    if (otpResponse.code == 0) {
                        Log.d(TAG, "OTP resent successfully for voter: ${voterData.id}")
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
        } catch (e: Exception) {
            Log.e(TAG, "Exception during OTP resend", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Verify OTP for voting
     */
    fun verifyVotingOTP(voterData: VoterData, otpCode: String): Flow<Result<OTPVerifyResponse>> = flow {
        try {
            Log.d(TAG, "Verifying OTP for voting - voter_id: ${voterData.id}")

            val token = getStoredToken()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("Authentication token not found")))
                return@flow
            }

            val request = OTPVerifyRequest(
                phone_number = voterData.telephone ?: "",
                purpose = "vote_cast",
                voter_id = voterData.id,
                otp_code = otpCode
            )

            val response = otpApiService.verifyOTP("Bearer $token", request)

            if (response.isSuccessful) {
                response.body()?.let { verifyResponse ->
                    if (verifyResponse.code == 0 && verifyResponse.data?.is_valid == true) {
                        Log.d(TAG, "OTP verified successfully for voter: ${voterData.id}")
                        emit(Result.success(verifyResponse))
                    } else {
                        val errorMsg = verifyResponse.error?.error_message ?:
                        verifyResponse.data?.message ?: "Invalid OTP"
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
                emit(Result.failure(Exception("API Error: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during OTP verification", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

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
    private fun storeOTPToken(otpToken: String?) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("current_otp_token", otpToken)
            putLong("otp_token_timestamp", System.currentTimeMillis())
            apply()
        }
        Log.d(TAG, "OTP token stored for voting process")
    }

    /**
     * Get stored OTP token
     */
    fun getStoredOTPToken(): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("current_otp_token", null)
        val timestamp = sharedPreferences.getLong("otp_token_timestamp", 0)

        // Check if token is still valid (within 10 minutes)
        val isValid = (System.currentTimeMillis() - timestamp) < 10 * 60 * 1000

        return if (token != null && isValid) {
            Log.d(TAG, "Valid OTP token found")
            token
        } else {
            Log.d(TAG, "No valid OTP token found")
            null
        }
    }

    /**
     * Clear stored OTP token
     */
    fun clearOTPToken() {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("current_otp_token")
            remove("otp_token_timestamp")
            apply()
        }
        Log.d(TAG, "OTP token cleared")
    }
}