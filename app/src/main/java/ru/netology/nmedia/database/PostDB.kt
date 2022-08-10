package ru.netology.nmedia.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nmedia.database.PostDB.Companion.DB_VERSION
import ru.netology.nmedia.database.dao.DeletedPostDAO
import ru.netology.nmedia.database.dao.PostDAO
import ru.netology.nmedia.database.entities.DeletedPostEntity
import ru.netology.nmedia.database.entities.PostEntity

@Database(
    entities = [PostEntity::class, DeletedPostEntity::class],
    version = DB_VERSION,
    exportSchema = true)
abstract class PostDB : RoomDatabase() {

    abstract fun getPostDao(): PostDAO

    abstract fun getPostDeletedDao(): DeletedPostDAO

    companion object {
        const val DB_VERSION: Int = 8
        const val DB_NAME: String = "posts-database"
    }
}