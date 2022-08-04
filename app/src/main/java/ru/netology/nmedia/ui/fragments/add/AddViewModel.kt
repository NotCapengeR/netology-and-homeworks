package ru.netology.nmedia.ui.fragments.add

import android.app.Application
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.nmedia.R
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.dto.Photo
import ru.netology.nmedia.ui.base.BaseViewModel
import java.io.File
import javax.inject.Inject

class AddViewModel @Inject constructor(
    application: Application,
    private val repository: PostRepository,
) : BaseViewModel(application) {


    val photo: MutableLiveData<Photo> = MutableLiveData(Photo.NO_PHOTO)
    val isUpdating: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoaded: MutableLiveData<Boolean> = MutableLiveData(false)

    fun setPhoto(file: File?, uri: Uri?) {
        photo.value = Photo(file, uri)
    }

    fun addPost(
        title: String,
        text: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            isUpdating.postValue(true)
            if (photo.value != null && photo.value != Photo.NO_PHOTO) {
                repository.addPostWithAttachment(title, text, photo.value).also {
                    withContext(Dispatchers.Main) {
                        isUpdating.value = false
                        if (it > 0L) {
                            photo.value = Photo.NO_PHOTO
                            isLoaded.value = true
                            isLoaded.value = false
                        } else {
                            showToast(R.string.error_problem_with_internet_connection)
                        }
                    }
                }
            } else {
                repository.addPost(title, text).also {
                    withContext(Dispatchers.Main) {
                        isUpdating.value = false
                        if (it > 0L) {
                            isLoaded.value = true
                            isLoaded.value = false
                        } else {
                            showToast(R.string.error_problem_with_internet_connection)
                        }
                    }
                }
            }
        }
    }
}