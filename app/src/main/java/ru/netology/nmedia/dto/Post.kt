package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val title: String,
    val text: String,
    val avatarId: Int,
    val date: Long,
    val likes: Int = 0,
    val comments: Int = 0,
    val shared: Int = 0,
    val views: Int = 0,
    val isLiked: Boolean = false,
)
