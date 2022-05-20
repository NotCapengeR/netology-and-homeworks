package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {

    fun getPosts(): List<Post>

    fun addPost(title: String, text: String): Long

    fun removePost(id: Long): Boolean

    fun editPost(id: Long, newText: String): Boolean

    fun getPostById(id: Long): Post?

    fun likePost(id: Long): Boolean

    fun sharePost(id: Long): Int

    fun commentPost(id: Long): Int

    fun onPostMoved(id: Long, movedBy: Int): Pair<Post, Post>?
}