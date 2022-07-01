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
    private var cachedIds: MutableList<Long> = mutableListOf()
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
            repository.getAllPosts().collect {
                if (postsList.value != it && it.data != null) {
                    loadData()
                } else {
                    loadToCurrentData()
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.Main) {
            val dbList = Mapper.mapPostsToResponseList(repository.getPostsFromDBAsList())
            if (postsList.value?.data != dbList) {
                _postsList.value = NetworkResult.Success(dbList)
                cachedIds = dbList.map { it.id }.toMutableList()
            }
            repository.getAllPosts()
                .catch { Timber.e("Error while loading data in ViewModel: ${it.getErrorMessage()}") }
                .flowOn(Dispatchers.IO)
                .collect {
                    _postsList.value = it
                    cachedIds =
                        it.data?.map { response -> response.id }?.toMutableList() ?: cachedIds
                }

            needLoading.value = false
        }
    }

    private fun loadToCurrentData() {
        viewModelScope.launch(Dispatchers.Main) {
            repository.getPostsFromDB()
                .map { posts ->
                    posts.filter { post ->
                        if (cachedIds.isNotEmpty()) {
                            cachedIds.contains(post.id)
                        } else true
                    }
                }
                .map { Mapper.mapPostsToResponse(it) }
                .catch { Timber.e("Exception occurred while loading data from DB: ${it.getErrorMessage()}") }
                .flowOn(Dispatchers.IO)
                .collect {
                    _postsList.value = it
                    cachedIds =
                        it.data?.map { response -> response.id }?.toMutableList() ?: cachedIds
                }
        }
    }

    private suspend fun getPostById(id: Long): Post? {
        return withContext(viewModelScope.coroutineContext + Dispatchers.Main) {
            repository.getPostById(id)
        }
    }
}




