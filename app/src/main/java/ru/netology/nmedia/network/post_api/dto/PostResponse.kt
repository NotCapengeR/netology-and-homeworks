package ru.netology.nmedia.network.post_api.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.utils.Mapper
import java.time.OffsetDateTime

@Parcelize
data class PostResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("author") val title: String,
    @SerializedName("content") val text: String,
    @SerializedName("published") val date: Long,
    @SerializedName("likedByMe") val isLiked: Boolean,
    @SerializedName("likes") val likes: Int
) : Parcelable {

    companion object {

        fun parser(entity: PostEntity): PostResponse {
            return PostResponse(
                id = entity.id,
                title = entity.title,
                text = entity.text,
                date = OffsetDateTime.parse(entity.date, Mapper.formatter).toEpochSecond(),
                isLiked = entity.isLiked,
                likes = entity.likes
            )
        }

        fun parser(post: Post): PostResponse {
            return PostResponse(
                id = post.id,
                title = post.title,
                text = post.text,
                date = post.date,
                isLiked = post.isLiked,
                likes = post.likes
            )
        }
    }
}