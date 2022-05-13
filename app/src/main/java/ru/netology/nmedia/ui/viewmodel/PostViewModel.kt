package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepositoryImpl
import timber.log.Timber

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val postRepository = PostRepositoryImpl()
    val liveData = MutableLiveData<List<Post>>()

    init {
        postRepository.addPost("Университет НЕТОЛОГИЯЯЯЯЯЯЯЯЯЯЯЯЯЯЯЯ", "KJNJK24H4HJK21142HJK")
        repeat(7) {
            postRepository.addPost(
                "Университет Нетология", "afshjafhsjkfaskhjfasjkhgfasjkhg" +
                        "fsalkjafsljkfasljkfalsjkafsljk" +
                        "faslkjkfasljkfasljkfasljkfasj;" +
                        "faslhjkfasjkfashjkfahsjkjkfas"
            )
        }
        notifyChanges()
    }

    fun addPost(title: String, text: String): Long {
        val result = postRepository.addPost(title, text)
        notifyChanges()
        return result
    }

    // Эта функция у меня всегда вызывается два раза, если вы знаете причину — сообщите пж
    fun removePost(id: Long): Boolean {
        postRepository.removePost(id).also {
            if (it) notifyChanges()
            Timber.d("Fun actually returns $it")
            return postRepository.getPostById(id) == null
        }
    }

    fun editPost(id: Long, newText: String): Boolean {
        val result = postRepository.editPost(id, newText)
        if (result) notifyChanges()
        return result
    }

    fun likePost(id: Long): Boolean {
        val result = postRepository.likePost(id)
        notifyChanges()
        return result
    }

    fun sharePost(id: Long): Int {
        val result = postRepository.sharePost(id)
        notifyChanges()
        return result
    }

    private fun notifyChanges() {
        liveData.value = postRepository.getPosts()
    }
}
