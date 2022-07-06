package ru.netology.nmedia.repository.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Attachment(
    @SerializedName("url") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("type") val type: AttachmentType
) : Parcelable {

    @Parcelize
    enum class AttachmentType : Parcelable {
        IMAGE;

        companion object {
            fun valueOfOrNull(value: String): AttachmentType? {
                return try {
                    valueOf(value)
                } catch (ex: IllegalArgumentException) {
                    null
                }
            }
        }
    }
}

