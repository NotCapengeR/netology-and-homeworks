package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {

    fun getPosts(): List<Post>

    fun getAllPosts(): List<Post>

    suspend fun addPost(title: String, text: String): Long

    suspend fun addVideo(url: String, postId: Long)

    suspend fun removeLink(id: Long): Boolean

    suspend fun removePost(id: Long): Boolean

    suspend fun editPost(id: Long, newText: String, newTitle: String): Boolean

    fun getPostById(id: Long): Post?

    suspend fun likePost(id: Long): Boolean

    suspend fun sharePost(id: Long): Int

    suspend fun commentPost(id: Long): Int
}