package com.nocturna.votechain.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.nocturna.votechain.data.model.*
import com.nocturna.votechain.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class untuk mengelola user profile operations
 */
class UserProfileRepository(private val context: Context) {
    private val TAG = "UserProfileRepository"
    private val apiService = NetworkClient.apiService
    private val userLoginRepository = UserLoginRepository(context)

    private val PREFS_NAME = "VoteChainPrefs"
    private val KEY_USER_PROFILE = "user_profile"
    private val KEY_COMPLETE_PROFILE = "complete_profile"

    /**
     * Fetch complete user profile (user data + voter data)
     * Menggunakan email yang tersimpan dari login
     */
    suspend fun fetchCompleteUserProfile(): Result<CompleteUserProfile> = withContext(Dispatchers.IO) {
        try {
            val userEmail = userLoginRepository.getUserEmail()
            val userToken = userLoginRepository.getUserToken()

            if (userEmail.isEmpty() || userToken.isEmpty()) {
                return@withContext Result.failure(Exception("User not logged in"))
            }

            Log.d(TAG, "Fetching profile for email: $userEmail")

            // Step 1: Get user profile dari /v1/user/{email}
            val userProfileResult = getUserProfile(userEmail, userToken)

            return@withContext userProfileResult.fold(
                onSuccess = { userProfile ->
                    Log.d(TAG, "User profile fetched successfully for ID: ${userProfile.id}")

                    // Step 2: Get voter data menggunakan user_id dari profile
                    val voterProfileResult = getVoterProfileByUserId(userProfile.id, userToken)

                    voterProfileResult.fold(
                        onSuccess = { voterProfile ->
                            val completeProfile = CompleteUserProfile(
                                userProfile = userProfile,
                                voterProfile = voterProfile
                            )
                            saveCompleteProfile(completeProfile)
                            Result.success(completeProfile)
                        },
                        onFailure = { voterError ->
                            Log.w(TAG, "Failed to fetch voter profile: ${voterError.message}")
                            // Tetap return user profile meskipun voter data gagal
                            val completeProfile = CompleteUserProfile(
                                userProfile = userProfile,
                                voterProfile = null
                            )
                            saveCompleteProfile(completeProfile)
                            Result.success(completeProfile)
                        }
                    )
                },
                onFailure = { userError ->
                    Log.e(TAG, "Failed to fetch user profile: ${userError.message}")
                    Result.failure(userError)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during profile fetch", e)
            Result.failure(e)
        }
    }

    /**
     * Get user profile dari API /v1/user/{email}
     */
    private suspend fun getUserProfile(email: String, token: String): Result<UserProfileData> {
        return try {
            val response = apiService.getUserProfile(email, "Bearer $token")

            if (response.isSuccessful) {
                response.body()?.let { profileResponse ->
                    if (profileResponse.code == 0 && profileResponse.data != null) {
                        Log.d(TAG, "User profile API call successful")
                        Result.success(profileResponse.data)
                    } else {
                        val errorMsg = profileResponse.error?.error_message ?: profileResponse.message
                        Log.e(TAG, "User profile API returned error: $errorMsg")
                        Result.failure(Exception(errorMsg))
                    }
                } ?: run {
                    Log.e(TAG, "Empty response body from user profile API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "User profile API failed with code: ${response.code()}, body: $errorBody")
                Result.failure(Exception("API Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during user profile fetch", e)
            Result.failure(e)
        }
    }

    /**
     * Get voter profile berdasarkan user_id
     */
    private suspend fun getVoterProfileByUserId(userId: String, token: String): Result<VoterData> {
        return try {
            // Gunakan endpoint /v1/voter yang sudah ada
            val response = apiService.getVoterData("Bearer $token")

            if (response.isSuccessful) {
                response.body()?.let { voterResponse ->
                    if (voterResponse.code == 0 && voterResponse.data.isNotEmpty()) {
                        // Cari voter dengan user_id yang cocok
                        val matchingVoter = voterResponse.data.find { it.user_id == userId }

                        if (matchingVoter != null) {
                            Log.d(TAG, "Matching voter profile found for user_id: $userId")
                            Result.success(matchingVoter)
                        } else {
                            Log.w(TAG, "No voter profile found for user_id: $userId")
                            Result.failure(Exception("No voter profile found for this user"))
                        }
                    } else {
                        Log.e(TAG, "Voter profile API returned empty data or error")
                        Result.failure(Exception("No voter data available"))
                    }
                } ?: run {
                    Log.e(TAG, "Empty response body from voter profile API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Voter profile API failed with code: ${response.code()}, body: $errorBody")
                Result.failure(Exception("API Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during voter profile fetch", e)
            Result.failure(e)
        }
    }

    /**
     * Save complete profile ke SharedPreferences
     */
    private fun saveCompleteProfile(profile: CompleteUserProfile) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val profileJson = gson.toJson(profile)

        with(sharedPreferences.edit()) {
            putString(KEY_COMPLETE_PROFILE, profileJson)
            apply()
        }
        Log.d(TAG, "Complete profile saved to SharedPreferences")
    }

    /**
     * Get saved complete profile dari SharedPreferences
     */
    fun getSavedCompleteProfile(): CompleteUserProfile? {
        return try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val profileJson = sharedPreferences.getString(KEY_COMPLETE_PROFILE, null)

            if (profileJson != null) {
                val gson = Gson()
                gson.fromJson(profileJson, CompleteUserProfile::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing saved profile", e)
            null
        }
    }

    /**
     * Clear saved profile data
     */
    fun clearProfileData() {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(KEY_USER_PROFILE)
            remove(KEY_COMPLETE_PROFILE)
            apply()
        }
        Log.d(TAG, "Profile data cleared")
    }

    /**
     * Refresh profile data (fetch dari API lagi)
     */
    suspend fun refreshProfile(): Result<CompleteUserProfile> {
        clearProfileData()
        return fetchCompleteUserProfile()
    }
}