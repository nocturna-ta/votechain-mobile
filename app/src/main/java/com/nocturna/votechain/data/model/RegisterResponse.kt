package com.nocturna.votechain.data.model

import android.net.Uri

data class RegisterResponse(
    val email: String,
    val password: String,
    val role: String,
    val ethereumAddress: String,
    val nik: String = "",
    val fullName: String = "",
    val gender: String = "",
    val birthPlace: String = "",
    val birthDate: String = "",
    val residentialAddress: String = "",
    val kpuName: String = "",
    val region: String = "",
    val ktpFileUri: Uri? = null
)