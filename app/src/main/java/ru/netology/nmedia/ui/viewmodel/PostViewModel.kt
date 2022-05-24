package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepositoryImpl

class PostViewModel(application: Application) : AndroidViewModel(application) {

    val postsList: MutableLiveData<List<Post>> by lazy {
        MutableLiveData<List<Post>>()
    }

    init {
        PostRepositoryImpl.addPost("Университет НЕТОЛОГИЯЯЯЯЯЯЯЯЯЯЯЯЯЯЯЯ", "KJNJK24H4HJK21142HJK")
        repeat(9) { index ->
            PostRepositoryImpl.addPost("Нетология", "Пост под номером ${index + 2}")
        }
        notifyChanges()
    }

    // НЕ ДОДЕЛАНО! Руками не трогать
    fun addPost(title: String, text: String): Long = PostRepositoryImpl.addPost(title, text).also {
        if (it > 0) {
            notifyChanges()
        }
    }

    fun removePost(id: Long): Boolean {
        return PostRepositoryImpl.removePost(id).also {
            if (it) {
                notifyChanges()
            }
        }
    }

    // Тоже не доделано
    fun editPost(id: Long, newText: String): Boolean {
        return PostRepositoryImpl.editPost(id, newText).also {
            if (it) {
                notifyChanges()
            }
        }
    }

    fun likePost(id: Long): Boolean {
        return PostRepositoryImpl.likePost(id).also {
            if (it) {
                notifyChanges()
            }
        }
    }

    fun sharePost(id: Long): Int {
        return PostRepositoryImpl.sharePost(id).also {
            if (it > 0) {
                notifyChanges()
            }
        }
    }

    fun commentPost(id: Long): Int {
        return PostRepositoryImpl.commentPost(id).also {
            if (it > 0) {
                notifyChanges()
            }
        }
    }

    private fun notifyChanges() {
        postsList.value = PostRepositoryImpl.getPosts()
    }

    fun movePost(id: Long, movedBy: Int): Boolean = false
//TODO    {
//        val post = postRepository.getPostById(id) ?: return false
//        val postIndex = mutablePostsList.indexOf(post)
//        val swappablePostIndex = postIndex - movedBy
//        return try {
//            Collections.swap(mutablePostsList, postIndex, swappablePostIndex)
//            notifyChanges()
//            postsList.value == mutablePostsList.toList()
//        } catch (ex: ArrayIndexOutOfBoundsException) {
//            return false
//        }
//    }

}



