package ru.netology.nmedia.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.database.entities.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {

    @Query("SELECT COUNT(*) == 0 FROM post_remote_keys")
    suspend fun isEmpty(): Boolean

    @Query("SELECT MAX(id) FROM post_remote_keys")
    suspend fun max(): Long?

    @Query("SELECT MIN(id) FROM post_remote_keys")
    suspend fun min(): Long?

    @Query("SELECT (id) FROM post_remote_keys WHERE type = 'AFTER' LIMIT 1")
    suspend fun getAfter(): Long?

    @Query("SELECT (id) FROM post_remote_keys WHERE type = 'BEFORE' LIMIT 1")
    suspend fun getBefore(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: PostRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<PostRemoteKeyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg keys: PostRemoteKeyEntity)

    @Query("DELETE FROM post_remote_keys")
    suspend fun removeAll()
}