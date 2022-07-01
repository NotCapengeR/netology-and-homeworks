package ru.netology.nmedia.repository

import ru.netology.nmedia.network.exceptions.ResultNotFoundException
import ru.netology.nmedia.network.post_api.dto.PostRequest
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.post_api.service.PostService
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.network.results.safeApiCall
import ru.netology.nmedia.network.results.saveCall
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePostSource @Inject constructor(private val service: PostService) {

    suspend fun getAll(): NetworkResult<List<PostResponse>> = safeApiCall { service.getAll() }

    suspend fun getPostById(id: Long): NetworkResult<PostResponse> =
        safeApiCall { service.getPostById(id) }

    suspend fun addPost(title: String, text: String): NetworkResult<PostResponse> =
        safeApiCall {
            service.createPost(
                PostRequest(
                    id = 0L,
                    title = title,
                    text = text,
                    date = OffsetDateTime.now().toEpochSecond(),
                    isLiked = false,
                    likes = 0
                )
            )
        }

    suspend fun deletePostById(id: Long): Boolean {
        return saveCall { service.deletePostById(id) }
    }

    suspend fun editPost(id: Long, newText: String): NetworkResult<PostResponse> {
        return safeApiCall { service.editPost(
            PostRequest(
                id = id,
                text = newText,
                title = ""
            )
        ) }
    }

    suspend fun likeById(id: Long): NetworkResult<PostResponse> {
        val post = getPostById(id).data
            ?: return NetworkResult.Error(error = ResultNotFoundException("Post not found!"))
        return if (post.isLiked) {
            safeApiCall { service.dislikePostById(id) }
        } else safeApiCall { service.likePostById(id) }
    }
}