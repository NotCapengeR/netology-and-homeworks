package ru.netology.nmedia.network.post_api.service

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.network.post_api.dto.PostRequest
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.post_api.dto.PushToken
import ru.netology.nmedia.repository.auth.AuthData
import ru.netology.nmedia.repository.dto.Media

interface PostService {

    companion object {
        const val BASE_URL: String = "http://10.0.2.2:9999/api/"
    }

    @POST("users/push-tokens")
    suspend fun savePushToken(token: PushToken): Response<Unit>

    @GET("posts")
    suspend fun getAll(): Response<List<PostResponse>>

    @GET("posts/{post_id}/newer")
    suspend fun getNewer(@Path("post_id") id: Long): Response<List<PostResponse>>

    @Multipart
    @POST("media")
    suspend fun uploadImage(@Part media: MultipartBody.Part): Response<Media>

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

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun login(
        @Field("login") login: String,
        @Field("pass") password: String
    ): Response<AuthData>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun register(
        @Field("login") login: String,
        @Field("pass") password: String,
        @Field("name") name: String
    ): Response<AuthData>

    @Multipart
    @POST("users/registration")
    suspend fun registerWithPhoto(
        @Part("login") login: RequestBody,
        @Part("pass") password: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part,
    ): Response<AuthData>
}