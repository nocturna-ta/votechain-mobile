package com.nocturna.votechain.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.nocturna.votechain.data.model.ApiResponse
import com.nocturna.votechain.data.model.UserRegistrationData
import com.nocturna.votechain.data.model.UserRegistrationRequest
import com.nocturna.votechain.data.network.ApiRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Repository untuk mengelola operasi user (registrasi, login, dll)
 */
class UserRepository(private val context: Context) {
    private val apiRepository = ApiRepository()
    private val TAG = "UserRepository"

    /**
     * Register user dengan file KTP
     */
    suspend fun registerUser(
        email: String,
        password: String,
        nik: String,
        fullName: String,
        gender: String,
        birthPlace: String,
        birthDate: String,
        residentialAddress: String,
        region: String,
        role: String = "voter",
        voterAddress: String,
        ktpFileUri: Uri?
    ): Result<ApiResponse<UserRegistrationData>> {
        return try {
            Log.d(TAG, "Registering user with email: $email")

            // Prepare request data
            val request = UserRegistrationRequest(
                email = email,
                password = password,
                nik = nik,
                fullName = fullName,
                gender = gender,
                birthPlace = birthPlace,
                birthDate = birthDate,
                residentialAddress = residentialAddress,
                region = region,
                role = role,
                voterAddress = voterAddress
            )

            // Prepare file part if KTP file is provided
            val ktpFilePart = ktpFileUri?.let { uri ->
                createMultipartFromUri(uri, "ktp_file")
            }

            Log.d(TAG, "Sending registration request to API")

            // Make API call
            val response = apiRepository.registerUser(request, ktpFilePart)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    Log.d(TAG, "Registration successful: ${apiResponse.message}")
                    Result.success(apiResponse)
                } else {
                    Log.e(TAG, "Registration response body is null")
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                val errorMessage = "Registration failed: ${response.code()} ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Registration error", e)
            Result.failure(e)
        }
    }

    /**
     * Login user
     */
    suspend fun loginUser(email: String, password: String): Result<ApiResponse<Any>> {
        return try {
            Log.d(TAG, "Logging in user with email: $email")

            val response = apiRepository.loginUser(email, password)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    Log.d(TAG, "Login successful")
                    Result.success(apiResponse)
                } else {
                    Log.e(TAG, "Login response body is null")
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                val errorMessage = "Login failed: ${response.code()} ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            Result.failure(e)
        }
    }

    /**
     * Verify email with token
     */
    suspend fun verifyEmail(token: String): Result<ApiResponse<Any>> {
        return try {
            Log.d(TAG, "Verifying email with token")

            val response = apiRepository.verifyEmail(token)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    Log.d(TAG, "Email verification successful")
                    Result.success(apiResponse)
                } else {
                    Log.e(TAG, "Verification response body is null")
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                val errorMessage = "Email verification failed: ${response.code()} ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Email verification error", e)
            Result.failure(e)
        }
    }

    /**
     * Resend verification email
     */
    suspend fun resendVerification(email: String): Result<ApiResponse<Any>> {
        return try {
            Log.d(TAG, "Resending verification email to: $email")

            val response = apiRepository.resendVerification(email)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    Log.d(TAG, "Resend verification successful")
                    Result.success(apiResponse)
                } else {
                    Log.e(TAG, "Resend verification response body is null")
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                val errorMessage = "Resend verification failed: ${response.code()} ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Resend verification error", e)
            Result.failure(e)
        }
    }

    /**
     * Create MultipartBody.Part from Uri
     */
    private fun createMultipartFromUri(uri: Uri, partName: String): MultipartBody.Part? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.let { stream ->
                // Create temporary file
                val tempFile = File.createTempFile("upload", ".tmp", context.cacheDir)
                val outputStream = FileOutputStream(tempFile)

                stream.copyTo(outputStream)
                stream.close()
                outputStream.close()

                // Get file extension and mime type
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val extension = when (mimeType) {
                    "image/jpeg" -> "jpg"
                    "image/png" -> "png"
                    "image/jpg" -> "jpg"
                    else -> "jpg"
                }

                // Create request body
                val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())

                // Create multipart body part
                MultipartBody.Part.createFormData(
                    partName,
                    "ktp_file.$extension",
                    requestBody
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating multipart from URI", e)
            null
        }
    }

    /**
     * Get file name from Uri
     */
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = it.getString(displayNameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: "unknown_file"
    }
}