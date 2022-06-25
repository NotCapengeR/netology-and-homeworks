package ru.netology.nmedia.network.post_api.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("author") val title: String,
    @SerializedName("content") val text: String,
    @SerializedName("published") val date: Long,
    @SerializedName("likedByMe") val isLiked: Boolean,
    @SerializedName("likes") val likes: Int
) : Parcelable