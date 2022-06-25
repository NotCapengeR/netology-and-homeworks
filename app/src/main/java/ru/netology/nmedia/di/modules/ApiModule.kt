package ru.netology.nmedia.di.modules

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.network.post_api.dto.PostRequest
import ru.netology.nmedia.network.post_api.service.PostService
import ru.netology.nmedia.network.youtube.ApiService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(includes = [NetworkModule::class])
class ApiModule {

    private companion object {
        private const val BASE_URL: String = "https://www.googleapis.com/youtube/v3/"
    }

    @Provides
    @Singleton
    fun provideApiService(
        gson: GsonConverterFactory,
        client: OkHttpClient
    ): ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(gson)
        .client(client)
        .build()
        .create(ApiService::class.java)

    @Provides
    @Singleton
    fun providePostService(
        gson: GsonConverterFactory,
        client: OkHttpClient
    ): PostService = Retrofit.Builder()
        .baseUrl(PostService.BASE_URL)
        .addConverterFactory(gson)
        .client(client)
        .build()
        .create(PostService::class.java)

}

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideGSONConverterFactory(): GsonConverterFactory =
        GsonConverterFactory.create(GsonBuilder().create())

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

}