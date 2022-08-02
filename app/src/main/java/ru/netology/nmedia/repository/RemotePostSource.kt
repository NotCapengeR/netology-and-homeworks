package ru.netology.nmedia.repository

import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.results.NetworkResult
import kotlinx.coroutines.flow.Flow

interface RemotePostSource {

    val latestPosts: Flow<List<PostResponse>>

    suspend fun getAll(): NetworkResult<List<PostResponse>>

    suspend fun getPostById(id: Long): NetworkResult<PostResponse>

    suspend fun addPost(title: String, text: String): NetworkResult<PostResponse>

    suspend fun deletePostById(id: Long): Boolean

    suspend fun editPost(id: Long, newText: String): NetworkResult<PostResponse>

    suspend fun likeById(id: Long): NetworkResult<PostResponse>

}