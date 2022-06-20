package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.ui.base.BaseViewModel
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class PostViewModel @Inject constructor(
    application: Application,
    private val postRepository: PostRepository
) : BaseViewModel(application) {

    // это для перемещения постов, в мапах-то позиции нельзя менять)
    private val mutablePostsList: MutableList<Post> = mutableListOf()
    val editablePost: MutableLiveData<Post> by lazy {
        MutableLiveData(Post.EMPTY_POST)
    }
    private val indexes: Map<Long, Pair<Int, Post>>
        get() = mutablePostsList.associate { post ->
            post.id to (mutablePostsList.indexOf(post) to post)
        }
    val postsList: MutableLiveData<List<Post>> by lazy {
        MutableLiveData<List<Post>>()
    }


    fun changePost(id: Long = 0L) {
        editablePost.value = postRepository.getPostById(id) ?: Post.EMPTY_POST
    }

    @JvmOverloads
    fun interactWithPost(newTitle: String? = null, newText: String? = null) = when {
        newTitle != null && newText != null ->
            editablePost.value = editablePost.value?.copy(title = newTitle, text = newText)
        newTitle == null && newText != null ->
            editablePost.value = editablePost.value?.copy(text = newText)
        newTitle != null && newText == null ->
            editablePost.value = editablePost.value?.copy(title = newTitle)
        else -> {}
    }

    init {
        mutablePostsList.addAll(postRepository.getPosts())
        loadData()
    }

    suspend fun addPost(
        title: String,
        text: String,
        url: String? = null
    ): Long {
        return viewModelScope.async {
            postRepository.addPost(title, text).also {
                if (it > 0L) {
                    val post = postRepository.getPostById(it) ?: return@also
                    mutablePostsList.add(post)
                    loadData()
                    if (url != null) {
                        addVideo(url, it)
                    }
                }
            }
        }.await()
    }

    private suspend fun addVideo(url: String, id: Long) {
        val index = indexes[id]?.first ?: return
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.addVideo(url, id)
        }
        delay(1000)
        postRepository.getPostById(id)?.let {
            mutablePostsList[index] = it
            loadData()
        }
    }

    suspend fun removeLink(id: Long): Boolean {
        val post = indexes[id] ?: return false
        return postRepository.removeLink(id).also {
            if (it) {
                val newPost = postRepository.getPostById(id) ?: return false
                mutablePostsList[post.first] = newPost
                loadData()
            }
        }
    }

    suspend fun removePost(id: Long): Boolean {
        val post = indexes[id] ?: return false
        return withContext(viewModelScope.coroutineContext) {
            postRepository.removePost(id).also {
                if (it) {
                    mutablePostsList.remove(post.second)
                    loadData()
                }
            }
        }
    }

    suspend fun editPost(
        id: Long,
        newText: String,
        newTitle: String,
        url: String? = null
    ): Boolean {
        val post = indexes[id] ?: return false
        return withContext(viewModelScope.coroutineContext) {
            Timber.d("Post was edited: $id")
            postRepository.editPost(id, newText, newTitle).also {
                if (it) {
                    val newPost = postRepository.getPostById(id) ?: return@also
                    mutablePostsList[post.first] = newPost
                    loadData()
                    if (url != null) {
                        addVideo(url, id)
                    }
                }
            }
        }
    }

    suspend fun likePost(id: Long): Boolean {
        val post = indexes[id] ?: return false
        return withContext(viewModelScope.coroutineContext) {
            postRepository.likePost(id).also {
                if (it) {
                    val newPost = postRepository.getPostById(id) ?: return@also
                    mutablePostsList[post.first] = newPost
                    loadData()
                }
            }
        }
    }

    suspend fun sharePost(id: Long): Int {
        val post = indexes[id] ?: return -1
        return withContext(viewModelScope.coroutineContext) {
            postRepository.sharePost(id).also {
                if (it > 0) {
                    val newPost = postRepository.getPostById(id) ?: return@also
                    mutablePostsList[post.first] = newPost
                    loadData()
                }
            }
        }
    }

    suspend fun commentPost(id: Long): Int {
        val post = indexes[id] ?: return -1
        return withContext(viewModelScope.coroutineContext) {
            postRepository.commentPost(id).also {
                if (it > 0) {
                    val newPost = postRepository.getPostById(id) ?: return@also
                    mutablePostsList[post.first] = newPost
                    loadData()
                }
            }
        }
    }

    fun movePost(id: Long, movedBy: Int): Boolean {
        val post = indexes[id] ?: return false
        val swappablePostIndex = post.first - movedBy
        return try {
            Collections.swap(mutablePostsList, post.first, swappablePostIndex)
            loadData()
            postsList.value == mutablePostsList.toList()
        } catch (ex: ArrayIndexOutOfBoundsException) {
            return false
        }
    }

    fun getPostById(id: Long): Post? = postRepository.getPostById(id)

    private fun loadData() {
        viewModelScope.launch(Dispatchers.Main) {
            postsList.value = mutablePostsList.toList()
        }
    }
}




