package ru.netology.nmedia.service

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NewPost(
    val id: Long,
    val author: String,
    val title: String,
    val text: String
): Parcelable
