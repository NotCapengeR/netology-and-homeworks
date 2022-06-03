package ru.netology.nmedia.di.modules

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import ru.netology.nmedia.App
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import javax.inject.Singleton

@Module(includes = [RepositoryModule::class])
interface AppModule {

    @Binds
    fun bindContext(application: App): Context

    @Binds
    @Singleton
    fun bindApp(application: App): Application
}

@Module
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindPostRepository(postRepository: PostRepositoryImpl): PostRepository
}