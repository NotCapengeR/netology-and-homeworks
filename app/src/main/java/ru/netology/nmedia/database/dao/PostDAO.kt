package ru.netology.nmedia.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.utils.Mapper
import java.time.OffsetDateTime

@Dao
interface PostDAO {

    @Query("SELECT * FROM posts WHERE post_id > 0 ORDER BY post_id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) FROM POSTS")
    suspend fun getSize(): Int

    @Query("DELETE FROM posts")
    suspend fun removeAll()

    @Query("SELECT MAX(post_id) FROM posts")
    suspend fun max(): Long?

    @Query("SELECT MIN(post_id) FROM posts")
    suspend fun min(): Long?

    @Query("SELECT * FROM posts ORDER BY post_id DESC LIMIT 300")
    fun pagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT post_id FROM posts ORDER BY post_id ASC LIMIT 1")
    suspend fun getLastId(): Long?

    @Query("SELECT * FROM posts WHERE post_id > 0 ORDER BY post_id DESC")
    suspend fun getAllAsList(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE post_id = :id LIMIT 1")
    suspend fun getPostById(id: Long): PostEntity?

    @Query("DELETE FROM posts WHERE post_id = :id")
    suspend fun deletePostById(id: Long): Int

    @Query("UPDATE posts SET likes_count = :newCount WHERE post_id = :id")
    suspend fun updateLikesCount(id: Long, newCount: Int): Int

    @Query("UPDATE posts SET text = :newText WHERE post_id = :id")
    suspend fun updateText(id: Long, newText: String): Int

    @Query("UPDATE posts SET title = :newTitle WHERE post_id = :id")
    suspend fun updateTitle(id: Long, newTitle: String): Int

    @Query("UPDATE posts SET avatar_name = :newAvatar WHERE post_id = :id")
    suspend fun updateAvatar(id: Long, newAvatar: String): Int

    @Query("UPDATE posts SET date = :newDate WHERE post_id = :id")
    suspend fun updateDate(id: Long, newDate: String): Int

    @Query(
        "UPDATE posts SET attachment_name = :name, attachment_description = :description," +
                "attachment_type = :type WHERE post_id = :id"
    )
    suspend fun updateAttachment(
        id: Long,
        name: String?,
        description: String?,
        type: Attachment.AttachmentType?
    ): Int

    @Query("INSERT INTO posts (post_id, title, text, date, avatar_name, author_id) VALUES(:id, :title, :text, :date, :avatar, :authorId)")
    suspend fun addPost(
        id: Long,
        title: String,
        text: String,
        date: String = Mapper.parseEpochToAbsolute(OffsetDateTime.now().toEpochSecond()),
        avatar: String,
        authorId: Long,
    ): Long

    @Query("UPDATE posts SET text = :newText WHERE post_id = :id")
    suspend fun editPost(id: Long, newText: String): Int

    @Query(
        "UPDATE posts SET yt_id = :ytId, yt_author = :ytAuthor, yt_title = :ytTitle," +
                " yt_duration = :ytDuration, yt_thumbnailUrl = :ytThumbnail WHERE post_id = :id"
    )
    suspend fun addVideo(
        id: Long,
        ytId: String?,
        ytAuthor: String?,
        ytTitle: String?,
        ytDuration: String?,
        ytThumbnail: String?
    )

    @Query(
        "UPDATE posts SET yt_id = NULL, yt_author = NULL, yt_title = NULL," +
                " yt_duration = NULL, yt_thumbnailUrl = NULL WHERE post_id = :id"
    )
    suspend fun removeVideo(id: Long): Int

    @Query("UPDATE posts SET likes_count = likes_count + 1, is_liked = 1 WHERE post_id = :id")
    suspend fun likePostById(id: Long): Int

    @Query("UPDATE posts SET likes_count = :newCount, is_liked = :isLiked WHERE post_id = :id")
    suspend fun setLikes(id: Long, newCount: Int, isLiked: Boolean): Int

    @Query("UPDATE posts SET likes_count = likes_count - 1, is_liked = 0 WHERE post_id = :id")
    suspend fun dislikePostById(id: Long): Int

    @Query("UPDATE posts SET comments_count = :newCommentsCount WHERE post_id = :id")
    suspend fun commentPostById(id: Long, newCommentsCount: Int)

    @Query("UPDATE posts SET share_count = :newSharedCount WHERE post_id = :id")
    suspend fun sharePostById(id: Long, newSharedCount: Int)

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(vararg posts: PostEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    @Insert(onConflict = REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)
}