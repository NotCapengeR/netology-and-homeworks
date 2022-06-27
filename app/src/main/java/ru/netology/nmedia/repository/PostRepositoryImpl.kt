package ru.netology.nmedia.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import ru.netology.nmedia.database.dao.PostDAO
import ru.netology.nmedia.database.dto.Post
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.network.youtube.ApiService
import ru.netology.nmedia.utils.Mapper
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val service: ApiService,
    private val dao: PostDAO,
    private val source: RemotePostSource
) : PostRepository {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private fun getIdFromYouTubeLink(link: String?): String? {
        if (link == null) return null
        val matcher = COMPILED_PATTERN.matcher(link)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }


    override fun getAllPosts(): Flow<NetworkResult<List<PostResponse>>> = flow {
        emit(NetworkResult.Loading())
        emit(source.getAll())
    }.flowOn(Dispatchers.IO)

    override fun getPostsFromDB(): Flow<List<Post>> = dao.getAll()
        .map { Mapper.mapEntitiesToPosts(it) }
        .catch { Timber.e("Error occurredL ${it.message ?: it.toString()}") }
        .flowOn(Dispatchers.IO)

    override suspend fun getPostById(id: Long): Post? =
        when (val response = source.getPostById(id)) {
            is NetworkResult.Success -> Post.parser(response.data)
            is NetworkResult.Loading -> null
            is NetworkResult.Error -> Post.parser(dao.getPostById(id))
        }


    override suspend fun addPost(title: String, text: String): Long {
        source.addPost(title, text).data?.id?.let {
            dao.addPost(it, title, text)
            return it
        }
        return 0L
    }


    override fun addVideo(url: String, postId: Long) {
//        val id = getIdFromYouTubeLink(url) ?: return
//        service.getVideoData(id).enqueue(object : Callback<YouTubeVideo> {
//            override fun onResponse(
//                call: Call<YouTubeVideo>,
//                response: Response<YouTubeVideo>
//            ) {
//                Timber.d(
//                    "Response code: ${response.code()}, " +
//                            "body id: ${response.body()?.items?.first()?.id}"
//                )
//                if (response.code() == 200) {
//                    scope.launch(Dispatchers.IO) {
//                        val post = getPostById(postId) ?: return@launch
//                        val video = YouTubeVideoData.parser(response.body())
//                        dao.addVideo(
//                            post.id,
//                            video?.id,
//                            video?.author,
//                            video?.title,
//                            video?.duration,
//                            video?.thumbnailUrl
//                        )
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<YouTubeVideo>, t: Throwable) {
//                Timber.e("Exception occurred: ${t.message ?: t.toString()}")
//            }
//        })
    }

    override suspend fun removeLink(id: Long): Boolean {
        return false
        //return dao.removeVideo(id) > 0
    }

    override suspend fun removePost(id: Long): Boolean {
        return source.deletePostById(id).also {
            if (it) {
                dao.deletePostById(id)
            }
        }
    }

    override suspend fun editPost(id: Long, newText: String): Boolean {
        return (source.editPost(id, newText) is NetworkResult.Success).also {
            if (it) {
                dao.editPost(id, newText)
            }
        }
    }

    override suspend fun likePost(id: Long): Boolean {
        return source.likeById(id).also {
            val post = it.data ?: return@also
            dao.likePostById(post.id, post.likes, post.isLiked)
        } is NetworkResult.Success
    }

    override suspend fun sharePost(id: Long): Int {
        return 0
//        val post = getPostById(id) ?: return -1
//        val nextValue = post.shared + 1
//        dao.sharePostById(post.id, nextValue)
//        return nextValue
    }

    override suspend fun commentPost(id: Long): Int {
        return 0
//        val post = getPostById(id) ?: return -1
//        val nextValue = post.comments + 1
//        dao.commentPostById(post.id, nextValue)
//        return nextValue
    }

    private companion object {
        private const val URL_PATTERN: String =
            "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*"
        private val COMPILED_PATTERN: Pattern =
            Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE)
    }
}