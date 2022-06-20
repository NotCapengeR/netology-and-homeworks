package ru.netology.nmedia.database.dao

import android.text.format.DateFormat
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.dto.Post
import java.util.*

@Dao
interface PostDAO {

    @Query("SELECT * FROM posts WHERE id > 0")
    fun getAll(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    fun getPostById(id: Long): PostEntity

    @Query("DELETE FROM posts WHERE id = :id")
    fun deletePostById(id: Long): Int

    @Query("INSERT INTO posts (title, text, date) VALUES(:title, :text, :date)")
    fun addPost(
        title: String,
        text: String,
        date: String = DateFormat.format(Post.POST_DATE_ABSOLUTE, Date().time).toString()
    ): Long

    @Query("UPDATE posts SET title = :newTitle, text = :newText WHERE id = :id")
    fun editPost(id: Long, newTitle: String, newText: String): Int

    @Query("UPDATE posts SET yt_id = :ytId, yt_author = :ytAuthor, yt_title = :ytTitle," +
            " yt_duration = :ytDuration, yt_thumbnail_url = :ytThumbnail WHERE id = :id"
    )
    fun addVideo(
        id: Long,
        ytId: String?,
        ytAuthor: String?,
        ytTitle: String?,
        ytDuration: String?,
        ytThumbnail: String?
    )

    @Query("UPDATE posts SET yt_id = NULL, yt_author = NULL, yt_title = NULL," +
            " yt_duration = NULL, yt_thumbnail_url = NULL WHERE id = :id"
    )
    fun removeVideo(id: Long): Int

    @Query("UPDATE posts SET likes_count = :newLikesCount, is_liked = :changed WHERE id = :id")
    fun likePostById(id: Long, newLikesCount: Int, changed: Boolean): Int

    @Query("UPDATE posts SET comments_count = :newCommentsCount WHERE id = :id")
    fun commentPostById(id: Long, newCommentsCount: Int)

    @Query("UPDATE posts SET share_count = :newSharedCount WHERE id = :id")
    fun sharePostById(id: Long, newSharedCount: Int)

    @Insert
    fun insertAll(vararg posts: PostEntity)

    @Insert
    fun insertPost(post: PostEntity)

    @Delete
    fun deletePost(post: PostEntity)
}