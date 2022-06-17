package ru.netology.nmedia.dto

import ru.netology.nmedia.R

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
    val video: YouTubeVideoData? = null
) {
    companion object {
        const val POST_TITLE: String = "post_title"
        const val POST_TEXT: String = "post_text"
        const val POST_ID: String = "post_id"
        const val POST_DATE_PATTERN: String = "d MMMM yyyy, HH:mm"
        const val POST_DATE_ABSOLUTE: String = "dd-MM-yyyy, HH:mm:ss"
        val EMPTY_POST: Post = Post(
            0L,
            "",
            "",
            0L,
            0,
            0,
            0,
            0,
            0,
            false,
            null
        )
    }
}
