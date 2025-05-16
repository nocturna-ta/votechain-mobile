package com.nocturna.votechain.domain

import com.nocturna.votechain.data.model.CandidateDetailData
import com.nocturna.votechain.data.repository.CandidateRepository

/**
 * Use case for retrieving candidate detail data
 * Use cases implement business logic between the data layer and presentation layer
 */
class GetCandidateDetail(
    private val repository: CandidateRepository
) {
    /**
     * Execute the use case to get candidate detail by ID
     */
    suspend operator fun invoke(candidateId: String): CandidateDetailData {
        return repository.getCandidateById(candidateId)
    }
}