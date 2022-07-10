package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.results.NetworkResult

interface PostRepository {

    fun getAllPosts(): Flow<NetworkResult<List<PostResponse>>>

    fun getPostsFromDB(): Flow<List<Post>>

    suspend fun getPostsFromDBAsList(): List<Post>

    suspend fun addPost(title: String, text: String): Long

    suspend fun getDeletedPostsIds(): List<Long>

    fun addVideo(url: String, postId: Long)

    suspend fun removeLink(id: Long): Boolean

    suspend fun removePost(id: Long): Boolean

    suspend fun editPost(id: Long, newText: String): Boolean

    suspend fun getPostById(id: Long): Post?

    suspend fun getPostFromDBById(id: Long): Post?

    suspend fun getException(id: Long): Throwable?

    suspend fun likePost(id: Long): Boolean

    suspend fun sharePost(id: Long): Int

    suspend fun commentPost(id: Long): Int
}