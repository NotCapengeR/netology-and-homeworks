package ru.netology.nmedia.ui.fragments.details

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.ui.base.BaseViewModel
import javax.inject.Inject

class DetailsViewModel @Inject constructor(
    private val repository: PostRepository,
    application: Application
) : BaseViewModel(application) {

    val post: MutableLiveData<Post> by lazy {
        MutableLiveData(Post.EMPTY_POST)
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
            repository.likePost(id).also {
                if (it) {
                    loadPost(id)
                }
            }
        }
    }

    fun sharePost(id: Long) {
        viewModelScope.launch {
            repository.sharePost(id).also {
                if (it > 0) {
                    loadPost(id)
                }
            }
        }
    }

    fun commentPost(id: Long) {
        viewModelScope.launch {
            repository.commentPost(id).also {
                if (it > 0) {
                    loadPost(id)
                }
            }
        }
    }

    private suspend fun getPostById(id: Long): Post? {
        return withContext(viewModelScope.coroutineContext + Dispatchers.Main) {
            repository.getPostFromDBById(id).also { newPost ->
                post.value = newPost
            }
        }
    }

    fun loadPost(postId: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            val newPost = getPostById(postId) ?: return@launch
            post.value = newPost
        }
    }
}