package ru.netology.nmedia.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.netology.nmedia.App
import ru.netology.nmedia.database.PostDAO
import ru.netology.nmedia.database.PostDAOImpl
import ru.netology.nmedia.database.PostDB
import ru.netology.nmedia.dto.Post.Companion.POST_ID
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import javax.inject.Singleton

@Module(includes = [AppModule::class, RepositoryModule::class])
class MemoryModule {

    @Provides
    @Singleton
    fun provideSharedPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(POST_ID, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideDB(db: PostDB): SQLiteDatabase = db.writableDatabase
}

@Module
class AppModule(private val application: App) {

    @Provides
    @Singleton
    fun provideContext(): Context = application

    @Provides
    @Singleton
    fun provideApp(): Application = application
}

@Module
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindPostRepository(postRepository: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    fun bindPostDAO(postDAO: PostDAOImpl): PostDAO
}

