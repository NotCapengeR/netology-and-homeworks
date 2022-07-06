package ru.netology.nmedia.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.utils.Mapper
import java.time.OffsetDateTime

@Dao
interface PostDAO {

    @Query("SELECT * FROM posts WHERE post_id > 0 ORDER BY post_id ASC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE post_id > 0 ORDER BY post_id ASC")
    suspend fun getAllAsList(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE post_id = :id LIMIT 1")
    suspend fun getPostById(id: Long): PostEntity?

    @Query("DELETE FROM posts WHERE post_id = :id")
    suspend fun deletePostById(id: Long): Int

    @Query("INSERT INTO posts (post_id, title, text, date) VALUES(:id, :title, :text, :date)")
    suspend fun addPost(
        id: Long,
        title: String,
        text: String,
        date: String = Mapper.parseEpochToAbsolute(OffsetDateTime.now().toEpochSecond())
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

    @Query("UPDATE posts SET likes_count = :newLikesCount, is_liked = :changed WHERE post_id = :id")
    suspend fun likePostById(id: Long, newLikesCount: Int, changed: Boolean): Int

    @Query("UPDATE posts SET comments_count = :newCommentsCount WHERE post_id = :id")
    suspend fun commentPostById(id: Long, newCommentsCount: Int)

    @Query("UPDATE posts SET share_count = :newSharedCount WHERE post_id = :id")
    suspend fun sharePostById(id: Long, newSharedCount: Int)

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(vararg posts: PostEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)
}