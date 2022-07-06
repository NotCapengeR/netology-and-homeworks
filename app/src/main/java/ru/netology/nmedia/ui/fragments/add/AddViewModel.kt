package ru.netology.nmedia.ui.fragments.add

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.ui.base.BaseViewModel
import javax.inject.Inject

class AddViewModel @Inject constructor(
    application: Application,
    private val repository: PostRepository,
) : BaseViewModel(application) {

    val isUpdating: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoaded: MutableLiveData<Boolean> = MutableLiveData(false)

    fun addPost(
        title: String,
        text: String,
        url: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            isUpdating.postValue(true)
            repository.addPost(title, text).also {
                withContext(Dispatchers.Main) {
                    isUpdating.value = false
                    if (it > 0L) {
                        isLoaded.value = true
                        isLoaded.value = false
                    } else {
                        showToast(
                            "Something went wrong." +
                                    " Check your Internet connection and try again later"
                        )
                    }
                }
//                if (it > 0L && url != null) {
//                    addVideo(url, it)
//                }
            }
        }
    }
}