package ru.netology.nmedia.repository

import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.results.NetworkResult
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.repository.dto.Media
import ru.netology.nmedia.repository.dto.Photo

interface RemotePostSource {

    suspend fun getPostById(id: Long): NetworkResult<PostResponse>

    suspend fun addPost(title: String, text: String, attachment: Attachment?): NetworkResult<PostResponse>

    suspend fun deletePostById(id: Long): Boolean

    suspend fun editPost(id: Long, newText: String): NetworkResult<PostResponse>

    suspend fun likeById(id: Long): NetworkResult<PostResponse>

    suspend fun uploadImage(photo: Photo?): NetworkResult<Media>

}