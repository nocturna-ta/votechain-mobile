package com.nocturna.votechain.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for date formatting
 */
object DateFormatter {

    /**
     * Format date from API format (yyyy-MM-dd) to display format (dd MMMM yyyy)
     * Example: "1969-05-07" -> "07 Mei 1969" (Indonesian) or "07 May 1969" (English)
     */
    fun formatBirthDate(dateString: String, language: String = "Indonesia"): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)

            val locale = if (language == "Indonesia") {
                Locale("id", "ID")
            } else {
                Locale.ENGLISH
            }

            val outputFormat = SimpleDateFormat("dd MMMM yyyy", locale)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            // Return original string if parsing fails
            dateString
        }
    }

    /**
     * Format date for birth info display (place and date combined)
     * Example: "Kuningan, 07 Mei 1969"
     */
    fun formatBirthInfo(birthPlace: String, birthDate: String, language: String = "Indonesia"): String {
        val formattedDate = formatBirthDate(birthDate, language)
        return "$birthPlace, $formattedDate"
    }
}