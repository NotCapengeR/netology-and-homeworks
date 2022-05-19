package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepositoryImpl

class PostViewModel(application: Application) : AndroidViewModel(application) {

    val post: MutableLiveData<Post> by lazy {
         PostRepositoryImpl.getPostLiveData() as MutableLiveData
    }

    fun likePost(post: Post): Boolean = PostRepositoryImpl.likePost(post).also {
        notifyChanges()
    }


    fun sharePost(post: Post): Int = PostRepositoryImpl.sharePost(post).also {
        notifyChanges()
    }

    private fun notifyChanges() {
        post.value = PostRepositoryImpl.getPost()
    }

}


