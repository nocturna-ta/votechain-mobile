//package com.nocturna.votechain.ui.screens.votepage
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.nocturna.votechain.ui.screens.votepage.LiveResultScreen
//
///**
// * Example of how to integrate live results into existing results screen
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EnhancedResultsScreen(categoryId: String) {
//    var showLiveResults by remember { mutableStateOf(true) }
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        // Toggle between live and static results
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            FilterChip(
//                onClick = { showLiveResults = true },
//                label = { Text("Live Results") },
//                selected = showLiveResults
//            )
//            FilterChip(
//                onClick = { showLiveResults = false },
//                label = { Text("Final Results") },
//                selected = !showLiveResults
//            )
//        }
//
//        // Display appropriate content
//        if (showLiveResults) {
//            LiveResultsScreen(categoryId = categoryId)
//        } else {
//            // Your existing static results screen
//            // StaticResultsScreen(categoryId = categoryId)
//        }
//    }
//}