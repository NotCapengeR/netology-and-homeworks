package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.netology.nmedia.database.dto.Post
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.ui.base.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

class PostViewModel @Inject constructor(
    application: Application,
    private val repository: PostRepository
) : BaseViewModel(application) {

    private val _postsList: MutableLiveData<NetworkResult<List<PostResponse>>> = MutableLiveData()
    val postsList: LiveData<NetworkResult<List<PostResponse>>> = _postsList

    fun addPost(
        title: String,
        text: String,
        url: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPost(title, text).also {
                if (it > 0L) loadData()
//                if (it > 0L && url != null) {
//                    addVideo(url, it)
//                }
            }
        }
    }

    init {
        loadData()
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
            repository.removePost(id).also {
                if (it) loadData()
            }
        }
    }

    fun likePost(id: Long) {
        viewModelScope.launch {
            repository.likePost(id).also {
                if (it) loadData()
            }
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

    fun updateLiveData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_postsList != repository.getAllPosts().asLiveData()) {
                loadData()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.Main) {
            repository.getAllPosts()
                .catch { Timber.e("Exception occurred: ${it.message ?: it.toString()}") }
                .flowOn(Dispatchers.IO)
                .collect { _postsList.value = it }
        }
    }

    private suspend fun getPostById(id: Long): Post? = repository.getPostById(id)

}




