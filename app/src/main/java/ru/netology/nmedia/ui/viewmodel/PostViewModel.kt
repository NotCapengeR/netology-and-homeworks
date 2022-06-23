package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.withContext
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.ui.base.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

class PostViewModel @Inject constructor(
    application: Application,
    private val postRepository: PostRepository
) : BaseViewModel(application) {

    val postsList: LiveData<List<Post>> = postRepository.getAllPosts().asLiveData()

    suspend fun addPost(
        title: String,
        text: String,
        url: String? = null
    ): Long {
        return withContext(viewModelScope.coroutineContext) {
            postRepository.addPost(title, text).also {
                if (it > 0L && url != null) {
                    addVideo(url, it)
                }
            }
        }
    }

    private fun addVideo(url: String, id: Long) {
        postRepository.addVideo(url, id)
    }

    suspend fun removeLink(id: Long): Boolean {
        return withContext(viewModelScope.coroutineContext) {
            postRepository.removeLink(id)
        }
    }

    suspend fun removePost(id: Long): Boolean {
        return withContext(viewModelScope.coroutineContext) {
            postRepository.removePost(id)
        }
    }

    suspend fun editPost(
        id: Long,
        newText: String,
        newTitle: String,
        url: String? = null
    ): Boolean {
        return withContext(viewModelScope.coroutineContext) {
            Timber.d("Post was edited: $id")
            postRepository.editPost(id, newText, newTitle).also {
                if (it && url != null) {
                    addVideo(url, id)
                }
            }
        }
    }

    suspend fun likePost(id: Long): Boolean {
        return withContext(viewModelScope.coroutineContext) {
            postRepository.likePost(id)
        }
    }

    suspend fun sharePost(id: Long): Int {
        return withContext(viewModelScope.coroutineContext) {
            postRepository.sharePost(id)
        }
    }

    suspend fun commentPost(id: Long): Int {
        return withContext(viewModelScope.coroutineContext) {
            postRepository.commentPost(id)
        }
    }

    fun getPostById(id: Long): Post? = postRepository.getPostById(id)

}




