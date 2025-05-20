package com.nocturna.votechain.utils

import android.content.Context
import com.nocturna.votechain.data.repository.RegistrationStateManager

/**
 * Utility class to handle registration state checking on app launch
 */
class RegistrationStatusChecker(private val context: Context) {

    private val registrationStateManager = RegistrationStateManager(context)

    /**
     * Check if there's a pending registration state
     * @return true if there's a pending, approved, or rejected registration
     */
    fun hasRegistrationState(): Boolean {
        return registrationStateManager.getRegistrationState() != RegistrationStateManager.STATE_NONE
    }

    /**
     * Get the appropriate start destination based on registration state
     * @return The route name of the screen that should be shown
     */
    fun getStartDestination(): String {
        return when (registrationStateManager.getRegistrationState()) {
            RegistrationStateManager.STATE_WAITING -> "waiting"
            RegistrationStateManager.STATE_APPROVED -> "accepted"
            RegistrationStateManager.STATE_REJECTED -> "rejected"
            else -> "splash" // Default start destination
        }
    }
}