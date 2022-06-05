package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.network.ApiService
import ru.netology.nmedia.network.YouTubeVideo
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val service: ApiService
) : PostRepository {

    private val posts: MutableMap<Long, Post> = HashMap()
    private val removedPosts: MutableMap<Long, Post> = HashMap()
    private var postId: Long = 1L

    private fun findPostById(id: Long): PostSearchResult {
        val post = posts[id] ?: return PostSearchResult.NotFound
        return PostSearchResult.Success(post)
    }

    private fun getIdFromYouTubeLink(link: String?): String? {
        if (link == null) return null
        val matcher = COMPILED_PATTERN.matcher(link)
        return if (matcher.find()) {
            matcher.group(1)
        } else null

    }

    override fun getPosts(): MutableList<Post> = posts.values.toMutableList()

    override fun getPostById(id: Long): Post? = findPostById(id).post

    override fun addPost(title: String, text: String): Long {
        val post = Post(postId, title, text, Date().time, R.mipmap.ic_launcher)
        posts[postId] = post
        postId++
        return post.id
    }

    override suspend fun addImage(url: String, postId: Long) = withContext(Dispatchers.IO) {
        val id = getIdFromYouTubeLink(url)
        val post = getPostById(postId)
        if (id != null && post != null) {
            service.getVideoData(id).enqueue(object : Callback<YouTubeVideo> {
                override fun onResponse(
                    call: Call<YouTubeVideo>,
                    response: Response<YouTubeVideo>
                ) {
                    Timber.d("Response code: ${response.code()}")
                    if (response.code() == 200) {
                        posts[postId] = post.copy(video = response.body())
                    }
                }

                override fun onFailure(call: Call<YouTubeVideo>, t: Throwable) {
                    Timber.e("Something went wrong: $t")
                }

            })
        }
    }

    override fun removePost(id: Long): Boolean {
        val post = findPostById(id).post ?: return false
        posts.remove(id)
        removedPosts[id] = post
        return !posts.containsValue(post)
    }

    override fun editPost(id: Long, newText: String): Boolean {
        val post = findPostById(id).post ?: return false
        val newPost = post.copy(text = newText)
        newPost.editHistory.add(post.text)
        posts[id] = newPost
        return posts.containsValue(newPost)
    }

    override fun likePost(id: Long): Boolean {
        val post = findPostById(id).post ?: return false
        val previousLikesCount = post.likes
        if (post.isLiked) {
            posts[id] = post.copy(likes = previousLikesCount - 1, isLiked = !post.isLiked)
        } else {
            posts[id] = post.copy(likes = previousLikesCount + 1, isLiked = !post.isLiked)
        }
        return true
    }

    override fun sharePost(id: Long): Int {
        val post = findPostById(id).post ?: return -1
        val nextValue = post.shared + 450
        posts[id] = post.copy(shared = nextValue)
        return nextValue
    }

    override fun commentPost(id: Long): Int {
        val post = findPostById(id).post ?: return -1
        val nextValue = post.comments + 450
        posts[id] = post.copy(comments = nextValue)
        return nextValue
    }

    override fun onPostMoved(id: Long, movedBy: Int): Pair<Post, Post>? {
        return null
        //  может когда-нибудь придётся переделать...
        //        val post = findPostById(id).post ?: return null
        //        val postsList = getPosts()
        //        val postIndex = postsList.indexOf(post)
        //        return try {
        //            val swappablePost = postsList[postIndex - movedBy]
        //            Pair(post, swappablePost)
        //        } catch (ex: ArrayIndexOutOfBoundsException) {
        //            null
        //        }
    }

    private companion object {
        private const val URL_PATTERN: String =
            "https?:\\/\\/(?:[0-9A-Z-]+\\.)?(?:youtu\\.be\\/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|<\\/a>))[?=&+%\\w]*"
        private val COMPILED_PATTERN: Pattern =
            Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE)
    }

}