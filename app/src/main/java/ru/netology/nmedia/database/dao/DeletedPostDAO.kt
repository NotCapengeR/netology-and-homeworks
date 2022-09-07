package ru.netology.nmedia.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import ru.netology.nmedia.database.entities.DeletedPostEntity
import ru.netology.nmedia.database.entities.PostEntity

@Dao
interface DeletedPostDAO {

    @Query("SELECT * FROM deleted_posts WHERE post_id > 0 ORDER BY post_id ASC")
    suspend fun getAllAsList(): List<DeletedPostEntity>

    @Query("SELECT post_id FROM deleted_posts WHERE post_id > 0 ORDER BY post_id ASC")
    suspend fun getAllIds(): List<Long>

    @Query("INSERT INTO deleted_posts (post_id) VALUES(:id)")
    suspend fun addNew(id: Long): Long

    @Query("DELETE FROM deleted_posts WHERE post_id = :id")
    suspend fun removeFromDeleted(id: Long): Int

    @Query("SELECT * FROM deleted_posts WHERE post_id = :id LIMIT 1")
    suspend fun getPostById(id: Long): PostEntity?

    @Insert(onConflict = REPLACE)
    suspend fun insert(post: DeletedPostEntity): Long
}