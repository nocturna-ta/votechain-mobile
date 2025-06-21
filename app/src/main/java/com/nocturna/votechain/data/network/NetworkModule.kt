package com.nocturna.votechain.data.network

import androidx.test.espresso.core.internal.deps.dagger.Module
import androidx.test.espresso.core.internal.deps.dagger.Provides
import com.nocturna.votechain.data.repository.LiveResultRepository
import javax.inject.Singleton

@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideLiveResultsWebSocketManager(): LiveResultsWebSocketManager {
        return LiveResultsWebSocketManager()
    }

    @Provides
    @Singleton
    fun provideLiveResultRepository(
        webSocketManager: LiveResultsWebSocketManager
    ): LiveResultRepository {
        return LiveResultRepository()
    }
}