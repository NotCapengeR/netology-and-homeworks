package ru.netology.nmedia.ui.fragments.edit

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.database.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.ui.base.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

class EditViewModel @Inject constructor(
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

    fun saveText(text: String) {
        post.value = post.value?.copy(text = text)
    }

    fun saveTitle(title: String) {
        post.value = post.value?.copy(title = title)
    }


    fun editPost(
        id: Long,
        newText: String,
        url: String? = null
    ) {
        viewModelScope.launch {
            Timber.d("Post was edited: $id")
            repository.editPost(id, newText).also {
                if (it) loadAllPosts()
//                if (it && url != null) {
//                    addVideo(url, id)
//                }
            }
        }
    }


    fun likePost(id: Long) {
        viewModelScope.launch {
            repository.likePost(id).also {
                if (it)  {
                    val newPost = getPostById(id) ?: return@also
                    post.value = post.value?.copy(likes = newPost.likes, isLiked = newPost.isLiked)
                }
            }
        }
    }

    fun sharePost(id: Long) {
        viewModelScope.launch {
            repository.sharePost(id).also {
                if (it > 0)  {
                    val newPost = getPostById(id) ?: return@also
                    post.value = post.value?.copy(shared = newPost.shared)
                }
            }
        }
    }

    fun commentPost(id: Long) {
        viewModelScope.launch {
            repository.commentPost(id).also {
                if (it > 0)  {
                    val newPost = getPostById(id) ?: return@also
                    post.value = post.value?.copy(comments = newPost.comments)
                }
            }
        }
    }

    private fun addVideo(url: String, id: Long) {
        repository.addVideo(url, id)
    }

    private suspend fun getPostById(id: Long): Post? = repository.getPostById(id)

    fun loadPost(postId: Long) {
        viewModelScope.launch {
            val newPost = getPostById(postId) ?: return@launch
            post.value = newPost
        }
    }

    fun loadAllPosts() {
        repository.getAllPosts()
    }
}