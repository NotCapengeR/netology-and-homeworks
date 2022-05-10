package ru.netology.nmedia.ui

import java.time.LocalDateTime

data class Post(
    val id: Long,
    val title: String,
    val text: String,
    val avatarId: Int,
    val date: LocalDateTime,
    val likes: Int = 0,
    val comments: Int = 0,
    val shared: Int = 0,
    val views: Int = 0,
)
