package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepositoryImpl

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val postRepository = PostRepositoryImpl()
    val post: MutableLiveData<Post> by lazy {
        MutableLiveData<Post>()
    }

    init {
        post.value = postRepository.getPost()
    }

    fun likePost(post: Post): Boolean = postRepository.likePost(post).also {
        notifyChanges()
    }


    fun sharePost(post: Post): Int = postRepository.sharePost(post).also {
        notifyChanges()
    }

    private fun notifyChanges() {
        post.value = postRepository.getPost()
    }
}
