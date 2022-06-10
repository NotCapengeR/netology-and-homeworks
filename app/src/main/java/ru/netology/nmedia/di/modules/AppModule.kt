package ru.netology.nmedia.di.modules

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.netology.nmedia.App
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.lang.reflect.Type
import javax.inject.Singleton

@Module(includes = [RepositoryModule::class])
class AppModule(private val application: App) {

    @Provides
    @Singleton
    fun provideContext(): Context = application

    @Provides
    @Singleton
    fun provideApp(): Application = application
}

@Module(includes = [MemoryModule::class])
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindPostRepository(postRepository: PostRepositoryImpl): PostRepository
}

@Module
class MemoryModule {

    @Provides
    fun provideGson(): Gson = Gson()

    @Provides
    fun provideType(): Type =
        TypeToken.getParameterized(List::class.java, Post::class.java).type
}