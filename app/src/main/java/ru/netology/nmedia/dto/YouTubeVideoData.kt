package ru.netology.nmedia.dto

import ru.netology.nmedia.network.YouTubeVideo

data class YouTubeVideoData(
    val id: String,
    val author: String,
    val title: String,
    val duration: String,
    val thumbnailUrl: String
) {

    companion object {

        @JvmStatic
        fun parser(data: YouTubeVideo?): YouTubeVideoData? {
            return if (data == null) {
                null
            } else YouTubeVideoData(
                id = data.items.first().id,
                author = data.items.first().snippet.channelTitle,
                title = data.items.first().snippet.title,
                duration = data.items.first().contentDetails.duration
                    .replace("PT", "")
                    .replace('S', ' ')
                    .replace('H', ':')
                    .replace('M', ':'),
                thumbnailUrl = data.items.first().snippet.thumbnails.thumbnail.url
            )
        }
    }
}