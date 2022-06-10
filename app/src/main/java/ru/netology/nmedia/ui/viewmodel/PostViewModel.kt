package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import java.util.*
import javax.inject.Inject

class PostViewModel @Inject constructor(
    application: Application,
    private val postRepository: PostRepository
) : AndroidViewModel(application) {

    // это для перемещения постов, в мапах-то позиции нельзя менять)
    private val mutablePostsList: MutableList<Post> = mutableListOf()

    private val indexes: Map<Long, Pair<Int, Post>>
        get() = mutablePostsList.associate { post ->
            post.id to (mutablePostsList.indexOf(post) to post)
        }

    val postsList: MutableLiveData<List<Post>> by lazy {
        MutableLiveData<List<Post>>()
    }

    init {
        mutablePostsList.addAll(postRepository.getPosts())
        loadData()
    }

    fun addPost(
        title: String,
        text: String,
        url: String? = null
    ): Long {
        return postRepository.addPost(title, text).also {
            if (it > 0) {
                val post = postRepository.getPostById(it) ?: return -1L
                mutablePostsList.add(post)
                loadData()
                if (url != null) {
                    viewModelScope.launch {
                        addVideo(url, it)
                    }
                }
            }
        }
    }

    private suspend fun addVideo(url: String, id: Long) {
        val index = indexes[id]?.first ?: return
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.addVideo(url, id)
        }
        delay(1000)
        postRepository.getPostById(id)?.let {
            mutablePostsList[index] = it
            loadData()
        }
    }

    fun removeLink(id: Long): Boolean {
        val post = indexes[id] ?: return false
        return postRepository.removeLink(id).also {
            if (it) {
                val newPost = postRepository.getPostById(id) ?: return false
                mutablePostsList[post.first] = newPost
                loadData()
            }
        }
    }

    fun removePost(id: Long): Boolean {
        val post = indexes[id] ?: return false
        return postRepository.removePost(id).also {
            if (it) {
                mutablePostsList.remove(post.second)
                loadData()
            }
        }
    }

    fun editPost(id: Long, newText: String, newTitle: String, url: String? = null): Boolean {
        val post = indexes[id] ?: return false
        return postRepository.editPost(id, newText, newTitle).also {
            if (it) {
                val newPost = postRepository.getPostById(id) ?: return false
                mutablePostsList[post.first] = newPost
                loadData()
                if (url != null) {
                    viewModelScope.launch {
                        addVideo(url, id)
                    }
                }
            }
        }
    }

    fun likePost(id: Long): Boolean {
        val post = indexes[id] ?: return false
        return postRepository.likePost(id).also {
            if (it) {
                val newPost = postRepository.getPostById(id) ?: return false
                mutablePostsList[post.first] = newPost
                loadData()
            }
        }
    }

    fun sharePost(id: Long): Int {
        val post = indexes[id] ?: return -1
        return postRepository.sharePost(id).also {
            if (it > 0) {
                val newPost = postRepository.getPostById(id) ?: return -2
                mutablePostsList[post.first] = newPost
                loadData()
            }
        }
    }

    fun commentPost(id: Long): Int {
        val post = indexes[id] ?: return -1
        return postRepository.commentPost(id).also {
            if (it > 0) {
                val newPost = postRepository.getPostById(id) ?: return -2
                mutablePostsList[post.first] = newPost
                loadData()
            }
        }
    }

    fun movePost(id: Long, movedBy: Int): Boolean {
        val post = indexes[id] ?: return false
        val swappablePostIndex = post.first - movedBy
        return try {
            Collections.swap(mutablePostsList, post.first, swappablePostIndex)
            loadData()
            postsList.value == mutablePostsList.toList()
        } catch (ex: ArrayIndexOutOfBoundsException) {
            return false
        }
    }

    fun getPostById(id: Long): Post? = postRepository.getPostById(id)

    private fun loadData() {
        viewModelScope.launch {
            postsList.value = mutablePostsList.toList()
        }
    }
}




