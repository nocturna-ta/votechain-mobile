package com.nocturna.votechain.data.repository

import com.nocturna.votechain.data.model.VisionMissionModel

// Repository interface for accessing vision & mission data
interface VisionMissionRepository {
    suspend fun getVisionMission(candidateNumber: Int): VisionMissionModel
}

// Implementation using mock data (to be replaced with API integration)
class VisionMissionRepositoryImpl : VisionMissionRepository {
    override suspend fun getVisionMission(candidateNumber: Int): VisionMissionModel {
        // This is a temporary implementation with dummy data
        // In a real app, this would make an API call to fetch data from a backend
        // For example:
        // return apiService.getVisionMission(candidateNumber)

        return when(candidateNumber) {
            1 -> VisionMissionModel(
                candidateNumber = 1,
                vision = "Indonesia Adil Makmur Untuk Semua",
                missions = listOf(
                    "Memastikan Ketersediaan Kebutuhan Pokok dan Biaya Hidup Murah melalui Kemandirian Pangan, Ketahanan Energi, dan Kedaulatan Air.",
                    "Mengentaskan Kemiskinan dengan Memperluas Kesempatan Berusaha dan Menciptakan Lapangan Kerja, Mewujudkan Upah Berkeadilan, Menjamin Kemajuan Ekonomi Berbasis Kemandirian dan Pemerataan, serta Mendukung Korporasi Indonesia Berhasil di Negeri Sendiri dan Bertumbuh di Kancah Global.",
                    "Mewujudkan Keadilan Ekologis Berkelanjutan untuk Generasi Mendatang.",
                    "Membangun Kota dan Desa Berbasis Kawasan yang Manusiawi, Berkeadilan dan Saling Memajukan.",
                    "Mewujudkan Manusia Indonesia yang Sehat, Cerdas, Produktif, Berakhlak, dan Berbudaya.",
                    "Mewujudkan Keluarga Indonesia yang Sejahtera dan Bahagia sebagai Akar Kekuatan Bangsa.",
                    "Memperkuat Sistem Pertahanan dan Keamanan Negara, serta Meningkatkan Peran dan Kepemimpinan Indonesia dalam Arena Politik Global untuk Mewujudkan Kepentingan Nasional dan Perdamaian Dunia.",
                    "Memulihkan Kualitas Demokrasi, Menegakkan Hukum dan HAM, Memberantas Korupsi Tanpa Tebang Pilih, serta Menyelenggarakan Pemerintahan yang Berpihak pada Rakyat."
                )
            )
            2 -> VisionMissionModel(
                candidateNumber = 2,
                vision = "Indonesia Maju, Berdaulat, Mandiri, dan Berbudaya",
                missions = listOf(
                    "Mempercepat pembangunan ekonomi yang berdaulat dan mandiri dengan menekankan transformasi digital.",
                    "Memperkuat sistem pertahanan dan keamanan nasional untuk melindungi kedaulatan negara.",
                    "Meningkatkan kualitas pendidikan dan pengembangan sumber daya manusia untuk menciptakan tenaga kerja yang kompetitif.",
                    "Memperkuat identitas nasional dan melestarikan budaya Indonesia sebagai fondasi pembangunan.",
                    "Melanjutkan pembangunan infrastruktur yang merata di seluruh wilayah Indonesia.",
                    "Menjamin ketahanan pangan dan energi melalui kebijakan yang berkelanjutan.",
                    "Mereformasi birokrasi dan meningkatkan tata kelola pemerintahan yang bersih dan efisien."
                )
            )
            3 -> VisionMissionModel(
                candidateNumber = 3,
                vision = "Indonesia Sejahtera untuk Semua",
                missions = listOf(
                    "Menciptakan ketahanan ekonomi melalui kebijakan yang inklusif dan berkelanjutan.",
                    "Meningkatkan kesejahteraan masyarakat dengan memperluas akses terhadap layanan kesehatan yang berkualitas.",
                    "Memastikan pemerataan akses pendidikan berkualitas di seluruh Indonesia.",
                    "Menjamin keadilan sosial dan perlindungan hukum bagi seluruh warga negara tanpa diskriminasi.",
                    "Mengoptimalkan pemanfaatan sumber daya alam dengan memperhatikan kelestarian lingkungan.",
                    "Memperkuat demokrasi dan membangun tata kelola pemerintahan yang transparan dan akuntabel.",
                    "Mengembangkan nilai-nilai toleransi dan keragaman budaya sebagai kekuatan bangsa Indonesia.",
                    "Membangun hubungan internasional yang strategis untuk mendukung kepentingan nasional."
                )
            )
            else -> throw IllegalArgumentException("Data for candidate number $candidateNumber not available")
        }
    }
}
