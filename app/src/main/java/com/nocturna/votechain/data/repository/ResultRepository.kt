package com.nocturna.votechain.data.repository

import com.nocturna.votechain.data.model.VotingOption
import com.nocturna.votechain.data.model.VotingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Repository for detailed voting results.
 * This will eventually connect to the API, but uses dummy data for now.
 */
class DetailedResultRepository {

    /**
     * Get detailed results for a specific voting category.
     * When API is ready, this will make an API call instead of returning dummy data.
     */
    fun getDetailedResults(categoryId: String, regionCode: String? = null): Flow<Result<VotingResult>> = flow {
        try {
            // Simulate API delay
            delay(800)

            // In the future, this would be an API call like:
            // val response = apiService.getDetailedResults(categoryId, regionCode)
            // if (response.isSuccessful) {
            //     emit(Result.success(response.body()!!))
            // } else {
            //     emit(Result.failure(Exception("API Error: ${response.code()}")))
            // }

            // For now, return dummy data based on categoryId
            val dummyResult = getDummyDetailedResult(categoryId, regionCode)
            emit(Result.success(dummyResult))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get available regions for a specific voting result.
     * Will be replaced with actual API call in the future.
     */
    fun getAvailableRegions(categoryId: String): Flow<Result<List<Region>>> = flow {
        try {
            // Simulate API delay
            delay(500)

            // For now, return dummy regions
            val regions = getDummyRegions()
            emit(Result.success(regions))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Dummy data for development purposes.
     * This will be removed when the API is ready.
     */
    private fun getDummyDetailedResult(categoryId: String, regionCode: String?): VotingResult {
        // Different dummy data based on region to simulate filtering
        val regionSpecificData = when(regionCode) {
            "JKT" -> {
                // Jakarta data - different percentages
                VotingResult(
                    categoryId = categoryId,
                    categoryTitle = "2024 Presidential Election - Indonesia",
                    options = listOf(
                        VotingOption(id = "candidate_1", name = "Anies Baswedan", votes = 3200000, percentage = 0.62f),
                        VotingOption(id = "candidate_2", name = "Prabowo Subianto", votes = 1500000, percentage = 0.29f),
                        VotingOption(id = "candidate_3", name = "Ganjar Pranowo", votes = 450000, percentage = 0.09f)
                    ),
                    totalVotes = 5150000
                )
            }
            "WJV" -> {
                // West Java data
                VotingResult(
                    categoryId = categoryId,
                    categoryTitle = "2024 Presidential Election - Indonesia",
                    options = listOf(
                        VotingOption(id = "candidate_1", name = "Anies Baswedan", votes = 6500000, percentage = 0.55f),
                        VotingOption(id = "candidate_2", name = "Prabowo Subianto", votes = 3800000, percentage = 0.32f),
                        VotingOption(id = "candidate_3", name = "Ganjar Pranowo", votes = 1550000, percentage = 0.13f)
                    ),
                    totalVotes = 11850000
                )
            }
            "BAL" -> {
                // Bali data - very different from national average
                VotingResult(
                    categoryId = categoryId,
                    categoryTitle = "2024 Presidential Election - Indonesia",
                    options = listOf(
                        VotingOption(id = "candidate_1", name = "Anies Baswedan", votes = 320000, percentage = 0.18f),
                        VotingOption(id = "candidate_2", name = "Prabowo Subianto", votes = 480000, percentage = 0.27f),
                        VotingOption(id = "candidate_3", name = "Ganjar Pranowo", votes = 980000, percentage = 0.55f)
                    ),
                    totalVotes = 1780000
                )
            }
            else -> {
                // National data (default)
                VotingResult(
                    categoryId = categoryId,
                    categoryTitle = "2024 Presidential Election - Indonesia",
                    options = listOf(
                        VotingOption(id = "candidate_1", name = "Anies Baswedan", votes = 38500000, percentage = 0.52f),
                        VotingOption(id = "candidate_2", name = "Prabowo Subianto", votes = 20100000, percentage = 0.27f),
                        VotingOption(id = "candidate_3", name = "Ganjar Pranowo", votes = 15600000, percentage = 0.21f)
                    ),
                    totalVotes = 74200000
                )
            }
        }

        // For Presidential Election specifically, use data that matches the design
        // with the exact percentages shown in the screenshot (67%, 27%, 21%)
        return if (categoryId == "4" && regionCode == null) {
            VotingResult(
                categoryId = categoryId,
                categoryTitle = "2024 Presidential Election - Indonesia",
                options = listOf(
                    VotingOption(id = "candidate_1", name = "Anies Baswedan", votes = 49590000, percentage = 0.67f),
                    VotingOption(id = "candidate_2", name = "Prabowo Subianto", votes = 19980000, percentage = 0.27f),
                    VotingOption(id = "candidate_3", name = "Ganjar Pranowo", votes = 15540000, percentage = 0.21f)
                ),
                totalVotes = 74000000
            )
        } else {
            regionSpecificData
        }
    }

    /**
     * Dummy regions for development purposes.
     */
    private fun getDummyRegions(): List<Region> {
        return listOf(
            Region("ALL", "All Regions"),
            Region("JKT", "Jakarta"),
            Region("WJV", "West Java"),
            Region("CJV", "Central Java"),
            Region("EJV", "East Java"),
            Region("BAL", "Bali"),
            Region("SUM", "Sumatra"),
            Region("KAL", "Kalimantan"),
            Region("SUL", "Sulawesi"),
            Region("PAP", "Papua")
        )
    }
}

/**
 * Data class for representing regions.
 * Will match the API response format in the future.
 */
data class Region(
    val code: String,
    val name: String
)