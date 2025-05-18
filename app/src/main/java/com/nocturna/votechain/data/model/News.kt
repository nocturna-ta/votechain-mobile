package com.nocturna.votechain.data.model

data class NewsItem(
    val id: Int,
    val blog_name: String,
    val file_foto: String,     // Added this field
    val foto_thumb: String,
    val post_slug: String,
    val created_at: String
)

data class NewsResponse(
    val success: Boolean,
    val data: NewsData
)

data class NewsData(
    val current_page: Int,
    val data: List<NewsItem>
)