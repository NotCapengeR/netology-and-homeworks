package ru.netology.nmedia.repository

import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import java.util.*

class PostRepositoryImpl : PostRepository {

    private val posts: MutableMap<Long, Post> = HashMap()
    private var postId: Long = 1L

    fun getPosts(): List<Post> = posts.values.toList()

    private fun findPostById(id: Long): PostSearchResult {
        val post = posts[id] ?: return PostSearchResult.NotFound
        return PostSearchResult.Success(post)
    }

    override fun getPostById(id: Long): Post? = findPostById(id).post

    override fun addPost(title: String, text: String): Long {
        val post = Post(postId, title, text, Date().time, R.mipmap.ic_launcher)
        posts[postId] = post
        postId++
        return post.id
    }

    override fun removePost(id: Long): Boolean {
        val post = findPostById(id).post ?: return false
        posts.remove(id)
        return !posts.containsValue(post)
    }

    override fun editPost(id: Long, newText: String): Boolean {
        val post = findPostById(id).post ?: return false
        val newPost = post.copy(text = newText)
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

    override fun onPostMoved(id: Long, movedBy: Int): Long {
        val post = findPostById(id).post ?: return -1L
        val swappablePost = posts[post.id - movedBy] ?: return -2L
        posts[post.id] = swappablePost.copy(id = post.id)
        posts[swappablePost.id] = post.copy(id = swappablePost.id)
        return swappablePost.id
    }
}