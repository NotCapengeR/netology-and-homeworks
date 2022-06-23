package ru.netology.nmedia.repository

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.database.dao.PostDAO
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

    private val posts: Map<Long, Post>
    get() = runBlocking(Dispatchers.IO) {
        getAllPosts().first().associateBy {
            it.id
        }
    }
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private fun findPostById(id: Long): PostSearchResult {
        val post = posts[id] ?: return PostSearchResult.NotFound
        return PostSearchResult.Success(post)
    }

    private fun getIdFromYouTubeLink(link: String?): String? {
        if (link == null) return null
        val matcher = COMPILED_PATTERN.matcher(link)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    override suspend fun getPosts(): MutableList<Post> =
        withContext(scope.coroutineContext + Dispatchers.IO) {
            posts.values.toMutableList()
        }

    override fun getAllPosts(): Flow<List<Post>> = dao.getAll()
        .map { entities -> Post.mapEntitiesToPosts(entities) }
        .flowOn(Dispatchers.IO)

    override fun getPostById(id: Long): Post? = findPostById(id).post

    override suspend fun addPost(title: String, text: String): Long {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            dao.addPost(title, text)
        }
    }


    override fun addVideo(url: String, postId: Long) {
        val id = getIdFromYouTubeLink(url) ?: return
        val post = getPostById(postId) ?: return
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
                    scope.launch(Dispatchers.IO) {
                        val video = YouTubeVideoData.parser(response.body())
                        dao.addVideo(
                            post.id,
                            video?.id,
                            video?.author,
                            video?.title,
                            video?.duration,
                            video?.thumbnailUrl
                        )
                    }
                }
            }

            override fun onFailure(call: Call<YouTubeVideo>, t: Throwable) {
                Timber.e("Something went wrong: $t")
            }
        })
    }

    override suspend fun removeLink(id: Long): Boolean {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            dao.removeVideo(id)
        } > 0
    }

    override suspend fun removePost(id: Long): Boolean {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            dao.deletePostById(id)
        } > 0
    }

    override suspend fun editPost(id: Long, newText: String, newTitle: String): Boolean {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            dao.editPost(
                id,
                newTitle,
                newText
            )
        } > 0
    }

    override suspend fun likePost(id: Long): Boolean {
        val post = findPostById(id).post ?: return false
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val changed = !post.isLiked
            val nextValue = if (post.isLiked) post.likes - 1 else post.likes + 1
            dao.likePostById(post.id, nextValue, changed)
        } > 0
    }

    override suspend fun sharePost(id: Long): Int {
        val post = findPostById(id).post ?: return -1
        val nextValue = post.shared + 1
        withContext(scope.coroutineContext + Dispatchers.IO) {
            dao.sharePostById(post.id, nextValue)
        }
        return nextValue
    }

    override suspend fun commentPost(id: Long): Int {
        val post = findPostById(id).post ?: return -1
        val nextValue = post.comments + 1
        withContext(scope.coroutineContext + Dispatchers.IO) {
            dao.commentPostById(post.id, nextValue)
        }
        return nextValue
    }

    private companion object {
        private const val URL_PATTERN: String =
            "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*"
        private val COMPILED_PATTERN: Pattern =
            Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE)
    }
}