package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.ui.base.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

class PostViewModel @Inject constructor(
    application: Application,
    private val repository: PostRepository
) : BaseViewModel(application) {

    val postsList: LiveData<List<Post>> = repository.getAllPosts()
        .catch { Timber.e("Exception occurred: ${it.message ?: it.toString()}") }
        .asLiveData()

    fun addPost(
        title: String,
        text: String,
        url: String? = null
    ) {
        viewModelScope.launch {
            repository.addPost(title, text).also {
                if (it > 0L && url != null) {
                    addVideo(url, it)
                }
            }
        }
    }

    private fun addVideo(url: String, id: Long) {
        repository.addVideo(url, id)
    }

    fun removeLink(id: Long) {
        viewModelScope.launch {
            repository.removeLink(id)
        }
    }

    fun removePost(id: Long) {
        viewModelScope.launch {
            repository.removePost(id)
        }
    }

    fun likePost(id: Long) {
        viewModelScope.launch {
            repository.likePost(id)
        }
    }

    fun sharePost(id: Long) {
        viewModelScope.launch {
            repository.sharePost(id)
        }
    }

    fun commentPost(id: Long) {
        viewModelScope.launch {
            repository.commentPost(id)
        }
    }

    private suspend fun getPostById(id: Long): Post? = repository.getPostById(id)

}




