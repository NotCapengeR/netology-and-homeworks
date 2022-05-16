package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepositoryImpl

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val postRepository = PostRepositoryImpl()
    val postsList = MutableLiveData<List<Post>>()

    init {
        postRepository.addPost("Университет НЕТОЛОГИЯЯЯЯЯЯЯЯЯЯЯЯЯЯЯЯ", "KJNJK24H4HJK21142HJK")
        repeat(9) {
            postRepository.addPost(
                "Университет Нетология",
                "afshjafhsjkfaskhjfasjkhgfasjkhgfasfsfasfasafsfafadsfasafsfasfasfasfas" +
                        "fsalkjafsljkfasljkfalsjkafsljkfsafasfaslhjkafskjafsjkhhfajkasfjklfsas" +
                        "faslkjkfasljkfasljkfasljkfasj;faslkjaflsjkaflskjflajkslajkfsfkalsjfsa" +
                        "faslhjkfasjkfashjkfahsjkjkfasfasfasfasafsfasfasfasfasfasfas212312312123"
            )
        }
        notifyChanges()
    }

    // НЕ ДОДЕЛАНО! Руками не трогать
    fun addPost(title: String, text: String): Long {
        val result = postRepository.addPost(title, text)
        notifyChanges()
        return result
    }

    fun removePost(id: Long): Boolean {
        postRepository.removePost(id).also {
            if (it) notifyChanges()
            return it
        }
    }

    // Тоже не доделано
    fun editPost(id: Long, newText: String): Boolean {
        return postRepository.editPost(id, newText).also {
            if (it) notifyChanges()
        }
    }

    fun likePost(id: Long): Boolean {
        return postRepository.likePost(id).also {
            if (it) notifyChanges()
        }
    }

    fun sharePost(id: Long): Int = postRepository.sharePost(id).also {
        if (it > 0) notifyChanges()
    }

    fun commentPost(id: Long): Int = postRepository.commentPost(id).also {
        if (it > 0) notifyChanges()
    }

    fun movePost(id: Long, movedBy: Int): Long = postRepository.onPostMoved(id, movedBy).also {
        if (it > 0L) notifyChanges()
    }

    private fun notifyChanges() {
        postsList.value = postRepository.getPosts()
    }
}
