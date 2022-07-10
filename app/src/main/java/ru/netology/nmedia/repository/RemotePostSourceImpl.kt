package ru.netology.nmedia.repository

import ru.netology.nmedia.network.exceptions.PostNotFoundException
import ru.netology.nmedia.network.post_api.dto.PostRequest
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.post_api.service.PostService
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.network.results.NetworkResult.Companion.RESPONSE_CODE_NOT_FOUND
import ru.netology.nmedia.network.results.safeApiCall
import ru.netology.nmedia.network.results.saveCall
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePostSourceImpl @Inject constructor(
    private val service: PostService
) : RemotePostSource {

    override suspend fun getAll(): NetworkResult<List<PostResponse>> = safeApiCall { service.getAll() }

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
        return saveCall { service.deletePostById(id) }
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

    override suspend fun dislikeById(id: Long): NetworkResult<PostResponse> {
        TODO("Maybe later???")
    }
}