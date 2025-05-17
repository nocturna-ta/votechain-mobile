package com.nocturna.votechain.ui.screens.homepage

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.NewsItem
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.DangerColors
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeCarousel(
    viewModel: HomeViewModel = viewModel(),
    onNewsClick: (String) -> Unit = {}
) {
    val latestNews by viewModel.latestNews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MainColors.Primary1)
        }
    } else if (error != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error loading news: ${error ?: "Unknown error"}",
                color = DangerColors.Danger50
            )
        }
    } else if (latestNews.isEmpty()) {
        // Fallback to default carousel items if no news is available
        DefaultCarousel()
    } else {
        // News Carousel
        val pagerState = rememberPagerState(pageCount = { latestNews.size })
        val coroutineScope = rememberCoroutineScope()

        // Auto-scroll effect
        LaunchedEffect(key1 = Unit) {
            while (true) {
                delay(3000) // Change slide every 3 seconds
                coroutineScope.launch {
                    val nextPage = (pagerState.currentPage + 1) % latestNews.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val newsItem = latestNews[page]
                NewsCarouselItemContent(
                    newsItem = newsItem,
                    onClick = { onNewsClick(newsItem.post_slug) }
                )
            }

            // Carousel Indicators
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .align(Alignment.BottomCenter),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(latestNews.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) MainColors.Primary1
                                    else NeutralColors.Neutral30.copy(alpha = 0.7f)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCarouselItemContent(
    newsItem: NewsItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
    ) {
        // Background Image - URL encoding the thumbnail filename to handle spaces and special characters
        val encodedThumb = Uri.encode(newsItem.foto_thumb)
        val imageUrl = "https://www.kpu.go.id/images/blogs/$encodedThumb"

        AsyncImage(
            model = imageUrl,
            contentDescription = newsItem.blog_name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_background), // Fallback image if loading fails
            placeholder = painterResource(id = R.drawable.ic_launcher_background) // Placeholder while loading
        )

        // Gradient overlay for better text visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 300f
                    )
                )
        )

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.85f) // Limit the width of the text column
            ) {
                Text(
                    text = newsItem.blog_name,
                    style = AppTypography.heading3Bold,
                    color = NeutralColors.Neutral10,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainColors.Primary1
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("More Details")
                }
            }
        }
    }
}

// Original HomeCarousel implementation as fallback
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DefaultCarousel() {
    val carouselItems = listOf(
        CarouselItem(
            imageRes = R.drawable.ic_launcher_background,
            title = "Presidential Election 2024",
            description = "Your vote matters! Participate in the upcoming presidential election."
        ),
        CarouselItem(
            imageRes = R.drawable.ic_launcher_background,
            title = "Regional Elections",
            description = "Local leadership affects your daily life. Make your voice heard!"
        ),
        CarouselItem(
            imageRes = R.drawable.ic_launcher_background,
            title = "Secure Blockchain Voting",
            description = "Experience safe and transparent elections with VoteChain technology."
        )
    )

    val pagerState = rememberPagerState(pageCount = { carouselItems.size })
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll effect
    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(3000) // Change slide every 3 seconds
            coroutineScope.launch {
                val nextPage = (pagerState.currentPage + 1) % carouselItems.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = carouselItems[page]
//            CarouselItemContent(item)
        }

        // Overlay gradient and text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.85f) // Limit the width of the text column
            ) {
                Text(
                    text = carouselItems[pagerState.currentPage].title,
                    style = AppTypography.heading3Bold,
                    color = NeutralColors.Neutral10,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = carouselItems[pagerState.currentPage].description,
                    style = AppTypography.heading5Medium,
                    color = NeutralColors.Neutral10,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Carousel Indicators
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(carouselItems.size) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) MainColors.Primary1
                            else NeutralColors.Neutral30.copy(alpha = 0.7f)
                        )
                )
            }
        }
    }
}

data class CarouselItem(
    val imageRes: Int,
    val title: String,
    val description: String
)