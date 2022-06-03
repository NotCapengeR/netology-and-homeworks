package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    val tag: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val postsList: MutableLiveData<List<Post>> by lazy {
        MutableLiveData<List<Post>>()
    }

    init {
        mutablePostsList.addAll(postRepository.getPosts())
        loadData()
    }

    fun currentTag(tag: String?) {
        this.tag.value = tag
    }

    fun addPost(title: String, text: String): Long = postRepository.addPost(title, text).also {
        if (it > 0) {
            val post = postRepository.getPostById(it) ?: return -1
            mutablePostsList.add(post)
            loadData()
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

    fun editPost(id: Long, newText: String): Boolean {
        val post = postRepository.getPostById(id) ?: return false
        return postRepository.editPost(id, newText).also {
            if (it) {
                val newPost = postRepository.getPostById(id) ?: return false
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                loadData()
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



