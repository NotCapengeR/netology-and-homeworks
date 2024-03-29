package ru.netology.nmedia.repository.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.utils.Mapper

sealed interface PostAdapterEntity

@Parcelize
data class Post(
    val id: Long,
    val title: String,
    val text: String,
    val avatar: String,
    val authorId: Long,
    val date: Long,
    val likes: Int = 0,
    val comments: Int = 0,
    val shared: Int = 0,
    val views: Int = 0,
    val isLiked: Boolean = false,
    val video: YouTubeVideoData? = null,
    val attachment: Attachment? = null,
    var isOwner: Boolean = false
) : Parcelable, PostAdapterEntity {

    companion object {
        const val POST_ID: String = "post_id"
        const val POST_DATE_PATTERN: String = "d MMMM yyyy, HH:mm"
        const val POST_DATE_ABSOLUTE: String = "dd-MM-yyyy, HH:mm:ss"
        const val AVATARS_BASE_URL: String = "http://10.0.2.2:9999/avatars/"
        const val ATTACHMENTS_BASE_URL: String = "http://10.0.2.2:9999/media/"
        const val PAGE_SIZE: Int = 10
        const val MAX_SIZE: Int = 300
        val EMPTY_POST: Post = Post(
            id = 0L,
            title = "",
            text = "",
            avatar = "",
            date = 0L,
            authorId = 0L,
            likes = 0,
            comments = 0,
            shared = 0,
            views = 0,
            isLiked = false,
            video = null,
            attachment = null,
            isOwner = false
        )

        fun parser(entity: PostEntity?): Post? {
            if (entity == null) return null
            return Post(
                id = entity.id,
                title = entity.title,
                text = entity.text,
                authorId = entity.authorId,
                date = Mapper.parseStringToEpoch(entity.date),
                avatar = entity.avatar,
                likes = entity.likes,
                comments = entity.comments,
                shared = entity.shares,
                views = entity.views,
                isLiked = entity.isLiked,
                video = entity.video,
                attachment = entity.attachment,
                isOwner = entity.isOwner
            )
        }

        fun parserNotNull(entity: PostEntity): Post {
            return Post(
                id = entity.id,
                title = entity.title,
                text = entity.text,
                authorId = entity.authorId,
                date = Mapper.parseStringToEpoch(entity.date),
                avatar = entity.avatar,
                likes = entity.likes,
                comments = entity.comments,
                shared = entity.shares,
                views = entity.views,
                isLiked = entity.isLiked,
                video = entity.video,
                attachment = entity.attachment,
                isOwner = entity.isOwner
            )
        }

        fun parser(response: PostResponse?): Post? {
            if (response == null) return null
            return Post(
                id = response.id,
                title = response.title,
                text = response.text,
                authorId = response.authorId,
                avatar = response.avatar,
                date = response.date,
                likes = response.likes,
                isLiked = response.isLiked,
                attachment = response.attachment,
                isOwner = response.isOwner
            )
        }
    }
}

@Parcelize
data class PostTimeSeparator(val time: String): Parcelable, PostAdapterEntity
