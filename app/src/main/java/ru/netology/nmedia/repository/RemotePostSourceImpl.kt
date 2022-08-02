package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.netology.nmedia.database.dao.PostDAO
import ru.netology.nmedia.network.exceptions.PostNotFoundException
import ru.netology.nmedia.network.post_api.dto.PostRequest
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.post_api.service.PostService
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.network.results.NetworkResult.Companion.RESPONSE_CODE_NOT_FOUND
import ru.netology.nmedia.network.results.safeApiCall
import ru.netology.nmedia.utils.getErrorMessage
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePostSourceImpl @Inject constructor(
    private val service: PostService,
    private val dao: PostDAO
) : RemotePostSource {

    override val latestPosts: Flow<List<PostResponse>> = flow {
        while (true) {
            val id = dao.getLastId()
            if (id != null) {
                val news = safeApiCall { service.getNewer(id) }
                emit(news.data ?: emptyList())
            }
            delay(DELAY)
        }
    }.catch { Timber.e("Error occurred while getting newer posts: ${it.getErrorMessage()}") }
        .flowOn(Dispatchers.IO)

    override suspend fun getAll(): NetworkResult<List<PostResponse>> =
        safeApiCall { service.getAll() }

    override suspend fun getPostById(id: Long): NetworkResult<PostResponse> =
        safeApiCall { service.getPostById(id) }

    override suspend fun addPost(title: String, text: String): NetworkResult<PostResponse> =
        safeApiCall {
            service.createPost(
                PostRequest(
                    id = 0L,
                    title = title,
                    text = text,
                    avatar = "",
                    date = OffsetDateTime.now().toEpochSecond(),
                    isLiked = false,
                    likes = 0,
                    attachment = null
                )
            )
        }

    override suspend fun deletePostById(id: Long): Boolean {
        return safeApiCall { service.deletePostById(id) } is NetworkResult.Success
    }

    override suspend fun editPost(id: Long, newText: String): NetworkResult<PostResponse> {
        return safeApiCall {
            service.editPost(
                PostRequest(
                    id = id,
                    text = newText,
                )
            )
        }
    }

    override suspend fun likeById(id: Long): NetworkResult<PostResponse> {
        val post = getPostById(id).data ?: return NetworkResult.Error(
            error = PostNotFoundException("Post with id $id is not found!"),
            code = RESPONSE_CODE_NOT_FOUND
        )
        return if (post.isLiked) {
            safeApiCall { service.dislikePostById(id) }
        } else safeApiCall { service.likePostById(id) }
    }

    private companion object {
        private const val DELAY: Long = 5000L
    }
}