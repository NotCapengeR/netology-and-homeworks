package ru.netology.nmedia.network.pojo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Items (
    @SerializedName("etag") val etag : String,
    @SerializedName("id") val id : String,
    @SerializedName("snippet") val snippet : Snippet,
    @SerializedName("contentDetails") val contentDetails : ContentDetails,
) : Parcelable