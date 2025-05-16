package com.nocturna.votechain.utils

data class LocalizedStrings(
    val settings: String,
    val darkMode: String,
    val language: String,
    val about: String,
    val faq: String,
    val voteComplete: String,
    val view: String,
    val home: String,
    val votes: String,
    val profile: String
)

fun getLocalizedStrings(language: String): LocalizedStrings {
    return when (language) {
        "Indonesia" -> LocalizedStrings(
            settings = "Pengaturan",
            darkMode = "Mode Gelap",
            language = "Bahasa",
            about = "Tentang",
            faq = "Pertanyaan Umum",
            voteComplete = "Sudah Memilih",
            view = "Lihat",
            home = "Beranda",
            votes = "Voting",
            profile = "Profil"
        )
        else -> LocalizedStrings( // Default English
            settings = "Settings",
            darkMode = "Dark Mode",
            language = "Language",
            about = "About",
            faq = "FAQ",
            voteComplete = "Vote Complete",
            view = "View",
            home = "Home",
            votes = "Votes",
            profile = "Profile"
        )
    }
}