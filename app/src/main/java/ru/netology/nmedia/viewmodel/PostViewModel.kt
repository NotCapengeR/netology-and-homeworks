package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post

class PostViewModel(application: Application) : AndroidViewModel(application) {
    val post: MutableLiveData<Post> by lazy {
        MutableLiveData<Post>()
    }

    init {
        post.value = Post(1, "Университет Нетология", "—", 0, 0)
    }

    fun likePost(post: Post): Boolean {
        if (!post.isLiked) {
            post.likesCount++
        } else {
            post.likesCount--
        }
        post.isLiked = !post.isLiked
        notifyChanges(post)
        return post.isLiked
    }

    fun sharePost(post: Post): Int {
        post.shareCount++
        notifyChanges(post)
        return post.shareCount
    }

    private fun notifyChanges(post: Post) {
        this.post.value = post
    }
}