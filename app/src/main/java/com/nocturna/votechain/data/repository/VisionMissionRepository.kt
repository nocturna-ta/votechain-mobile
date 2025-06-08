package com.nocturna.votechain.data.repository

import com.nocturna.votechain.data.model.VisionMissionModel
import com.nocturna.votechain.data.model.VisionMissionDetailModel
import com.nocturna.votechain.data.model.WorkProgram
import com.nocturna.votechain.data.network.ElectionApiService
import javax.inject.Inject

// Updated Repository interface
interface VisionMissionRepository {
    suspend fun getVisionMission(candidateNumber: Int): VisionMissionModel // Keep for backward compatibility
    suspend fun getVisionMissionFromAPI(pairId: String): VisionMissionDetailModel // New method
}

// Updated Implementation with API integration
class VisionMissionRepositoryImpl @Inject constructor(
    private val apiService: ElectionApiService? = null // Make it optional for backward compatibility
) : VisionMissionRepository {

    // Keep old method for backward compatibility
    override suspend fun getVisionMission(candidateNumber: Int): VisionMissionModel {
        return when(candidateNumber) {
            1 -> VisionMissionModel(
                candidateNumber = 1,
                vision = "Indonesia Adil Makmur Untuk Semua",
                missions = listOf(
                    "Memastikan Ketersediaan Kebutuhan Pokok dan Biaya Hidup Murah melalui Kemandirian Pangan, Ketahanan Energi, dan Kedaulatan Air.",
                    "Mengentaskan Kemiskinan dengan Memperluas Kesempatan Berusaha dan Menciptakan Lapangan Kerja, Mewujudkan Upah Berkeadilan, Menjamin Kemajuan Ekonomi Berbasis Kemandirian dan Pemerataan, serta Mendukung Korporasi Indonesia Berhasil di Negeri Sendiri dan Bertumbuh di Kancah Global."
                )
            )
            else -> VisionMissionModel(
                candidateNumber = candidateNumber,
                vision = "Vision not available",
                missions = listOf("Mission not available")
            )
        }
    }

    // New method for API integration
    override suspend fun getVisionMissionFromAPI(pairId: String): VisionMissionDetailModel {
        if (apiService == null) {
            throw Exception("API service not available")
        }

        val response = apiService.getVisionMissionDetail(pairId)

        if (response.isSuccessful && response.body()?.code == 0) {
            val data = response.body()?.data
            return VisionMissionDetailModel(
                electionPairId = data?.election_pair_id ?: "",
                id = data?.id ?: "",
                vision = data?.vision ?: "",
                mission = data?.mission ?: "",
                programDocs = data?.program_docs,
                workPrograms = data?.work_program?.map { workProgram ->
                    WorkProgram(
                        programName = workProgram.program_name,
                        programDesc = workProgram.program_desc,
                        programPhoto = workProgram.program_photo
                    )
                } ?: emptyList()
            )
        } else {
            throw Exception(response.body()?.error?.error_message ?: "Failed to fetch data")
        }
    }
}
