package com.nocturna.votechain.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.CompleteUserProfile
import com.nocturna.votechain.data.repository.UserLoginRepository
import com.nocturna.votechain.data.repository.UserProfileRepository
import com.nocturna.votechain.data.repository.VoterRepository
import com.nocturna.votechain.security.CryptoKeyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State untuk Profile Screen
 */
sealed class ProfileUiState {
    object Initial : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: CompleteUserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    object NotLoggedIn : ProfileUiState()
}

/**
 * ViewModel untuk Profile Screen
 */
class UserProfileViewModel(
    private val context: Context
) : ViewModel() {
    private val userLoginRepository = UserLoginRepository(context)
    private val cryptoKeyManager = CryptoKeyManager(context)
    private val voterRepository = VoterRepository(context)
    private val userProfileRepository = UserProfileRepository(context)

    private val TAG = "ProfileViewModel"

    // State untuk private key
    private val _privateKeyState = MutableLiveData<String?>()
    val privateKeyState: LiveData<String?> = _privateKeyState

    private val _keyLoadingState = MutableLiveData<Boolean>(false)
    val keyLoadingState: LiveData<Boolean> = _keyLoadingState

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Initial)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * Load profile data with enhanced crypto key loading
     */
    fun loadProfileData() {
        viewModelScope.launch {
            try {
                _keyLoadingState.value = true

                // Step 1: Get current user email
                val userEmail = userLoginRepository.getUserEmail()
                if (userEmail.isNullOrEmpty()) {
                    Log.w(TAG, "No user email found")
                    return@launch
                }

                // Step 2: Debug current key state
                val debugInfo = cryptoKeyManager.debugKeyStorage()
                Log.d(TAG, "Current key state:\n$debugInfo")

                // Step 3: Attempt to load private key
                var privateKey = cryptoKeyManager.getPrivateKey()

                if (privateKey == null) {
                    Log.w(TAG, "Private key not found in CryptoKeyManager, attempting repair...")

                    // Step 4: Try to repair from backup
                    val repairSuccess = cryptoKeyManager.repairCorruptedKeys(userEmail)

                    if (repairSuccess) {
                        privateKey = cryptoKeyManager.getPrivateKey()
                        Log.d(TAG, "✅ Private key repaired successfully")
                    } else {
                        Log.e(TAG, "❌ Failed to repair private key")

                        // Step 5: Check backup storage directly
                        val backupPrivateKey = userLoginRepository.getPrivateKey(userEmail)
                        if (backupPrivateKey != null) {
                            Log.d(TAG, "Found private key in backup storage")
                            privateKey = backupPrivateKey
                        }
                    }
                }

                // Step 6: Update UI state
                _privateKeyState.value = privateKey

                // Step 7: Log result
                if (privateKey != null) {
                    Log.d(TAG, "✅ Private key loaded successfully for profile display")
                } else {
                    Log.e(TAG, "❌ Private key still not available after all attempts")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile data: ${e.message}", e)
                _privateKeyState.value = null
            } finally {
                _keyLoadingState.value = false
            }
        }
    }

    /**
     * Force reload private key (untuk refresh button)
     */
    fun forceReloadPrivateKey() {
        viewModelScope.launch {
            try {
                _keyLoadingState.value = true

                val userEmail = userLoginRepository.getUserEmail()
                if (userEmail == null) {
                    Log.w(TAG, "Cannot reload: no user email")
                    return@launch
                }

                // Force reload from storage
                val reloadSuccess = cryptoKeyManager.forceReloadKeys()

                if (reloadSuccess) {
                    val privateKey = cryptoKeyManager.getPrivateKey()
                    _privateKeyState.value = privateKey
                    Log.d(TAG, "✅ Private key force reloaded")
                } else {
                    // Try repair as last resort
                    val repairSuccess = cryptoKeyManager.repairCorruptedKeys(userEmail)
                    if (repairSuccess) {
                        _privateKeyState.value = cryptoKeyManager.getPrivateKey()
                        Log.d(TAG, "✅ Private key repaired during force reload")
                    } else {
                        Log.e(TAG, "❌ Force reload and repair both failed")
                        _privateKeyState.value = null
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during force reload: ${e.message}", e)
                _privateKeyState.value = null
            } finally {
                _keyLoadingState.value = false
            }
        }
    }

    /**
     * Check if keys need regeneration
     */
    fun checkKeyStatus(): KeyStatus {
        return try {
            val hasStoredKeys = cryptoKeyManager.hasStoredKeyPair()
            val privateKey = cryptoKeyManager.getPrivateKey()
            val publicKey = cryptoKeyManager.getPublicKey()
            val voterAddress = cryptoKeyManager.getVoterAddress()

            when {
                hasStoredKeys && privateKey != null && publicKey != null && voterAddress != null ->
                    KeyStatus.HEALTHY

                hasStoredKeys && (privateKey == null || publicKey == null || voterAddress == null) ->
                    KeyStatus.CORRUPTED

                else -> KeyStatus.MISSING
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking key status: ${e.message}", e)
            KeyStatus.ERROR
        }
    }

    enum class KeyStatus {
        HEALTHY,    // All keys present and accessible
        CORRUPTED,  // Keys exist but not accessible
        MISSING,    // No keys found
        ERROR       // Error during check
    }


//    init {
//        loadUserProfile()
//    }

    /**
     * Load user profile - coba dari cache dulu, kalau tidak ada fetch dari API
     */
//    fun loadUserProfile() {
//        _uiState.value = ProfileUiState.Loading
//
//        viewModelScope.launch {
//            try {
//                // Coba ambil dari cache dulu
//                val cachedProfile = userProfileRepository.getSavedCompleteProfile()
//
//                if (cachedProfile != null) {
//                    Log.d(TAG, "Loading profile from cache")
//                    _uiState.value = ProfileUiState.Success(cachedProfile)
//
//                    // Refresh data di background
//                    refreshProfileInBackground()
//                } else {
//                    // Tidak ada cache, fetch dari API
//                    Log.d(TAG, "No cached profile, fetching from API")
//                    fetchProfileFromApi()
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error loading profile", e)
//                _uiState.value = ProfileUiState.Error("Failed to load profile: ${e.message}")
//            }
//        }
//    }

    /**
     * Refresh profile data dari API
     */
//    fun refreshProfile() {
//        _uiState.value = ProfileUiState.Loading
//
//        viewModelScope.launch {
//            fetchProfileFromApi()
//        }
//    }

    /**
     * Fetch profile dari API
     */
//    private suspend fun fetchProfileFromApi() {
//        try {
//            val result = userProfileRepository.fetchCompleteUserProfile()
//
//            result.fold(
//                onSuccess = { profile ->
//                    Log.d(TAG, "Profile fetched successfully")
//                    _uiState.value = ProfileUiState.Success(profile)
//                },
//                onFailure = { error ->
//                    Log.e(TAG, "Failed to fetch profile: ${error.message}")
//
//                    // Check jika error karena not logged in
//                    if (error.message?.contains("not logged in") == true) {
//                        _uiState.value = ProfileUiState.NotLoggedIn
//                    } else {
//                        _uiState.value = ProfileUiState.Error(error.message ?: "Unknown error")
//                    }
//                }
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "Exception during profile fetch", e)
//            _uiState.value = ProfileUiState.Error("An unexpected error occurred: ${e.message}")
//        }
//    }

    /**
     * Refresh profile di background (tidak mengubah UI state ke loading)
     */
//    private fun refreshProfileInBackground() {
//        viewModelScope.launch {
//            try {
//                val result = userProfileRepository.refreshProfile()
//                result.fold(
//                    onSuccess = { profile ->
//                        Log.d(TAG, "Profile refreshed in background")
//                        _uiState.value = ProfileUiState.Success(profile)
//                    },
//                    onFailure = { error ->
//                        Log.w(TAG, "Background refresh failed: ${error.message}")
//                        // Tidak mengubah UI state karena ini background refresh
//                    }
//                )
//            } catch (e: Exception) {
//                Log.w(TAG, "Exception during background refresh", e)
//                // Tidak mengubah UI state karena ini background refresh
//            }
//        }
//    }

    /**
     * Get current profile data (jika ada)
     */
    fun getCurrentProfile(): CompleteUserProfile? {
        return when (val currentState = _uiState.value) {
            is ProfileUiState.Success -> currentState.profile
            else -> null
        }
    }

    /**
     * Clear profile data (untuk logout)
     */
    fun clearProfile() {
        userProfileRepository.clearProfileData()
        _uiState.value = ProfileUiState.Initial
    }
}