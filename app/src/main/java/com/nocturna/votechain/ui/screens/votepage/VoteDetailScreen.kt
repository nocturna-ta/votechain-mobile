// VotingDetailScreen.kt
package com.example.votingapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nocturna.votechain.data.model.DummyData
import com.nocturna.votechain.viewmodel.vote.VotingViewModel

data class VotingOption(val id: String, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VotingDetailScreen(
    categoryId: String,
    navController: NavController,
    viewModel: VotingViewModel
) {
    // Find voting category from dummy data
    val category = DummyData.activeVotingCategories.find { it.id == categoryId }

    // For demo purposes, create some options
    val options = listOf(
        VotingOption("opt1", "Candidate A"),
        VotingOption("opt2", "Candidate B"),
        VotingOption("opt3", "Candidate C")
    )

    var selectedOption by remember { mutableStateOf<String?>(null) }
    var hasVoted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category?.title ?: "Voting") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = category?.title ?: "Voting Details",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category?.description ?: "Select your preferred option",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (hasVoted) {
                Text(
                    text = "Thank you for voting!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Back to Voting List")
                }
            } else {
                Text(
                    text = "Select your choice:",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedOption == option.id,
                                    onClick = { selectedOption = option.id },
                                    role = Role.RadioButton
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption == option.id,
                                onClick = null
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        selectedOption?.let { optionId ->
                            viewModel.submitVote(categoryId, optionId)
                            hasVoted = true
                        }
                    },
                    enabled = selectedOption != null,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Submit Vote")
                }
            }
        }
    }
}