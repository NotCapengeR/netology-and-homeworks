package ru.netology.nmedia.ui.fragments

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.auth.AuthData
import ru.netology.nmedia.repository.auth.AuthManager
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.repository.dto.PostAdapterEntity
import ru.netology.nmedia.ui.base.BaseViewModel
import javax.inject.Inject

class PostViewModel @Inject constructor(
    application: Application,
    private val authManager: AuthManager,
    private val repository: PostRepository
) : BaseViewModel(application) {

    val posts: Flow<PagingData<PostAdapterEntity>> = repository.posts.cachedIn(viewModelScope)
    val authData: LiveData<AuthData> =
        authManager.authData.asLiveData(Dispatchers.Default)


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


    fun setAuth(id: Long, token: String) {
        return authManager.setAuth(id, token)
    }

    fun clearAuth() {
        return authManager.clearAuth()
    }

    suspend fun getDBSize(): Int = repository.getDBSize()


    fun getAuthId(): Long = repository.getAuthId()

}