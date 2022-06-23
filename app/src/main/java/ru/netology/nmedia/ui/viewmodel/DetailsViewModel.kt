package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.withContext
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.ui.base.BaseViewModel
import javax.inject.Inject

class DetailsViewModel @Inject constructor(
    private val repository: PostRepository,
    application: Application
) : BaseViewModel(application) {

    val post: MutableLiveData<Post> by lazy {
        MutableLiveData(Post.EMPTY_POST)
    }


    suspend fun removeLink(id: Long): Boolean {
        return withContext(viewModelScope.coroutineContext) {
            repository.removeLink(id)
        }
    }

    suspend fun removePost(id: Long): Boolean {
        return withContext(viewModelScope.coroutineContext) {
            repository.removePost(id)
        }
    }


    suspend fun likePost(id: Long): Boolean {
        return withContext(viewModelScope.coroutineContext) {
            repository.likePost(id)
        }
    }

    suspend fun sharePost(id: Long): Int {
        return withContext(viewModelScope.coroutineContext) {
            repository.sharePost(id)
        }
    }

    suspend fun commentPost(id: Long): Int {
        return withContext(viewModelScope.coroutineContext) {
            repository.commentPost(id)
        }
    }

    fun getPostById(id: Long): Post? = repository.getPostById(id)

    fun loadPost(postId: Long) {
        post.value = repository.getPostById(postId) ?: Post.EMPTY_POST
    }
}