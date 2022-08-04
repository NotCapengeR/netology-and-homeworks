package ru.netology.nmedia.repository.dto

import android.net.Uri
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.File

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

    companion object {
        fun attachmentFromMedia(media: Media?): Attachment? {
            if (media == null) return null
            return Attachment(
                name = media.id,
                description = "",
                type = AttachmentType.IMAGE
            )
        }
    }
}

@Parcelize
data class Media(val id: String) : Parcelable

@Parcelize
data class Photo(val file: File?, val uri: Uri?) : Parcelable {

    companion object {
        val NO_PHOTO: Photo = Photo(null, null)
    }
}

