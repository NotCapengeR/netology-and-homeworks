package ru.netology.nmedia.ui.fragments

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.repository.PostRepository
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

    fun addPost(
        title: String,
        text: String,
        url: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPost(title, text).also {
                if (it > 0L) {
                    loadData()
                } else {
                    withContext(Dispatchers.Main) {
                        showToast(
                            "Something went wrong." +
                                    " Check your Internet connection and try again later"
                        )
                    }
                }
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
                if (it) {
                    loadData()
                } else {
                    showToast(
                        "Something went wrong." +
                                " Check your Internet connection and try again later"
                    )
                }
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

    fun updateCurrentPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_postsList != repository.getAllPosts().asLiveData()) {
                loadToCurrentData()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.Main) {
            _postsList.value = NetworkResult.Success(
                Mapper.mapPostsToResponseList(repository.getPostsFromDBAsList())
            )
            repository.getAllPosts()
                .catch { Timber.e("Error while loading data in ViewModel: ${it.getErrorMessage()}") }
                .flowOn(Dispatchers.IO)
                .collect { _postsList.value = it }

            needLoading.value = false
        }
    }

    private fun loadToCurrentData() {
        viewModelScope.launch(Dispatchers.Main) {
            repository.getPostsFromDB()
                .map { posts ->
                    posts.filter { post ->
                        val data = (postsList.value as NetworkResult.Success).data.map { it.id }
                        data.contains(post.id)
                    }
                }
                .map { Mapper.mapPostsToResponse(it) }
                .catch { Timber.e("Exception occurred while loading data from DB: ${it.getErrorMessage()}") }
                .flowOn(Dispatchers.IO)
                .collect { _postsList.value = it }
        }
    }

    private suspend fun getPostById(id: Long): Post? = repository.getPostById(id)

}




