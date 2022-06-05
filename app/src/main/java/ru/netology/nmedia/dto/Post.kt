package ru.netology.nmedia.dto

import ru.netology.nmedia.R
import ru.netology.nmedia.network.YouTubeVideo

data class Post(
    val id: Long,
    val title: String,
    val text: String,
    val date: Long,
    val avatarId: Int = R.drawable.ic_baseline_account_circle_24,
    val likes: Int = 0,
    val comments: Int = 0,
    val shared: Int = 0,
    val views: Int = 0,
    val isLiked: Boolean = false,
    val editHistory: MutableList<String> = mutableListOf(),
    val video: YouTubeVideo? = null
)
