package ru.netology.nmedia.service

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String
) : Parcelable
