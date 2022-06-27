package ru.netology.nmedia.utils

import ru.netology.nmedia.database.dto.Post
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.results.NetworkResult
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

object Mapper {
    private val SIMPLE_POST_FORMAT = SimpleDateFormat("d MMMM yyyy, HH:mm")
    private val ABSOLUTE_POST_FORMAT = SimpleDateFormat("dd-MM-yyyy, HH:mm:ss")
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(Post.POST_DATE_ABSOLUTE)

    fun mapEntitiesToResponse(entities: List<PostEntity>): NetworkResult<List<PostResponse>> {
        return NetworkResult.Success(data = entities.map { PostResponse.parser(it) })
    }


    fun parseEpochSeconds(epoch: Long): String {
        val date = Date(epoch * 1000L)
        return SIMPLE_POST_FORMAT.format(date)
    }

    fun parseEpochToAbsolute(epoch: Long): String {
        val date = Date(epoch * 1000L)
        return ABSOLUTE_POST_FORMAT.format(date)
    }

    fun parseStringToEpoch(date: String): Long {
        return (ABSOLUTE_POST_FORMAT.parse(date)?.time?.div(1000))
            ?: throw IllegalArgumentException("Invalid date pattern")
    }

    fun mapPostsToResponse(posts: List<Post>): NetworkResult<List<PostResponse>> {
        return NetworkResult.Success(data = mapPostsToResponseList(posts))
    }

    fun mapPostsToResponseList(posts: List<Post>): List<PostResponse> {
        return posts.map { PostResponse.parser(it) }
            .reversed()
    }

    fun mapNetworkResultToPostsResponseList(result: NetworkResult<List<PostResponse>>): List<PostResponse> {
        return result.data ?: emptyList()
    }

    fun mapEntitiesToPosts(entities: List<PostEntity>): List<Post> =
        entities.mapNotNull { Post.parser(it) }

    fun mapResponsesToPosts(responses: List<PostResponse>): List<Post> =
        responses.mapNotNull { Post.parser(it) }
}
