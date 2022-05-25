package ru.netology.nmedia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.MapKey
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

class PostViewModel @Inject constructor(
    application: Application,
    private val postRepository: PostRepository
) : AndroidViewModel(application) {
   // private val postRepository = PostRepositoryImpl()
    private val mutablePostsList: MutableList<Post> = mutableListOf()
    val postsList: MutableLiveData<List<Post>> by lazy {
        MutableLiveData<List<Post>>()
    }

    init {
        postRepository.addPost("Университет НЕТОЛОГИЯЯЯЯЯЯЯЯЯЯЯЯЯЯЯЯ", "KJNJK24H4HJK21142HJK")
        repeat(9) { index ->
            postRepository.addPost("Нетология", "Пост под номером ${index + 2}")
        }
        mutablePostsList.addAll(postRepository.getPosts())
        notifyChanges()
    }

    // НЕ ДОДЕЛАНО! Руками не трогать
    fun addPost(title: String, text: String): Long = postRepository.addPost(title, text).also {
        if (it > 0) {
            val post = postRepository.getPostById(it) ?: return -1
            mutablePostsList.add(post)
            notifyChanges()
        }
    }

    fun removePost(id: Long): Boolean {
        val post = postRepository.getPostById(id) ?: return false
        return postRepository.removePost(id).also {
            if (it) {
                mutablePostsList.remove(post)
                notifyChanges()
            }
        }
    }

    // Тоже не доделано
    fun editPost(id: Long, newText: String): Boolean {
        val post = postRepository.getPostById(id) ?: return false
        return postRepository.editPost(id, newText).also {
            if (it) {
                val newPost = postRepository.getPostById(id) ?: return false
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                notifyChanges()
            }
        }
    }

    fun likePost(id: Long): Boolean {
        val post = postRepository.getPostById(id) ?: return false
        return postRepository.likePost(id).also {
            if (it) {
                val newPost = postRepository.getPostById(id) ?: return false
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                notifyChanges()
            }
        }
    }

    fun sharePost(id: Long): Int {
        val post = postRepository.getPostById(id) ?: return -1
        return postRepository.sharePost(id).also {
            if (it > 0) {
                val newPost = postRepository.getPostById(id) ?: return -2
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                notifyChanges()
            }
        }
    }

    fun commentPost(id: Long): Int {
        val post = postRepository.getPostById(id) ?: return -1
        return postRepository.commentPost(id).also {
            if (it > 0) {
                val newPost = postRepository.getPostById(id) ?: return -2
                val postIndex = mutablePostsList.indexOf(post)
                mutablePostsList[postIndex] = newPost
                notifyChanges()
            }
        }
    }

    fun movePost(id: Long, movedBy: Int): Boolean {
        val post = postRepository.getPostById(id) ?: return false
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

class ViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        var creator: Provider<out ViewModel>? = creators[modelClass]
        if (creator == null) {
            for ((key, value) in creators) {
                if (modelClass.isAssignableFrom(key)) {
                    creator = value
                    break
                }
            }
        }
        if (creator == null) {
            throw IllegalArgumentException("Unknown model class " + modelClass)
        }
        try {
            @Suppress("UNCHECKED_CAST")
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

