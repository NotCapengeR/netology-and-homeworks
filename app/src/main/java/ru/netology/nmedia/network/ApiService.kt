package ru.netology.nmedia.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    private companion object {
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