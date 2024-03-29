package ru.netology.nmedia.ui.fragments.edit

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.nmedia.R
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.ui.base.BaseViewModel
import ru.netology.nmedia.utils.SingleLiveEvent
import timber.log.Timber
import javax.inject.Inject

class EditViewModel @Inject constructor(
    private val repository: PostRepository,
    application: Application
) : BaseViewModel(application) {

    val post: MutableLiveData<Post> by lazy {
        MutableLiveData(Post.EMPTY_POST)
    }
    val isUpdating: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoaded: MutableLiveData<Boolean> = MutableLiveData(false)
    val errorMsg: SingleLiveEvent<String> = SingleLiveEvent()


    fun clearErrorMsg() {
        errorMsg.call()
    }

    fun removeLink(id: Long) {
        viewModelScope.launch {
            repository.removeLink(id)
        }
    }

    fun saveText(text: CharSequence) {
        saveText(text = text.toString())
    }


    fun saveText(text: String) {
        post.value = post.value?.copy(text = text)
    }

    fun editPost(
        id: Long,
        newText: String,
        url: String? = null
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            isUpdating.value = true
            Timber.d("Post was edited: $id")
            repository.editPost(id, newText).also {
                isUpdating.value = false
                if (it) {
                    isLoaded.value = true
                    isLoaded.value = false
                } else {
                    errorMsg.value = getString(R.string.error_problem_with_internet_connection)
                }
            }
        }
    }


    fun likePost(id: Long, text: String) {
        viewModelScope.launch {
            repository.likePost(id).also {
                if (it) {
                    val newPost = getPostById(id) ?: return@also
                    post.value = post.value?.copy(
                        likes = newPost.likes,
                        isLiked = newPost.isLiked,
                        text = text
                    )
                }
            }
        }
    }

    fun sharePost(id: Long) {
        viewModelScope.launch {
            repository.sharePost(id).also {
                if (it > 0) {
                    val newPost = getPostById(id) ?: return@also
                    post.value = post.value?.copy(shared = newPost.shared)
                }
            }
        }
    }

    fun commentPost(id: Long) {
        viewModelScope.launch {
            repository.commentPost(id).also {
                if (it > 0) {
                    val newPost = getPostById(id) ?: return@also
                    post.value = post.value?.copy(comments = newPost.comments)
                }
            }
        }
    }

    private fun addVideo(url: String, id: Long) {
        repository.addVideo(url, id)
    }

    private suspend fun getPostById(id: Long): Post? {
        return withContext(viewModelScope.coroutineContext + Dispatchers.Main) {
            repository.getPostFromDBById(id)
        }
    }

    fun loadPost(postId: Long, onLoad: (Post?) -> Unit) {
        viewModelScope.launch {
            val previousPost = post.value
            val newPost = getPostById(postId) ?: return@launch
            if (previousPost?.id == newPost.id) {
                post.value = newPost.copy(text = previousPost.text)
            } else {
                post.value = newPost
            }
            onLoad.invoke(post.value)
        }
    }
}