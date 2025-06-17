package com.nocturna.votechain.data.model

object DummyData {
    val activeVotingCategories = listOf(
        VotingCategory(
            id = "1",
            title = "2024 Presidential Election - Indonesia",
            description = "Choose the leaders you trust to guide Indonesia's future",
            isActive = true
        )
    )

    val emptyVotingCategories = emptyList<VotingCategory>()

    val votingResults = listOf(
        VotingResult(
            categoryId = "4",
            categoryTitle = "Last Year's Presidential Election",
            options = listOf(
                VotingOption(
                    id = "opt1",
                    name = "Candidate A",
                    votes = 2500,
                    percentage = 0.45f
                ),
                VotingOption(
                    id = "opt2",
                    name = "Candidate B",
                    votes = 3000,
                    percentage = 0.55f
                )
            ),
            totalVotes = 5500
        ),
        VotingResult(
            categoryId = "5",
            categoryTitle = "Regional Governor Election",
            options = listOf(
                VotingOption(
                    id = "opt1",
                    name = "Governor 1",
                    votes = 1500,
                    percentage = 0.33f
                ),
                VotingOption(
                    id = "opt2",
                    name = "Governor 2",
                    votes = 2000,
                    percentage = 0.44f
                ),
                VotingOption(
                    id = "opt3",
                    name = "Governor 3",
                    votes = 1000,
                    percentage = 0.23f
                )
            ),
            totalVotes = 4500
        )
    )
}