package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepositoryImpl

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val postRepository = PostRepositoryImpl()
    val liveData = MutableLiveData<List<Post>>()

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

    fun editPost(id: Long, newText: String): Boolean {
        postRepository.editPost(id, newText).also {
            if (it) notifyChanges()
            return it
        }
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
