package ru.netology.nmedia.repository.dto

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize
import ru.netology.nmedia.R
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.utils.Mapper

@Parcelize
data class Post(
    val id: Long,
    val title: String,
    val text: String,
    val date: Long,
    @DrawableRes val avatarId: Int = R.mipmap.ic_launcher,
    val likes: Int = 0,
    val comments: Int = 0,
    val shared: Int = 0,
    val views: Int = 0,
    val isLiked: Boolean = false,
    val video: YouTubeVideoData? = null
) : Parcelable {
    companion object {
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

        fun parser(entity: PostEntity?): Post? {
            if (entity == null) return null
            return Post(
                id = entity.id,
                title = entity.title,
                text = entity.text,
                date = Mapper.parseStringToEpoch(entity.date),
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

        fun parser(response: PostResponse?): Post? {
            if (response == null) return null
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
    }
}
