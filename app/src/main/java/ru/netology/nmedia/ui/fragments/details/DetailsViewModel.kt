package ru.netology.nmedia.ui.fragments.details

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.ui.base.BaseViewModel
import ru.netology.nmedia.utils.getErrorMessage
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
                if (it)  {
                    loadPost(id)
                } else {
                    showToast("Something went wrong: ${repository.getException(id)?.getErrorMessage()}!")
                }
            }
        }
    }

    fun sharePost(id: Long) {
        viewModelScope.launch {
            repository.sharePost(id).also {
                if (it > 0)  {
                    loadPost(id)
                }
            }
        }
    }

    fun commentPost(id: Long) {
        viewModelScope.launch {
            repository.commentPost(id).also {
                if (it > 0)  {
                    loadPost(id)
                }
            }
        }
    }

    private suspend fun getPostById(id: Long): Post? = repository.getPostById(id)

    fun loadPost(postId: Long) {
        viewModelScope.launch {
            val newPost = getPostById(postId) ?: return@launch
            post.value = newPost
        }
    }
}