package com.nocturna.votechain.data.model

data class Province(
    val code: String,
    val name: String
)

data class Regency(
    val code: String,
    val name: String
)

data class ProvinceResponse(
    val data: List<com.nocturna.votechain.data.model.Province>
)

data class RegencyResponse(
    val data: List<Regency>
)
