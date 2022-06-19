package ru.netology.nmedia.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.database.PostDAO
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.YouTubeVideoData
import ru.netology.nmedia.network.ApiService
import ru.netology.nmedia.network.YouTubeVideo
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val service: ApiService,
    private val dao: PostDAO
) : PostRepository {

    private val posts: MutableMap<Long, Post> = HashMap()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private fun findPostById(id: Long): PostSearchResult {
        val post = posts[id] ?: return PostSearchResult.NotFound
        return PostSearchResult.Success(post)
    }


    init {
        val postsList = dao.getAll()
        postsList.forEach {
            posts[it.id] = it
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
        val id = dao.addPost(title, text)
        val post = dao.getPostById(id) ?: return -1L
        posts[id] = post
        return id
    }

    override suspend fun addVideo(url: String, postId: Long) = withContext(Dispatchers.IO) {
        val id = getIdFromYouTubeLink(url) ?: return@withContext
        val post = getPostById(postId) ?: return@withContext
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
                    dao.addVideo(post.id, video)
                    posts[postId] = post.copy(video = video)
                }
            }

            override fun onFailure(call: Call<YouTubeVideo>, t: Throwable) {
                Timber.e("Something went wrong: $t")
            }
        })
    }

    override fun removeLink(id: Long): Boolean {
        val post = getPostById(id) ?: return false
        posts[id] = post.copy(video = null)
        dao.removeVideo(id)
        return posts[id]?.video == null
    }

    override fun removePost(id: Long): Boolean {
        val post = findPostById(id).post ?: return false
        posts.remove(id)
        dao.deletePost(id)
        return !posts.containsValue(post)
    }

    override fun editPost(id: Long, newText: String, newTitle: String): Boolean {
        val post = findPostById(id).post ?: return false
        val newPost = post.copy(text = newText, title = newTitle)
        dao.editPost(id, newText, newTitle)
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
        dao.likePost(id, previousLikesCount, changed)
        return true
    }

    override fun sharePost(id: Long): Int {
        val post = findPostById(id).post ?: return -1
        val nextValue = post.shared + 1
        dao.sharePost(id)
        posts[id] = post.copy(shared = nextValue)
        return nextValue
    }

    override fun commentPost(id: Long): Int {
        val post = findPostById(id).post ?: return -1
        val nextValue = post.comments + 1
        dao.sharePost(id)
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