package ru.netology.nmedia.dto

import ru.netology.nmedia.R
import java.util.*

data class Post(
    val id: Long,
    val title: String,
    val text: String,
    var likesCount: Int = 0,
    var shareCount: Int = 0,
    val commentsCount: Int = 0,
    var isLiked: Boolean = false,
    val views: Int = 0,
    val date: Long = Date().time,
    val avatarId: Int = R.drawable.ic_launcher_foreground
)
