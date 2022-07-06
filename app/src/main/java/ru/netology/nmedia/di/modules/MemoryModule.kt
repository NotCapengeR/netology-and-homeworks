package ru.netology.nmedia.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.netology.nmedia.App
import ru.netology.nmedia.database.PostDB
import ru.netology.nmedia.database.PostDB.Companion.DB_NAME
import ru.netology.nmedia.database.dao.PostDAO
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.repository.RemotePostSource
import ru.netology.nmedia.repository.RemotePostSourceImpl
import ru.netology.nmedia.repository.dto.Post.Companion.POST_ID
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
    fun provideDB(context: Context): PostDB = Room.databaseBuilder(
        context,
        PostDB::class.java,
        DB_NAME
    ).fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun providePostDAO(db: PostDB): PostDAO = db.getDao()
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
    fun bindRemotePostSource(remotePostSourceImpl: RemotePostSourceImpl): RemotePostSource

}

