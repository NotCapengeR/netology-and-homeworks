package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {

    fun likePost(post: Post): Boolean

    fun sharePost(post: Post): Int

    fun getPost(): Post
}