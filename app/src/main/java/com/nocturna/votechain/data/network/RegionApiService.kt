package com.nocturna.votechain.data.network

import com.nocturna.votechain.data.model.ProvinceResponse
import com.nocturna.votechain.data.model.RegencyResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface WilayahApiService {
    @GET("provinces.json")
    suspend fun getProvinces(): ProvinceResponse

    @GET("regencies/{provinceCode}.json")
    suspend fun getRegencies(@Path("provinceCode") provinceCode: String): RegencyResponse
}