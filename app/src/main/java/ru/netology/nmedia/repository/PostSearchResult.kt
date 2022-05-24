package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

sealed class PostSearchResult(open val post: Post?) {

    data class Success(override val post: Post) : PostSearchResult(post)

    data class Failure(override val post: Post, val errorCode: Int) : PostSearchResult(post)

    object NotFound : PostSearchResult(null)
}