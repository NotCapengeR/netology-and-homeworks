package ru.netology.nmedia.network.post_api.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PushToken(
    val token: String
) : Parcelable

@Parcelize
data class PushMessage(
    val recipientId: Long?,
    val content: String?
) : Parcelable
