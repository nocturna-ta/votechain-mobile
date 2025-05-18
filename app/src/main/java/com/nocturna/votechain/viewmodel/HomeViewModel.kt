package com.nocturna.votechain.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.decode.DataSource
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.nocturna.votechain.data.model.NewsItem
import com.nocturna.votechain.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "HomeViewModel"
    private val newsRepository = NewsRepository()
    private val context = getApplication<Application>()

    private val _latestNews = MutableStateFlow<List<NewsItem>>(emptyList())
    val latestNews: StateFlow<List<NewsItem>> = _latestNews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Custom image loader with proper caching
    private val imageLoader = ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizePercent(0.02)
                .build()
        }
        .respectCacheHeaders(false)
        .build()

    init {
        fetchLatestNews()
    }

    /**
     * Fetch the latest news from the repository
     */
    fun fetchLatestNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d(TAG, "Fetching latest news...")

            newsRepository.getLatestNews().collect { result ->
                _isLoading.value = false
                result.fold(
                    onSuccess = { news ->
                        Log.d(TAG, "Successfully fetched ${news.size} news items")
                        _latestNews.value = news
                        // Preload images
                        preloadImages(news)
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Error fetching news: ${e.message}", e)
                        _error.value = e.message ?: "Unknown error occurred"
                    }
                )
            }
        }
    }

    /**
     * Preload news images to cache for smoother UI experience
     */
    private fun preloadImages(newsItems: List<NewsItem>) {
        viewModelScope.launch {
            newsItems.forEach { newsItem ->
                try {
                    // Get the direct file_foto URL
                    val imageUrl = "https://www.kpu.go.id/images/${newsItem.file_foto}"

                    Log.d(TAG, "Preloading image: $imageUrl")

                    val request = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()

                    val result = imageLoader.execute(request)
                    if (result is SuccessResult) {
                        val source = when (result.dataSource) {
                            DataSource.MEMORY -> "memory cache"
                            DataSource.DISK -> "disk cache"
                            DataSource.NETWORK -> "network"
                            else -> "unknown"
                        }
                        Log.d(TAG, "Preloaded image from $source: ${newsItem.foto_thumb}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to preload image: ${newsItem.foto_thumb}", e)
                }
            }
        }
    }

    /**
     * Factory for creating HomeViewModel with application context
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}