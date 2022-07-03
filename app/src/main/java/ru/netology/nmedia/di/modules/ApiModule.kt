package ru.netology.nmedia.di.modules

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nmedia.network.post_api.service.PostService
import ru.netology.nmedia.network.youtube.ApiService
import timber.log.Timber
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
        client: OkHttpClient,
    ): PostService = Retrofit.Builder()
        .baseUrl(PostService.BASE_URL)
        .addConverterFactory(gson)
        .client(client)
        .build()
        .create(PostService::class.java)

}

@Module(includes = [FirebaseModule::class])
class NetworkModule {

    @Provides
    @Singleton
    fun provideGSONConverterFactory(): GsonConverterFactory =
        GsonConverterFactory.create(GsonBuilder().create())

    @Provides
    @Singleton
    fun provideHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()


    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor { message ->
        Timber.tag("Retrofit").d(message)
    }.setLevel(HttpLoggingInterceptor.Level.BODY)
}

@Module
class FirebaseModule {

    companion object {
        const val FIREBASE_TAG: String = "firebase"
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseApp(): FirebaseApp = FirebaseApp.getInstance()
}