package ru.netology.nmedia.ui.fragments.image

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

class ImageDetailsViewModel @Inject constructor(
    application: Application,
    private val repository: PostRepository
) : BaseViewModel(application) {

    val post: MutableLiveData<Post> by lazy {
        MutableLiveData(Post.EMPTY_POST)
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