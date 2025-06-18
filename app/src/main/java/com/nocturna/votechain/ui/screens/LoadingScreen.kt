package com.nocturna.votechain.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.nocturna.votechain.R

@Composable
fun LoadingScreen(
    onClose: () -> Unit = {}
) {
    // Context for Coil ImageLoader
    val context = LocalContext.current

    // Configure ImageLoader with GIF support
    val imageLoader = ImageLoader.Builder(context)
        .components {
            // Add decoder for GIF based on Android version
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        // Display GIF of ballot box using Coil
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(R.drawable.loading)
                    .build(),
                imageLoader = imageLoader
            ),
            contentDescription = "Loading Animation",
            modifier = Modifier.size(140.dp),
            contentScale = ContentScale.Fit
        )
    }
}