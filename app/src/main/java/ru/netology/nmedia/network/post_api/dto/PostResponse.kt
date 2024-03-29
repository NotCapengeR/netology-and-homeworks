package ru.netology.nmedia.network.post_api.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.utils.Mapper
import java.time.OffsetDateTime

@Parcelize
data class PostResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("author") val title: String,
    @SerializedName("authorId") val authorId: Long,
    @SerializedName("content") val text: String,
    @SerializedName("authorAvatar") val avatar: String,
    @SerializedName("published") val date: Long,
    @SerializedName("likedByMe") val isLiked: Boolean,
    @SerializedName("likes") val likes: Int,
    @SerializedName("attachment") val attachment: Attachment?,
    @SerializedName("ownedByMe") val isOwner: Boolean
) : Parcelable {

    companion object {
        val EMPTY_POST_RESPONSE = PostResponse(
            id = 0L,
            title = "",
            authorId = 0L,
            text = "",
            avatar = "",
            date = 0L,
            isLiked = false,
            likes = 0,
            attachment = null,
            isOwner = false
        )

        fun parser(entity: PostEntity): PostResponse {
            return PostResponse(
                id = entity.id,
                title = entity.title,
                text = entity.text,
                authorId = entity.authorId,
                avatar = entity.avatar,
                date = OffsetDateTime.parse(entity.date, Mapper.formatter).toEpochSecond(),
                isLiked = entity.isLiked,
                likes = entity.likes,
                attachment = entity.attachment,
                isOwner = entity.isOwner
            )
        }

        fun parser(post: Post): PostResponse {
            return PostResponse(
                id = post.id,
                title = post.title,
                text = post.text,
                authorId = post.authorId,
                avatar = post.avatar,
                date = post.date,
                isLiked = post.isLiked,
                likes = post.likes,
                attachment = post.attachment,
                isOwner = post.isOwner
            )
        }
    }
}