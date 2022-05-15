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
        val post = Post(postId, title, text, R.mipmap.ic_launcher, Date().time)
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
        TODO("Not yet implemented")
    }

    override fun sharePost(id: Long): Int {
        TODO("Not yet implemented")
    }
}