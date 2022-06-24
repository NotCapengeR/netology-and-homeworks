package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {

    fun getAllPosts(): Flow<List<Post>>

    suspend fun addPost(title: String, text: String): Long

    fun addVideo(url: String, postId: Long)

    suspend fun removeLink(id: Long): Boolean

    suspend fun removePost(id: Long): Boolean

    suspend fun editPost(id: Long, newText: String, newTitle: String): Boolean

    suspend fun getPostById(id: Long): Post?

    suspend fun likePost(id: Long): Boolean

    suspend fun sharePost(id: Long): Int

    suspend fun commentPost(id: Long): Int
}