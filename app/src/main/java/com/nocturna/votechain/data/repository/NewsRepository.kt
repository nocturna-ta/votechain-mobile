package com.nocturna.votechain.data.repository

import com.nocturna.votechain.data.model.NewsItem
import com.nocturna.votechain.data.model.NewsResponse
import com.nocturna.votechain.data.network.KpuNetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API interface for KPU news endpoints
 */
interface NewsApiService {
    @GET("blogs/{categoryId}")
    suspend fun getNews(@Path("categoryId") categoryId: Int): NewsResponse
}

/**
 * Repository for fetching news data from KPU API
 */
class NewsRepository {
    private val apiService = KpuNetworkClient.retrofit.create(NewsApiService::class.java)

    /**
     * Get latest news from the API
     * @param categoryId The category ID of news (104 for Berita Terkini)
     * @param limit Number of news items to retrieve
     * @return Flow of news items result
     */
    fun getLatestNews(categoryId: Int = 104, limit: Int = 7): Flow<Result<List<NewsItem>>> = flow {
        try {
            val response = apiService.getNews(categoryId)
            if (response.success) {
                // Get only the requested number of items (most recent)
                val latestNews = response.data.data.take(limit)
                emit(Result.success(latestNews))
            } else {
                emit(Result.failure(Exception("Failed to fetch news")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}