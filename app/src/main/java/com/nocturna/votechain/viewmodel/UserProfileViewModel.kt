package com.nocturna.votechain.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nocturna.votechain.data.model.CompleteUserProfile
import com.nocturna.votechain.data.repository.UserProfileRepository
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
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    private val TAG = "UserProfileViewModel"

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Initial)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

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

/**
 * Factory untuk UserProfileViewModel
 */
class UserProfileViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            return UserProfileViewModel(
                UserProfileRepository(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}