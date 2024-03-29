package ru.netology.nmedia.network.youtube

import ru.netology.nmedia.network.pojo.Items
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class YouTubeVideo(
    @SerializedName("items") val items : List<Items>
) : Parcelable
