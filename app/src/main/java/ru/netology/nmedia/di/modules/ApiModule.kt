package ru.netology.nmedia.di.modules

import com.google.android.gms.common.GoogleApiAvailability
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
import ru.netology.nmedia.network.youtube.YouTubeService
import ru.netology.nmedia.repository.auth.AuthManager
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(includes = [NetworkModule::class])
class ApiModule {

    @Provides
    @Singleton
    fun provideYouTubeService(
        gson: GsonConverterFactory,
        client: OkHttpClient
    ): YouTubeService = Retrofit.Builder()
        .baseUrl(YouTubeService.BASE_URL)
        .addConverterFactory(gson)
        .client(client)
        .build()
        .create(YouTubeService::class.java)

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

@Module(includes = [FirebaseModule::class])
class NetworkModule {

    @Provides
    @Singleton
    fun provideGSONConverterFactory(): GsonConverterFactory =
        GsonConverterFactory.create(GsonBuilder().create())

    @Provides
    @Singleton
    fun provideHttpClient(
        authManager: AuthManager,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            authManager.authData.value.token?.let { token ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                return@addInterceptor chain.proceed(request)
            }
            chain.proceed(chain.request())
        }
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
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    fun provideFirebaseApp(): FirebaseApp = FirebaseApp.getInstance()

    @Provides
    fun provideGoogleApiAvailability(): GoogleApiAvailability = GoogleApiAvailability.getInstance()
}