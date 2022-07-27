package ru.netology.nmedia.ui.fragments

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.SyncHelper
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.ui.base.BaseViewModel
import ru.netology.nmedia.utils.Mapper
import ru.netology.nmedia.utils.getErrorMessage
import timber.log.Timber
import javax.inject.Inject

class PostViewModel @Inject constructor(
    application: Application,
    private val repository: PostRepository
) : BaseViewModel(application) {

    private val _postsList: MutableLiveData<NetworkResult<List<PostResponse>>> = MutableLiveData()
    val postsList: LiveData<NetworkResult<List<PostResponse>>> = _postsList
    private val needLoading: MutableLiveData<Boolean> by lazy {
        MutableLiveData(false) // заготовка для следующих дз
    }

    private fun addVideo(url: String, id: Long) {
        repository.addVideo(url, id)
    }

    init {
        loadToCurrentData().also {
            fetchData()
        }
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

    fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository is SyncHelper) {
                repository.pingServer().also { result ->
                    if (result !is NetworkResult.Success) {
                        withContext(Dispatchers.Main) {
                            _postsList.value = result
                            loadToCurrentData()
                        }
                    }
                }
            }
        }
    }

    private fun loadToCurrentData() {
        viewModelScope.launch(Dispatchers.Main) {
            repository.getPostsFromDB()
                .map { posts ->
                    Mapper.mapPostsToResponse(posts)
                }
                .catch { Timber.e("Exception occurred while loading data from DB: ${it.getErrorMessage()}") }
                .flowOn(Dispatchers.IO)
                .collect { result ->
                    _postsList.value = result
                }
        }
    }

    private suspend fun getPostById(id: Long): Post? {
        return withContext(viewModelScope.coroutineContext + Dispatchers.Main) {
            repository.getPostById(id)
        }
    }
}