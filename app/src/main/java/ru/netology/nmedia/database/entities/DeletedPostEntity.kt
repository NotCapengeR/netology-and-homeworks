package ru.netology.nmedia.database.entities

import androidx.room.*
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.repository.dto.YouTubeVideoData
import ru.netology.nmedia.utils.Mapper
import java.time.OffsetDateTime

@Entity(
    tableName = "deleted_posts",
    indices = [Index("post_id")]
)
data class DeletedPostEntity(
    @PrimaryKey
    @ColumnInfo(name = "post_id")
    val id: Long,
    @ColumnInfo(name = "title")
    val title: String? = null,
    @ColumnInfo(name = "text")
    val text: String? = null,
    @ColumnInfo(name= "author_id", defaultValue = "5")
    val authorId: Long = 5L,
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
    @ColumnInfo(name = "is_owner")
    val isOwner: Boolean = false,
    @Embedded(prefix = "yt_")
    val video: YouTubeVideoData? = null,
    @Embedded(prefix = "attachment_")
    val attachment: Attachment? = null
) {
    companion object {
        fun parser(entity: PostEntity): DeletedPostEntity {
            return DeletedPostEntity(
                id = entity.id,
                title = entity.title,
                text = entity.text,
                avatar = entity.avatar,
                date = entity.date,
                authorId = entity.authorId,
                likes = entity.likes,
                comments = entity.comments,
                shares = entity.shares,
                views = entity.views,
                isLiked = entity.isLiked,
                video = entity.video,
                attachment = entity.attachment,
                isOwner = entity.isOwner
            )
        }
    }
}
