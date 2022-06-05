package ru.netology.nmedia.network.pojo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContentDetails (
	@SerializedName("duration") val duration: String,
	@SerializedName("dimension") val dimension: String,
	@SerializedName("definition") val definition: String,
) : Parcelable