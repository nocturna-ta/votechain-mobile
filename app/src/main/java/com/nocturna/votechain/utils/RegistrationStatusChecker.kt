package com.nocturna.votechain.utils

import android.content.Context
import android.util.Log
import com.nocturna.votechain.data.repository.RegistrationStateManager
import com.nocturna.votechain.data.repository.UserLoginRepository

/**
 * Enhanced utility class to handle registration state checking on app launch
 */
class RegistrationStatusChecker(private val context: Context) {

    private val TAG = "RegistrationStatusChecker"
    private val registrationStateManager = RegistrationStateManager(context)
    private val userLoginRepository = UserLoginRepository(context)

    /**
     * Check if there's a pending registration state
     * @return true if there's a pending, approved, or rejected registration
     */
    fun hasRegistrationState(): Boolean {
        val state = registrationStateManager.getRegistrationState()
        Log.d(TAG, "Current registration state: $state")
        return state != RegistrationStateManager.STATE_NONE
    }

    /**
     * Get the appropriate start destination based on registration and login state
     * @return The route name of the screen that should be shown
     */
    fun getStartDestination(): String {
        // First check if user is already logged in
        val isLoggedIn = userLoginRepository.isUserLoggedIn()
        Log.d(TAG, "User logged in status: $isLoggedIn")

        if (isLoggedIn) {
            // User is logged in - check if they have registration status
            val registrationState = registrationStateManager.getRegistrationState()
            Log.d(TAG, "Logged in user registration state: $registrationState")

            return when (registrationState) {
                RegistrationStateManager.STATE_WAITING -> {
                    Log.d(TAG, "Logged in user has waiting registration - show waiting from login")
                    "waiting_from_login"
                }
                RegistrationStateManager.STATE_APPROVED -> {
                    Log.d(TAG, "Logged in user has approved registration - show accepted from login")
                    "accepted_from_login"
                }
                RegistrationStateManager.STATE_REJECTED -> {
                    Log.d(TAG, "Logged in user has rejected registration - show rejected from login")
                    "rejected_from_login"
                }
                else -> {
                    Log.d(TAG, "Logged in user has no pending registration - go to home")
                    "home"
                }
            }
        } else {
            // User not logged in - check registration state for different flow
            val registrationState = registrationStateManager.getRegistrationState()
            Log.d(TAG, "Not logged in, registration state: $registrationState")

            return when (registrationState) {
                RegistrationStateManager.STATE_WAITING -> {
                    Log.d(TAG, "Not logged in but has waiting registration - show login to continue")
                    "login"
                }
                RegistrationStateManager.STATE_APPROVED -> {
                    Log.d(TAG, "Not logged in but has approved registration - show login")
                    "login"
                }
                RegistrationStateManager.STATE_REJECTED -> {
                    Log.d(TAG, "Not logged in and has rejected registration - show register")
                    "register"
                }
                else -> {
                    Log.d(TAG, "Not logged in and no registration state - show login")
                    "login"
                }
            }
        }
    }

    /**
     * Check if user should be redirected to registration status screen from login
     */
    fun shouldShowRegistrationStatusFromLogin(): Pair<Boolean, String?> {
        val isLoggedIn = userLoginRepository.isUserLoggedIn()

        if (isLoggedIn) {
            val registrationState = registrationStateManager.getRegistrationState()

            return when (registrationState) {
                RegistrationStateManager.STATE_WAITING -> Pair(true, "waiting_from_login")
                RegistrationStateManager.STATE_APPROVED -> Pair(true, "accepted_from_login")
                RegistrationStateManager.STATE_REJECTED -> Pair(true, "rejected_from_login")
                else -> Pair(false, null)
            }
        }

        return Pair(false, null)
    }

    /**
     * Clear all registration states (used when user completes registration flow)
     */
    fun clearRegistrationState() {
        Log.d(TAG, "Clearing registration state")
        registrationStateManager.clearRegistrationState()
    }

    /**
     * Get human readable status description
     */
    fun getStatusDescription(): String {
        val state = registrationStateManager.getRegistrationState()
        return when (state) {
            RegistrationStateManager.STATE_WAITING -> "Registration pending verification"
            RegistrationStateManager.STATE_APPROVED -> "Registration approved"
            RegistrationStateManager.STATE_REJECTED -> "Registration rejected"
            else -> "No pending registration"
        }
    }
}