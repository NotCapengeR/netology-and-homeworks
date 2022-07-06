package ru.netology.nmedia.repository.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.utils.Mapper

@Parcelize
data class Post(
    val id: Long,
    val title: String,
    val text: String,
    val avatar: String,
    val date: Long,
    val likes: Int = 0,
    val comments: Int = 0,
    val shared: Int = 0,
    val views: Int = 0,
    val isLiked: Boolean = false,
    val video: YouTubeVideoData? = null,
    val attachment: Attachment? = null
) : Parcelable {

    companion object {
        const val POST_ID: String = "post_id"
        const val POST_DATE_PATTERN: String = "d MMMM yyyy, HH:mm"
        const val POST_DATE_ABSOLUTE: String = "dd-MM-yyyy, HH:mm:ss"
        const val AVATARS_BASE_URL: String = "http://10.0.2.2:9999/avatars/"
        const val ATTACHMENTS_BASE_URL: String = "http://10.0.2.2:9999/images/"
        val EMPTY_POST: Post = Post(
            id = 0L,
            title = "",
            text = "",
            avatar = "",
            date = 0L,
            likes = 0,
            comments = 0,
            shared = 0,
            views = 0,
            isLiked = false,
            video = null,
            attachment = null
        )

        fun parser(entity: PostEntity?): Post? {
            if (entity == null) return null
            return Post(
                id = entity.id,
                title = entity.title,
                text = entity.text,
                date = Mapper.parseStringToEpoch(entity.date),
                avatar = entity.avatar,
                likes = entity.likes,
                comments = entity.comments,
                shared = entity.shares,
                views = entity.views,
                isLiked = entity.isLiked,
                video = entity.video,
                attachment = entity.attachment
            )
        }

        fun parser(response: PostResponse?): Post? {
            if (response == null) return null
            return Post(
                id = response.id,
                title = response.title,
                text = response.text,
                avatar = response.avatar,
                date = response.date,
                likes = response.likes,
                isLiked = response.isLiked,
                attachment = response.attachment
            )
        }
    }
}
