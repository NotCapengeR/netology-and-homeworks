package ru.netology.nmedia.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Post.Companion.POST_ID
import ru.netology.nmedia.dto.YouTubeVideoData
import ru.netology.nmedia.network.ApiService
import ru.netology.nmedia.network.YouTubeVideo
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val service: ApiService,
    private val gson: Gson,
    private val context: Context,
    private val preferences: SharedPreferences
) : PostRepository {

    private var posts: MutableMap<Long, Post> = HashMap()
    private val removedPosts: MutableMap<Long, Post> = HashMap()
    private var postId: Long = 1L

    private fun findPostById(id: Long): PostSearchResult {
        val post = posts[id] ?: return PostSearchResult.NotFound
        return PostSearchResult.Success(post)
    }

    init {
        val file = context.filesDir.resolve(FILENAME)
        if (file.exists()) {
            context.openFileInput(FILENAME).bufferedReader().use {
                val postsList: List<Post> = gson.fromJson(it, type)
                if (postsList.isNotEmpty()) {
                    postId = preferences.getLong(POST_ID, postId)
                    posts = postsList.associateBy { post -> post.id }.toMutableMap()
                } else {
                    writeFiles()
                    preferences.edit {
                        putLong(POST_ID, postId)
                    }
                }
            }
        } else {
            writeFiles()
            preferences.edit {
                putLong(POST_ID, postId)
            }
        }
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
        val post = Post(postId, title, text, Date().time, R.mipmap.ic_launcher)
        posts[postId] = post
        postId++
        preferences.edit {
            putLong(POST_ID, postId)
        }
        writeFiles()
        return post.id
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
                        posts[postId] = post.copy(video = YouTubeVideoData.parser(response.body()))
                        writeFiles()
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
        writeFiles()
        return posts[id]?.video == null
    }

    override fun removePost(id: Long): Boolean {
        val post = findPostById(id).post ?: return false
        posts.remove(id)
        removedPosts[id] = post
        writeFiles()
        return !posts.containsValue(post)
    }

    override fun editPost(id: Long, newText: String, newTitle: String): Boolean {
        val post = findPostById(id).post ?: return false
        val newPost = post.copy(text = newText, title = newTitle)
        newPost.editHistory.add(post.text)
        newPost.titleHistory.add(post.title)
        posts[id] = newPost
        writeFiles()
        return posts.containsValue(newPost)
    }

    override fun likePost(id: Long): Boolean {
        val post = findPostById(id).post ?: return false
        val previousLikesCount = post.likes
        if (post.isLiked) {
            posts[id] = post.copy(likes = previousLikesCount - 1, isLiked = !post.isLiked)
        } else {
            posts[id] = post.copy(likes = previousLikesCount + 1, isLiked = !post.isLiked)
        }
        writeFiles()
        return true
    }

    override fun sharePost(id: Long): Int {
        val post = findPostById(id).post ?: return -1
        val nextValue = post.shared + 450
        posts[id] = post.copy(shared = nextValue)
        writeFiles()
        return nextValue
    }

    override fun commentPost(id: Long): Int {
        val post = findPostById(id).post ?: return -1
        val nextValue = post.comments + 450
        posts[id] = post.copy(comments = nextValue)
        writeFiles()
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

    private fun writeFiles() {
        context.openFileOutput(FILENAME, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(posts.values.toList()))
        }
    }

    private companion object {
        private val type: Type =
            TypeToken.getParameterized(List::class.java, Post::class.java).type
        private const val URL_PATTERN: String =
            "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*"
        private val COMPILED_PATTERN: Pattern =
            Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE)
        private const val FILENAME: String = "posts.json"
    }
}