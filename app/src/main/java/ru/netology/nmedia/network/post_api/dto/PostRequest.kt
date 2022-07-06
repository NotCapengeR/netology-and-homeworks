package ru.netology.nmedia.network.post_api.dto

import android.os.Parcelable
import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import ru.netology.nmedia.repository.dto.Attachment
import java.time.OffsetDateTime

@Parcelize
data class PostRequest(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("author") val title: String = "",
    @SerializedName("authorAvatar") val avatar: String = "",
    @SerializedName("content") val text: String = "",
    @SerializedName("published") val date: Long = OffsetDateTime.now().toEpochSecond(),
    @SerializedName("likedByMe") val isLiked: Boolean = false,
    @SerializedName("likes") val likes: Int = 0,
    @SerializedName("attachment") val attachment: Attachment? = null
) : Parcelable {

    companion object {
        val EMPTY_POST_REQUEST = PostRequest(
            id = 0L,
            title = "",
            text = "",
            avatar = "",
            date = 0L,
            isLiked = false,
            likes = 0,
            attachment = null
        )
    }
}
