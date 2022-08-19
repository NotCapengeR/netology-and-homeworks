package ru.netology.nmedia.repository.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.netology.nmedia.network.youtube.YouTubeVideo
import ru.netology.nmedia.utils.Mapper

@Parcelize
data class YouTubeVideoData(
    val id: String,
    val author: String,
    val title: String,
    val duration: String,
    val thumbnailUrl: String
) : Parcelable {

    companion object {
        const val YOUTUBE_URL: String = "https://www.youtube.com/watch?v="
        fun parser(data: YouTubeVideo?): YouTubeVideoData? {
            return if (data == null) {
                null
            } else YouTubeVideoData(
                id = data.items.first().id,
                author = data.items.first().snippet.channelTitle,
                title = data.items.first().snippet.title,
                duration = Mapper.formatYTDate(
                    data.items.first().contentDetails.duration
                        .replace("PT", "")
                        .replace('S', ' ')
                        .replace('H', ':')
                        .replace('M', ':')
                ),
                thumbnailUrl = data.items.first().snippet.thumbnails.thumbnail.url
            )
        }
    }
}