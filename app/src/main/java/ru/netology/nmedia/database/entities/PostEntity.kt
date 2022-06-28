package ru.netology.nmedia.database.entities

import android.text.format.DateFormat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.R
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.utils.Mapper
import java.time.OffsetDateTime

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "date")
    val date: String = Mapper.parseEpochToAbsolute(OffsetDateTime.now().toEpochSecond()),
    @ColumnInfo(name = "avatar_id", defaultValue = R.mipmap.ic_launcher.toString())
    val avatarId: Int = R.mipmap.ic_launcher,
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
    @ColumnInfo(name = "yt_id")
    val ytId: String? = null,
    @ColumnInfo(name = "yt_title")
    val ytTitle: String? = null,
    @ColumnInfo(name = "yt_author")
    val ytAuthor: String? = null,
    @ColumnInfo(name = "yt_duration")
    val ytDuration: String? = null,
    @ColumnInfo(name = "yt_thumbnail_url")
    val ytThumbnailUrl: String? = null,
) {
    companion object {
        fun parser(post: Post): PostEntity {
            return PostEntity(
                id = post.id,
                title = post.title,
                text = post.text,
                date = DateFormat.format(Post.POST_DATE_ABSOLUTE, post.date).toString(),
                avatarId = post.avatarId,
                likes = post.likes,
                comments = post.comments,
                shares = post.shared,
                views = post.views,
                isLiked = post.isLiked,
                ytId = post.video?.id,
                ytTitle = post.video?.title,
                ytAuthor = post.video?.author,
                ytDuration = post.video?.duration,
                ytThumbnailUrl = post.video?.thumbnailUrl
            )
        }

        fun parser(response: PostResponse): PostEntity {
            return PostEntity(
                id = response.id,
                title = response.title,
                text = response.text,
                date = Mapper.parseEpochToAbsolute(response.date),
                likes = response.likes,
                isLiked = response.isLiked
            )
        }
    }
}
