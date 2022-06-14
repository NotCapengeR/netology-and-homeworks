package ru.netology.nmedia.di.modules

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nmedia.network.ApiService
import javax.inject.Singleton

@Module
class ApiModule {

    private companion object {
        private const val BASE_URL: String = "https://www.googleapis.com/youtube/v3/"
    }

    @Provides
    @Singleton
    fun provideApiService(): ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

}