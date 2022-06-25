package ru.netology.nmedia.network.post_api.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class PostRequest(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("author") val title: String,
    @SerializedName("content") val text: String,
    @SerializedName("published") val date: Long = Date().time,
    @SerializedName("likedByMe") val isLiked: Boolean = false,
    @SerializedName("likes") val likes: Int = 0
) : Parcelable