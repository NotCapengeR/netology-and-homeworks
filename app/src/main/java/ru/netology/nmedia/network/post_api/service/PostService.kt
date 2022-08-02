package ru.netology.nmedia.network.post_api.service

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.network.post_api.dto.PostRequest
import ru.netology.nmedia.network.post_api.dto.PostResponse

interface PostService {

    companion object {
        const val BASE_URL: String = "http://10.0.2.2:9999/api/"
    }

    @GET("posts")
    suspend fun getAll(): Response<List<PostResponse>>

    @GET("posts/{post_id}/newer")
    suspend fun getNewer(@Path("post_id") id: Long): Response<List<PostResponse>>

    @GET("posts/{post_id}")
    suspend fun getPostById(@Path("post_id") id: Long): Response<PostResponse>

    @POST("posts")
    suspend fun createPost(@Body request: PostRequest): Response<PostResponse>

    @POST("posts")
    suspend fun editPost(@Body request: PostRequest): Response<PostResponse>

    @DELETE("posts/{post_id}")
    suspend fun deletePostById(@Path("post_id") id: Long): Response<Unit>

    @POST("posts/{post_id}/likes")
    suspend fun likePostById(@Path("post_id") id: Long): Response<PostResponse>

    @DELETE("posts/{post_id}/likes")
    suspend fun dislikePostById(@Path("post_id") id: Long): Response<PostResponse>
}