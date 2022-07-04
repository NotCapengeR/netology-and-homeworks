package ru.netology.nmedia.network.youtube

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    companion object {
        const val BASE_URL: String = "https://www.googleapis.com/youtube/v3/"
        private const val API_KEY: String = "AIzaSyDnRiiqBRnJedogYjvxmBzqKQbYPBFz9l0"
    }

    @GET("videos")
    fun getVideoData(
        @Query("id")
        id: String,
        @Query("key")
        key: String = API_KEY,
        @Query("part")
        parts: String = "snippet, contentDetails",
        @Query("field")
        fields: String = "items(id, etag, snippet, contentDetails)"
    ) : Call<YouTubeVideo>
}