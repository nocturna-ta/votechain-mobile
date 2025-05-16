package com.nocturna.votechain.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.nocturna.votechain.navigation.VotechainNavGraph
import com.nocturna.votechain.ui.theme.VotechainTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Uncomment this line to test API integration
        // testApiConnection()

        setContent {
            VotechainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    VotechainNavGraph(navController = navController)
                }
            }
        }
    }

    /**
     * Test API connection with KTP file upload
     * This function opens a file picker to select a KTP file,
     * then uses that file to test the registration API
     */
//    private fun testApiConnection() {
//        // Create intent to pick a file
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//            addCategory(Intent.CATEGORY_OPENABLE)
//            type = "*/*" // Accept any file type for testing
//        }
//
//        // Launch file picker
//        ktpFileLauncher.launch(intent)
//    }

    /**
     * File picker launcher
     * This handles the result from the file picker and calls testApiWithFile with the selected file URI
     */
//    private val ktpFileLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val uri = result.data?.data
//            if (uri != null) {
//                // Test API with the selected file
//                testApiWithFile(uri)
//            } else {
//                Toast.makeText(this, "Failed to select file", Toast.LENGTH_LONG).show()
//            }
//        }
//    }

    /**
     * Test API with the selected KTP file
     * @param fileUri URI of the selected KTP file
     */
//    private fun testApiWithFile(fileUri: Uri) {
//        val apiTest = ApiImplementationTest(this)
//
//        // Test API connection in a coroutine
//        lifecycleScope.launch {
//            // Show loading toast
//            withContext(Dispatchers.Main) {
//                Toast.makeText(this@MainActivity, "Testing API connection...", Toast.LENGTH_SHORT).show()
//            }
//
//            // Perform API test on IO thread
//            withContext(Dispatchers.IO) {
//                val isSuccess = apiTest.testApiRegistrationWithKtp(fileUri)
//
//                // Show result on Main thread
//                withContext(Dispatchers.Main) {
//                    if (isSuccess) {
//                        Toast.makeText(this@MainActivity, "API Connection Successful", Toast.LENGTH_LONG).show()
//                    } else {
//                        Toast.makeText(this@MainActivity, "API Connection Failed", Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//        }
//    }
}

