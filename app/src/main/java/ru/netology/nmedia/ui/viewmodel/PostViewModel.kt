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

    val postsList: MutableLiveData<List<Post>> by lazy {
        MutableLiveData<List<Post>>()
    }

    init {
        mutablePostsList.addAll(postRepository.getPosts())
        loadData()
        viewModelScope.launch {
            addVideo("https://www.youtube.com/watch?v=1dOsef2ZzQ8", 1)
        }
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
        val index = mutablePostsList.indexOf(postRepository.getPostById(id))
        if (index == -1) return
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.addVideo(url, id)
        }
        delay(1500L)
        postRepository.getPostById(id)?.let {
            if (it.video != null) {
                mutablePostsList[index] = it
                loadData()
            }
        }
    }

    fun removeLink(id: Long): Boolean {
        val post = postRepository.getPostById(id) ?: return false
        return postRepository.removeLink(id).also {
            if (it) {
                val newPost = postRepository.getPostById(id) ?: return false
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                loadData()
            }
        }
    }

    fun removePost(id: Long): Boolean {
        val post = postRepository.getPostById(id) ?: return false
        return postRepository.removePost(id).also {
            if (it) {
                mutablePostsList.remove(post)
                loadData()
            }
        }
    }

    fun editPost(id: Long, newText: String, url: String? = null): Boolean {
        val post = postRepository.getPostById(id) ?: return false
        return postRepository.editPost(id, newText).also {
            if (it) {
                val newPost = postRepository.getPostById(id) ?: return false
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
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
        val post = postRepository.getPostById(id) ?: return false
        return postRepository.likePost(id).also {
            if (it) {
                val newPost = postRepository.getPostById(id) ?: return false
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                loadData()
            }
        }
    }

    fun sharePost(id: Long): Int {
        val post = postRepository.getPostById(id) ?: return -1
        return postRepository.sharePost(id).also {
            if (it > 0) {
                val newPost = postRepository.getPostById(id) ?: return -2
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                loadData()
            }
        }
    }

    fun commentPost(id: Long): Int {
        val post = postRepository.getPostById(id) ?: return -1
        return postRepository.commentPost(id).also {
            if (it > 0) {
                val newPost = postRepository.getPostById(id) ?: return -2
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                loadData()
            }
        }
    }

    fun movePost(id: Long, movedBy: Int): Boolean {
        val post = postRepository.getPostById(id) ?: return false
        val postIndex = mutablePostsList.indexOf(post)
        val swappablePostIndex = postIndex - movedBy
        return try {
            Collections.swap(mutablePostsList, postIndex, swappablePostIndex)
            loadData()
            postsList.value == mutablePostsList.toList()
        } catch (ex: ArrayIndexOutOfBoundsException) {
            return false
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            postsList.value = mutablePostsList.toList()
        }
    }
}




