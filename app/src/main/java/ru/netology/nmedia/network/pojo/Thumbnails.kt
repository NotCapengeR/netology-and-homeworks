package ru.netology.nmedia.network.pojo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Thumbnails (
	@SerializedName("high") val thumbnail : Thumbnail
) : Parcelable