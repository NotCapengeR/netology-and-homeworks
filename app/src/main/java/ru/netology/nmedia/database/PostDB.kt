package ru.netology.nmedia.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostDB @Inject constructor(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_LITE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_LITE_DELETE)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        const val DATABASE_NAME: String = "Posts.db"
        private const val DATABASE_VERSION: Int = 6
        private const val SQL_LITE_CREATE: String =
            "CREATE TABLE IF NOT EXISTS ${PostEntry.TABLE_NAME} (" +
                    "${PostEntry.COLUMN_NAME_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${PostEntry.COLUMN_NAME_TITLE} TEXT, " +
                    "${PostEntry.COLUMN_NAME_TEXT} TEXT, " +
                    "${PostEntry.COLUMN_NAME_DATE} TEXT, " +
                    "${PostEntry.COLUMN_NAME_AVATAR_ID} INTEGER, " +
                    "${PostEntry.COLUMN_NAME_LIKES_COUNT} INTEGER DEFAULT 0, " +
                    "${PostEntry.COLUMN_NAME_COMMENTS_COUNT} INTEGER DEFAULT 0, " +
                    "${PostEntry.COLUMN_NAME_SHARE_COUNT} INTEGER DEFAULT 0, " +
                    "${PostEntry.COLUMN_NAME_VIEWS_COUNT} INTEGER DEFAULT 0, " +
                    "${PostEntry.COLUMN_NAME_IS_LIKED} TEXT DEFAULT 'FALSE', " +
                    "${PostEntry.COLUMN_NAME_YT_ID} TEXT DEFAULT NULL, " +
                    "${PostEntry.COLUMN_NAME_YT_TITLE} TEXT DEFAULT NULL, " +
                    "${PostEntry.COLUMN_NAME_YT_AUTHOR} TEXT DEFAULT NULL, " +
                    "${PostEntry.COLUMN_NAME_YT_DURATION} TEXT DEFAULT NULL, " +
                    "${PostEntry.COLUMN_NAME_YT_THUMBNAIL_URL} TEXT DEFAULT NULL); "
        private const val SQL_LITE_DELETE: String =
            "DROP TABLE IF EXISTS ${PostEntry.TABLE_NAME};"
        @JvmStatic
        fun parseFromSQLBoolean(boolean: String): Boolean? = when (boolean) {
            "TRUE" -> true
            "FALSE" -> false
            else -> null
        }
    }

    object PostEntry : BaseColumns {
        const val TABLE_NAME = "Posts"
        const val COLUMN_NAME_ID = "post_id"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_TEXT = "text"
        const val COLUMN_NAME_DATE = "date"
        const val COLUMN_NAME_AVATAR_ID = "avatar_id"
        const val COLUMN_NAME_LIKES_COUNT = "likes_count"
        const val COLUMN_NAME_COMMENTS_COUNT = "comments_count"
        const val COLUMN_NAME_SHARE_COUNT = "share_count"
        const val COLUMN_NAME_VIEWS_COUNT = "views_count"
        const val COLUMN_NAME_IS_LIKED = "is_liked"
        const val COLUMN_NAME_YT_ID = "yt_id"
        const val COLUMN_NAME_YT_DURATION = "yt_duration"
        const val COLUMN_NAME_YT_AUTHOR = "yt_author"
        const val COLUMN_NAME_YT_TITLE = "yt_title"
        const val COLUMN_NAME_YT_THUMBNAIL_URL = "yt_thumbnail_url"
    }
}