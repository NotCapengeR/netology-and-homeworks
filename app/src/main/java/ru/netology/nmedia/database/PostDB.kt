package ru.netology.nmedia.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nmedia.database.PostDB.Companion.DB_VERSION
import ru.netology.nmedia.database.dao.PostDAO
import ru.netology.nmedia.database.entities.PostEntity

@Database(entities = [PostEntity::class], version = DB_VERSION, exportSchema = true)
abstract class PostDB : RoomDatabase() {

    abstract fun getDao(): PostDAO

    companion object {
        const val DB_VERSION: Int = 3
        const val DB_NAME: String = "posts-database"
    }

}