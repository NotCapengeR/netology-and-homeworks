package ru.netology.nmedia.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.text.format.DateFormat
import androidx.core.database.getStringOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.R
import ru.netology.nmedia.database.PostDB.Companion.parseFromSQLBoolean
import ru.netology.nmedia.database.PostDB.PostEntry
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Post.Companion.POST_DATE_ABSOLUTE
import ru.netology.nmedia.dto.YouTubeVideoData
import ru.netology.nmedia.network.ApiService
import ru.netology.nmedia.network.YouTubeVideo
import ru.netology.nmedia.utils.toDateTime
import ru.netology.nmedia.utils.toSQL
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val service: ApiService,
    private val db: SQLiteDatabase
) : PostRepository {

    private val posts: MutableMap<Long, Post> = HashMap()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private fun findPostById(id: Long): PostSearchResult {
        val post = posts[id] ?: return PostSearchResult.NotFound
        return PostSearchResult.Success(post)
    }

    private fun getPostFromDB(id: Long): Post? {
        val cursor = db.query(
            PostEntry.TABLE_NAME,
            null,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            "${PostEntry.COLUMN_NAME_ID} ASC"
        )
        return if (cursor.moveToFirst()) {
            with(cursor) {
                val title = getString(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_TITLE))
                val text = getString(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_TEXT))
                val date = getString(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_DATE))
                    .toDateTime() ?: Timber.e("Post date value is invalid!")
                    .let { Date().time }
                val avatar = getInt(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_AVATAR_ID))
                val likes = getInt(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_LIKES_COUNT))
                val comments = getInt(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_COMMENTS_COUNT))
                val shares = getInt(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_SHARE_COUNT))
                val views = getInt(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_VIEWS_COUNT))
                val isLiked: Boolean = parseFromSQLBoolean(
                    getString(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_IS_LIKED))
                ) ?: Timber.e("Post isLiked value is invalid!").let { false }
                val ytId = getStringOrNull(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_YT_ID))
                val ytAuthor =
                    getStringOrNull(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_YT_AUTHOR))
                val ytTitle = getStringOrNull(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_YT_TITLE))
                val ytDuration =
                    getStringOrNull(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_YT_DURATION))
                val ytUrl =
                    getStringOrNull(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_YT_THUMBNAIL_URL))
                val video = buildVideoData(ytId, ytAuthor, ytTitle, ytDuration, ytUrl)
                Timber.d("YT video id in repo: ${video?.id}")
                cursor.close()
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
            }
        } else {
            cursor.close()
            null
        }
    }

    // Always returns 1 (or 0)
    private fun deletePostInDB(id: Long): Int = db.delete(
        PostEntry.TABLE_NAME,
        "${PostEntry.COLUMN_NAME_ID} = ?",
        arrayOf(id.toString())
    )


    init {
        val cursor = db.query(
            PostEntry.TABLE_NAME,
            arrayOf(PostEntry.COLUMN_NAME_ID),
            "${PostEntry.COLUMN_NAME_ID} >= ?",
            arrayOf("0"),
            null,
            null,
            "${PostEntry.COLUMN_NAME_ID} ASC"
        )
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(PostEntry.COLUMN_NAME_ID))
                val post = getPostFromDB(id)
                if (post != null) posts[id] = post
            }
        }
        cursor.close()
    }

    private fun buildVideoData(
        id: String?,
        author: String?,
        title: String?,
        duration: String?,
        url: String?
    ): YouTubeVideoData? {
        return if (id != null && author != null && title != null && duration != null && url != null) {
            YouTubeVideoData(id, author, title, duration, url)
        } else null
    }

    private fun getIdFromYouTubeLink(link: String?): String? {
        if (link == null) return null
        val matcher = COMPILED_PATTERN.matcher(link)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    override fun getPosts(): MutableList<Post> = posts.values.toMutableList()

    override fun getPostById(id: Long): Post? = findPostById(id).post

    override fun addPost(title: String, text: String): Long {
        val values = ContentValues().apply {
            put(PostEntry.COLUMN_NAME_TITLE, title)
            put(PostEntry.COLUMN_NAME_TEXT, text)
            put(
                PostEntry.COLUMN_NAME_DATE,
                DateFormat.format(POST_DATE_ABSOLUTE, Date().time).toString()
            )
            put(PostEntry.COLUMN_NAME_AVATAR_ID, R.mipmap.ic_launcher)
        }
        val id = db.insert(PostEntry.TABLE_NAME, null, values)
        val post = getPostFromDB(id)
        if (post != null) posts[id] = post
        return id
    }

    override suspend fun addVideo(url: String, postId: Long) = withContext(Dispatchers.IO) {
        val id = getIdFromYouTubeLink(url)
        val post = getPostById(postId)
        if (id != null && post != null) {
            service.getVideoData(id).enqueue(object : Callback<YouTubeVideo> {
                override fun onResponse(
                    call: Call<YouTubeVideo>,
                    response: Response<YouTubeVideo>
                ) {
                    Timber.d(
                        "Response code: ${response.code()}, " +
                                "body id: ${response.body()?.items?.first()?.id}"
                    )
                    if (response.code() == 200) {
                        val video = YouTubeVideoData.parser(response.body())
                        val value = ContentValues().apply {
                            put(PostEntry.COLUMN_NAME_YT_ID, video?.id)
                            put(PostEntry.COLUMN_NAME_YT_AUTHOR, video?.author)
                            put(PostEntry.COLUMN_NAME_YT_DURATION, video?.duration)
                            put(PostEntry.COLUMN_NAME_YT_TITLE, video?.title)
                            put(PostEntry.COLUMN_NAME_YT_THUMBNAIL_URL, video?.thumbnailUrl)
                        }
                        db.update(
                            PostEntry.TABLE_NAME,
                            value,
                            "${PostEntry.COLUMN_NAME_ID} = ?",
                            arrayOf(post.id.toString())
                        )
                        posts[postId] = post.copy(video = video)
                    }
                }

                override fun onFailure(call: Call<YouTubeVideo>, t: Throwable) {
                    Timber.e("Something went wrong: $t")
                }
            })
        }
    }

    override fun removeLink(id: Long): Boolean {
        val post = getPostById(id) ?: return false
        posts[id] = post.copy(video = null)
        val value = ContentValues().apply {
            putNull(PostEntry.COLUMN_NAME_YT_ID)
            putNull(PostEntry.COLUMN_NAME_YT_AUTHOR)
            putNull(PostEntry.COLUMN_NAME_YT_DURATION)
            putNull(PostEntry.COLUMN_NAME_YT_TITLE)
            putNull(PostEntry.COLUMN_NAME_YT_THUMBNAIL_URL)
        }
        db.update(
            PostEntry.TABLE_NAME,
            value,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(id.toString())
        )
        return posts[id]?.video == null
    }

    override fun removePost(id: Long): Boolean {
        val post = findPostById(id).post ?: return false
        posts.remove(id)
        deletePostInDB(id)
        return !posts.containsValue(post)
    }

    override fun editPost(id: Long, newText: String, newTitle: String): Boolean {
        val post = findPostById(id).post ?: return false
        val newPost = post.copy(text = newText, title = newTitle)
        val value = ContentValues().apply {
            put(PostEntry.COLUMN_NAME_TITLE, newTitle)
            put(PostEntry.COLUMN_NAME_TEXT, newText)
        }
        db.update(
            PostEntry.TABLE_NAME,
            value,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(id.toString())
        )
        posts[id] = newPost
        return posts.containsValue(newPost)
    }

    override fun likePost(id: Long): Boolean {
        val post = findPostById(id).post ?: return false
        val previousLikesCount = post.likes
        val changed = !post.isLiked
        posts[id] = post.copy(
            likes = if (post.isLiked) previousLikesCount - 1 else previousLikesCount + 1,
            isLiked = changed
        )

        val value = ContentValues().apply {
            put(
                PostEntry.COLUMN_NAME_LIKES_COUNT,
                if (post.isLiked) previousLikesCount - 1 else previousLikesCount + 1
            )
            put(PostEntry.COLUMN_NAME_IS_LIKED, changed.toSQL())
        }
        db.update(
            PostEntry.TABLE_NAME,
            value,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(id.toString())
        )
        return true
    }

    override fun sharePost(id: Long): Int {
        val post = findPostById(id).post ?: return -1
        val nextValue = post.shared + 1
        val value = ContentValues().apply {
            put(PostEntry.COLUMN_NAME_SHARE_COUNT, nextValue)
        }
        db.update(
            PostEntry.TABLE_NAME,
            value,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(id.toString())
        )
        posts[id] = post.copy(shared = nextValue)
        return nextValue
    }

    override fun commentPost(id: Long): Int {
        val post = findPostById(id).post ?: return -1
        val nextValue = post.comments + 1
        val value = ContentValues().apply {
            put(PostEntry.COLUMN_NAME_COMMENTS_COUNT, nextValue)
        }
        db.update(
            PostEntry.TABLE_NAME,
            value,
            "${PostEntry.COLUMN_NAME_ID} = ?",
            arrayOf(id.toString())
        )
        posts[id] = post.copy(comments = nextValue)
        return nextValue
    }

    override fun onPostMoved(id: Long, movedBy: Int): Pair<Post, Post>? {
        return null
        //  может когда-нибудь придётся переделать...
        //        val post = findPostById(id).post ?: return null
        //        val postsList = getPosts()
        //        val postIndex = postsList.indexOf(post)
        //        return try {
        //            val swappablePost = postsList[postIndex - movedBy]
        //            Pair(post, swappablePost)
        //        } catch (ex: ArrayIndexOutOfBoundsException) {
        //            null
        //        }
    }


    private companion object {
        private const val URL_PATTERN: String =
            "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*"
        private val COMPILED_PATTERN: Pattern =
            Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE)
    }
}