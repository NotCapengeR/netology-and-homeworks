package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.repository.dto.Photo
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.repository.dto.PostAdapterEntity

interface PostRepository {

    val posts: Flow<PagingData<PostAdapterEntity>>

    suspend fun getDBSize(): Int

    fun getPostsFromDB(): Flow<List<Post>>

    suspend fun getPostsFromDBAsList(): List<Post>

    suspend fun addPost(title: String, text: String, attachment: Attachment? = null): Long

    suspend fun addPostWithAttachment(title: String, text: String, photo: Photo?): Long

    suspend fun getDeletedPostsIds(): List<Long>

    fun addVideo(url: String, postId: Long)

    suspend fun removeLink(id: Long): Boolean

    suspend fun removePost(id: Long): Boolean

    suspend fun editPost(id: Long, newText: String): Boolean

    suspend fun getPostById(id: Long): Post?

    suspend fun getPostFromDBById(id: Long): Post?

    suspend fun likePost(id: Long): Boolean

    suspend fun sharePost(id: Long): Int

    suspend fun commentPost(id: Long): Int

    fun getAuthId(): Long
}