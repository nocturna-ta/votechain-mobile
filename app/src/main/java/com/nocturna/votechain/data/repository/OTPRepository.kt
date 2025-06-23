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

    val defaultPhoneNumber = "085722663467" // Default phone number for testing, replace with actual logic if needed

    /**
     * Generate OTP for voting verification
     */
    fun generateVotingOTP(voterData: VoterData, categoryId: String): Flow<Result<OTPGenerateResponse>> = flow {
        try {
            Log.d(TAG, "Generating OTP for voting verification - voter_id: ${voterData.id}")

            val token = getStoredToken()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("Authentication token not found")))
                return@flow
            }

            val request = OTPGenerateRequest(
//                phone_number = voterData.telephone ?: "",
                phone_number = defaultPhoneNumber,
                purpose = "vote_cast",
                voter_id = voterData.id
            )

            val response = otpApiService.generateOTP("Bearer $token", request)

            if (response.isSuccessful) {
                response.body()?.let { otpResponse ->
                    if (otpResponse.code == 0) {
                        Log.d(TAG, "OTP generated successfully for voter: ${voterData.id}")
                        emit(Result.success(otpResponse))
                    } else {
                        val errorMsg = otpResponse.error?.error_message ?: "Failed to generate OTP"
                        Log.e(TAG, "OTP generation failed: $errorMsg")
                        emit(Result.failure(Exception(errorMsg)))
                    }
                } ?: run {
                    Log.e(TAG, "Empty response body from OTP generate API")
                    emit(Result.failure(Exception("Empty response from server")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "OTP generate API failed with code: ${response.code()}, body: $errorBody")
                emit(Result.failure(Exception("API Error: ${response.code()} - $errorBody")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during OTP generation", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Resend OTP for voting verification
     */
    fun resendVotingOTP(voterData: VoterData, categoryId: String): Flow<Result<OTPGenerateResponse>> = flow {
        try {
            Log.d(TAG, "Resending OTP for voting verification - voter_id: ${voterData.id}")

            val token = getStoredToken()
            if (token.isNullOrEmpty()) {
                emit(Result.failure(Exception("Authentication token not found")))
                return@flow
            }

            val request = OTPGenerateRequest(
//                phone_number = voterData.telephone ?: "",
                phone_number = defaultPhoneNumber,
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
                phone_number = defaultPhoneNumber,
//                phone_number = voterData.telephone ?: "",
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
    }
}