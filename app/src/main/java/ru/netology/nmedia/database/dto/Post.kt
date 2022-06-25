package ru.netology.nmedia.database.dto

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import ru.netology.nmedia.R
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.utils.toDateTime
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class Post(
    val id: Long,
    val title: String,
    val text: String,
    val date: Long,
    @StringRes val avatarId: Int = R.mipmap.ic_launcher,
    val likes: Int = 0,
    val comments: Int = 0,
    val shared: Int = 0,
    val views: Int = 0,
    val isLiked: Boolean = false,
    val video: YouTubeVideoData? = null
) : Parcelable {
    companion object {
        private val SIMPLE_POST_FORMAT = SimpleDateFormat("d MMMM yyyy, HH:mm")
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

        fun parseEpochSeconds(epoch: Long): String {
            val date = Date(epoch * 1000L)
            return SIMPLE_POST_FORMAT.format(date)
        }

        fun parser(entity: PostEntity?): Post? {
            if (entity == null) return null
            return Post(
                id = entity.id,
                title = entity.title,
                text = entity.text,
                date = entity.date.toDateTime()
                    ?: throw IllegalArgumentException("Invalid date pattern"),
                avatarId = entity.avatarId,
                likes = entity.likes,
                comments = entity.comments,
                shared = entity.shares,
                views = entity.views,
                isLiked = entity.isLiked,
                video = YouTubeVideoData.buildVideoData(
                    entity.ytId,
                    entity.ytAuthor,
                    entity.ytTitle,
                    entity.ytDuration,
                    entity.ytThumbnailUrl
                )
            )
        }

        fun parser(response: PostResponse): Post {
            return Post(
                id = response.id,
                title = response.title,
                text = response.text,
                date = response.date,
                avatarId = R.mipmap.ic_launcher,
                likes = response.likes,
                isLiked = response.isLiked,
            )
        }

        fun mapEntitiesToPosts(entities: List<PostEntity>): List<Post> =
            entities.mapNotNull { parser(it) }
    }
}
