package ru.netology.nmedia.repository

import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.database.dao.PostDAO
import ru.netology.nmedia.network.exceptions.PostNotFoundException
import ru.netology.nmedia.network.post_api.dto.PostRequest
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.post_api.service.PostService
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.network.results.NetworkResult.Companion.EXCEPTION_OCCURRED_CODE
import ru.netology.nmedia.network.results.NetworkResult.Companion.RESPONSE_CODE_NOT_FOUND
import ru.netology.nmedia.network.results.safeApiCall
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.repository.dto.Media
import ru.netology.nmedia.repository.dto.Photo
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePostSourceImpl @Inject constructor(
    private val service: PostService,
) : RemotePostSource {

    override suspend fun getPostById(id: Long): NetworkResult<PostResponse> =
        safeApiCall { service.getPostById(id) }

    override suspend fun addPost(title: String, text: String, attachment: Attachment?): NetworkResult<PostResponse> =
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
                    attachment = attachment
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

    override suspend fun uploadImage(photo: Photo?): NetworkResult<Media> {
        if (photo?.file == null) return NetworkResult.Error(NullPointerException("Image is null"), EXCEPTION_OCCURRED_CODE)
        return safeApiCall {
            service.uploadImage(
                MultipartBody.Part.createFormData(
                    "file", photo.file.name, photo.file.asRequestBody()
                )
            )
        }
    }

    private companion object {
        private const val DELAY: Long = 5000L
    }
}