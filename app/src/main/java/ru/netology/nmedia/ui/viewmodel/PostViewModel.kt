package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.util.*

class PostViewModel(application: Application) : AndroidViewModel(application) {

    // приватная переменная нужна для перемещения постов, ибо в мапе их перемещать нельзя)
    private val mutablePostsList: MutableList<Post> = mutableListOf()
    val postsList: MutableLiveData<List<Post>> by lazy {
        MutableLiveData<List<Post>>()
    }

    init {
        PostRepositoryImpl.addPost("Университет НЕТОЛОГИЯЯЯЯЯЯЯЯЯЯЯЯЯЯЯЯ", "KJNJK24H4HJK21142HJK")
        repeat(9) { index ->
            PostRepositoryImpl.addPost("Нетология", "Пост под номером ${index + 2}")
        }
        mutablePostsList.addAll(PostRepositoryImpl.getPosts())
        notifyChanges()
    }

    // НЕ ДОДЕЛАНО! Руками не трогать
    fun addPost(title: String, text: String): Long = PostRepositoryImpl.addPost(title, text).also {
        if (it > 0) {
            val post = PostRepositoryImpl.getPostById(it) ?: return -1
            mutablePostsList.add(post)
            notifyChanges()
        }
    }

    fun removePost(id: Long): Boolean {
        val post = PostRepositoryImpl.getPostById(id) ?: return false
        return PostRepositoryImpl.removePost(id).also {
            if (it) {
                mutablePostsList.remove(post)
                notifyChanges()
            }
        }
    }

    // Тоже не доделано
    fun editPost(id: Long, newText: String): Boolean {
        val post = PostRepositoryImpl.getPostById(id) ?: return false
        return PostRepositoryImpl.editPost(id, newText).also {
            if (it) {
                val newPost = PostRepositoryImpl.getPostById(id) ?: return false
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                notifyChanges()
            }
        }
    }

    fun likePost(id: Long): Boolean {
        val post = PostRepositoryImpl.getPostById(id) ?: return false
        return PostRepositoryImpl.likePost(id).also {
            if (it) {
                val newPost = PostRepositoryImpl.getPostById(id) ?: return false
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                notifyChanges()
            }
        }
    }

    fun sharePost(id: Long): Int {
        val post = PostRepositoryImpl.getPostById(id) ?: return -1
        return PostRepositoryImpl.sharePost(id).also {
            if (it > 0) {
                val newPost = PostRepositoryImpl.getPostById(id) ?: return -2
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                notifyChanges()
            }
        }
    }

    fun commentPost(id: Long): Int {
        val post = PostRepositoryImpl.getPostById(id) ?: return -1
        return PostRepositoryImpl.commentPost(id).also {
            if (it > 0) {
                val newPost = PostRepositoryImpl.getPostById(id) ?: return -2
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                notifyChanges()
            }
        }
    }

    fun movePost(id: Long, movedBy: Int): Boolean {
        val post = PostRepositoryImpl.getPostById(id) ?: return false
        val postIndex = mutablePostsList.indexOf(post)
        val swappablePostIndex = postIndex - movedBy
        return try {
            Collections.swap(mutablePostsList, postIndex, swappablePostIndex)
            notifyChanges()
            postsList.value == mutablePostsList.toList()
        } catch (ex: ArrayIndexOutOfBoundsException) {
            return false
        }
    }

    private fun notifyChanges() {
        postsList.value = mutablePostsList.toList()
    }
}


