package ru.netology.nmedia.database.entities

import android.text.format.DateFormat
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.repository.dto.YouTubeVideoData
import ru.netology.nmedia.utils.Mapper
import java.time.OffsetDateTime

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    @ColumnInfo(name = "post_id")
    val id: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "avatar_name", defaultValue = "")
    val avatar: String = "",
    @ColumnInfo(name = "date")
    val date: String = Mapper.parseEpochToAbsolute(OffsetDateTime.now().toEpochSecond()),
    @ColumnInfo(name = "likes_count", defaultValue = "0")
    val likes: Int = 0,
    @ColumnInfo(name = "comments_count", defaultValue = "0")
    val comments: Int = 0,
    @ColumnInfo(name = "share_count", defaultValue = "0")
    val shares: Int = 0,
    @ColumnInfo(name = "views_count", defaultValue = "0")
    val views: Int = 0,
    @ColumnInfo(name = "is_liked")
    val isLiked: Boolean = false,
    @Embedded(prefix = "yt_")
    val video: YouTubeVideoData? = null,
    @Embedded(prefix = "attachment_")
    val attachment: Attachment? = null
) {
    companion object {
        fun parser(post: Post): PostEntity {
            return PostEntity(
                id = post.id,
                title = post.title,
                text = post.text,
                avatar = post.avatar,
                date = DateFormat.format(Post.POST_DATE_ABSOLUTE, post.date).toString(),
                likes = post.likes,
                comments = post.comments,
                shares = post.shared,
                views = post.views,
                isLiked = post.isLiked,
                video = post.video,
                attachment = post.attachment
            )
        }

        fun parser(response: PostResponse): PostEntity {
            return PostEntity(
                id = response.id,
                title = response.title,
                text = response.text,
                avatar = response.avatar,
                date = Mapper.parseEpochToAbsolute(response.date),
                likes = response.likes,
                isLiked = response.isLiked,
                attachment = response.attachment
            )
        }
    }
}
