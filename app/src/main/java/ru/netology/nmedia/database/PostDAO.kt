package ru.netology.nmedia.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.text.format.DateFormat
import androidx.core.database.getStringOrNull
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.YouTubeVideoData.Companion.buildVideoData
import ru.netology.nmedia.utils.toDateTime
import ru.netology.nmedia.database.PostDB.PostEntry
import ru.netology.nmedia.dto.YouTubeVideoData
import ru.netology.nmedia.utils.toSQL
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface PostDAO {

    fun getAll(): List<Post>

    fun getPostById(id: Long): Post?

    fun deletePost(id: Long): Int

    fun addPost(title: String, text: String): Long

    fun addVideo(postId: Long, video: YouTubeVideoData?): Int

    fun removeVideo(id: Long): Int

    fun editPost(id: Long, newText: String, newTitle: String): Int

    fun likePost(id: Long, previousLikesCount: Int, changed: Boolean): Int

    fun sharePost(id: Long): Int

    fun commentPost(id: Long): Int
}

@Singleton
class PostDAOImpl @Inject constructor(
    private val db: SQLiteDatabase
) : PostDAO {

    override fun getAll(): List<Post> {
        val cursor = db.query(
            PostEntry.TABLE_NAME,
            arrayOf(PostEntry.COLUMN_NAME_ID),
            "${PostEntry.COLUMN_NAME_ID} >= ?",
            arrayOf("0"),
            null,
            null,
            "${PostEntry.COLUMN_NAME_ID} ASC"
        )
        val postsList = mutableListOf<Post>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_ID))
                val post = getPostById(id)
                if (post != null) {
                    postsList.add(post)
                }
            }
            close()
            return postsList
        }
    }

    override fun getPostById(id: Long): Post? {
        val cursor = db.query(
            PostEntry.TABLE_NAME,
            null,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            "${PostEntry.COLUMN_NAME_ID} ASC"
        )
        with(cursor) {
            return if (moveToFirst()) {
                val title = getString(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_TITLE))
                val text = getString(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_TEXT))
                val date = getString(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_DATE))
                    .toDateTime() ?: Timber.e("Post date value is invalid!")
                    .let { Date().time }
                val avatar = getInt(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_AVATAR_ID))
                val likes = getInt(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_LIKES_COUNT))
                val comments =
                    getInt(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_COMMENTS_COUNT))
                val shares = getInt(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_SHARE_COUNT))
                val views = getInt(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_VIEWS_COUNT))
                val isLiked: Boolean = parseFromSQLBoolean(
                    getString(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_IS_LIKED))
                ) ?: Timber.e("Post isLiked value is invalid!").let { false }
                val ytId =
                    getStringOrNull(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_YT_ID))
                val ytAuthor =
                    getStringOrNull(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_YT_AUTHOR))
                val ytTitle =
                    getStringOrNull(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_YT_TITLE))
                val ytDuration =
                    getStringOrNull(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_YT_DURATION))
                val ytUrl =
                    getStringOrNull(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_YT_THUMBNAIL_URL))
                val video = buildVideoData(ytId, ytAuthor, ytTitle, ytDuration, ytUrl)
                Timber.d("YT video id in repo: ${video?.id}")
                close()
                Post(
                    id = id,
                    title = title,
                    text = text,
                    date = date,
                    avatarId = avatar,
                    likes = likes,
                    comments = comments,
                    shared = shares,
                    views = views,
                    isLiked = isLiked,
                    video = video
                )
            } else {
                close()
                null
            }
        }
    }

    override fun deletePost(id: Long): Int = db.delete(
        PostEntry.TABLE_NAME,
        "${PostEntry.COLUMN_NAME_ID} = ?",
        arrayOf(id.toString())
    )

    override fun addPost(title: String, text: String): Long {
        val values = ContentValues().apply {
            put(PostEntry.COLUMN_NAME_TITLE, title)
            put(PostEntry.COLUMN_NAME_TEXT, text)
            put(
                PostEntry.COLUMN_NAME_DATE,
                DateFormat.format(Post.POST_DATE_ABSOLUTE, Date().time).toString()
            )
            put(PostEntry.COLUMN_NAME_AVATAR_ID, R.mipmap.ic_launcher)
        }
        return db.insert(PostEntry.TABLE_NAME, null, values)
    }

    override fun addVideo(postId: Long, video: YouTubeVideoData?): Int {
        val value = ContentValues().apply {
            put(PostEntry.COLUMN_NAME_YT_ID, video?.id)
            put(PostEntry.COLUMN_NAME_YT_AUTHOR, video?.author)
            put(PostEntry.COLUMN_NAME_YT_DURATION, video?.duration)
            put(PostEntry.COLUMN_NAME_YT_TITLE, video?.title)
            put(PostEntry.COLUMN_NAME_YT_THUMBNAIL_URL, video?.thumbnailUrl)
        }
        return db.update(
            PostEntry.TABLE_NAME,
            value,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(postId.toString())
        )
    }

    override fun removeVideo(id: Long): Int {
        val value = ContentValues().apply {
            putNull(PostEntry.COLUMN_NAME_YT_ID)
            putNull(PostEntry.COLUMN_NAME_YT_AUTHOR)
            putNull(PostEntry.COLUMN_NAME_YT_DURATION)
            putNull(PostEntry.COLUMN_NAME_YT_TITLE)
            putNull(PostEntry.COLUMN_NAME_YT_THUMBNAIL_URL)
        }
        return db.update(
            PostEntry.TABLE_NAME,
            value,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(id.toString())
        )
    }

    override fun editPost(id: Long, newText: String, newTitle: String): Int {
        val value = ContentValues().apply {
            put(PostEntry.COLUMN_NAME_TITLE, newTitle)
            put(PostEntry.COLUMN_NAME_TEXT, newText)
        }
        return db.update(
            PostEntry.TABLE_NAME,
            value,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(id.toString())
        )
    }

    override fun likePost(id: Long, previousLikesCount: Int, changed: Boolean): Int {
        val post = getPostById(id) ?: return -1
        val value = ContentValues().apply {
            put(
                PostEntry.COLUMN_NAME_LIKES_COUNT,
                if (post.isLiked) previousLikesCount - 1 else previousLikesCount + 1
            )
            put(PostEntry.COLUMN_NAME_IS_LIKED, changed.toSQL())
        }
        return db.update(
            PostEntry.TABLE_NAME,
            value,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(post.id.toString())
        )
    }

    override fun sharePost(id: Long): Int {
        val post = getPostById(id) ?: return -1
        val value = ContentValues().apply {
            put(PostEntry.COLUMN_NAME_SHARE_COUNT, post.shared + 1)
        }
        return db.update(
            PostEntry.TABLE_NAME,
            value,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(id.toString())
        )
    }

    override fun commentPost(id: Long): Int {
        val post = getPostById(id) ?: return -1
        val value = ContentValues().apply {
            put(PostEntry.COLUMN_NAME_COMMENTS_COUNT, post.comments + 1)
        }
        return db.update(
            PostEntry.TABLE_NAME,
            value,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(id.toString())
        )
    }

    private fun parseFromSQLBoolean(strBoolean: String): Boolean? =
        when (strBoolean.uppercase().trim()) {
            "TRUE" -> true
            "FALSE" -> false
            else -> null
        }
}