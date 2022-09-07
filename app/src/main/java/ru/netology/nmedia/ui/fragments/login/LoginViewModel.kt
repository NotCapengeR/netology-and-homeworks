package ru.netology.nmedia.ui.fragments.login

import android.app.Application
import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.netology.nmedia.R
import ru.netology.nmedia.network.exceptions.FailedHttpRequestException
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.network.results.NetworkResult.Companion.RESPONSE_CODE_NOT_FOUND
import ru.netology.nmedia.repository.auth.AuthRepository
import ru.netology.nmedia.repository.dto.Photo
import ru.netology.nmedia.ui.base.BaseViewModel
import ru.netology.nmedia.utils.AndroidUtils.showToast
import ru.netology.nmedia.utils.SingleLiveEvent
import ru.netology.nmedia.utils.getErrorMessage
import ru.netology.nmedia.utils.isBlankOrEmpty
import ru.netology.nmedia.utils.isNotBlankOrEmpty
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    application: Application
) : BaseViewModel(application) {

    val isSuccess: MutableLiveData<Boolean> = MutableLiveData(false)
    val isBtnEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    val isProgress: MutableLiveData<Boolean> = MutableLiveData(false)
    val loginData: MutableLiveData<LoginData> = MutableLiveData(LoginData.EMPTY_LOGIN)
    val errorMsg: SingleLiveEvent<String> = SingleLiveEvent()

    fun clearErrorMsg() {
        errorMsg.call()
    }

    fun setPhoto(file: File?, uri: Uri?) {
        loginData.value = loginData.value?.copy(avatar = Photo(file, uri))
    }

    fun clearState() {
        isSuccess.value = false
    }

    fun login(login: String, password: String) {
        viewModelScope.launch {
            if (login.isBlankOrEmpty() || password.isBlankOrEmpty()) {
                errorMsg.value = getString(R.string.text_is_unfilled)
                return@launch
            }
            isProgress.value = true
            val result = repository.login(login, password)
            if (result is NetworkResult.Success) {
                isProgress.value = false
                isSuccess.value = true
            } else {
                handleError(result.error)
                isProgress.value = false
            }
        }
    }

    fun register(login: String, password: String, name: String) {
        loginData.value?.avatar?.let { avatar ->
            return registerWithAvatar(login, password, name, avatar)
        }
        viewModelScope.launch {
            if (login.isBlankOrEmpty() || password.isBlankOrEmpty() || name.isBlankOrEmpty()) {
                errorMsg.value = getString(R.string.text_is_unfilled)
                return@launch
            }
            isProgress.value = true
            val result = repository.register(login, password, name)
            if (result is NetworkResult.Success) {
                isProgress.value = false
                isSuccess.value = true
            } else {
                handleError(result.error)
                isProgress.value = false
            }
        }
    }

    private fun registerWithAvatar(login: String, password: String, name: String, avatar: Photo?) {
        viewModelScope.launch {
            if (login.isBlankOrEmpty() || password.isBlankOrEmpty() || name.isBlankOrEmpty()) {
                errorMsg.value = getString(R.string.text_is_unfilled)
                return@launch
            }
            isProgress.value = true
            val result = repository.registerWithAvatar(login, password, name, avatar)
            if (result is NetworkResult.Success) {
                isProgress.value = false
                isSuccess.value = true
            } else {
                handleError(result.error)
                isProgress.value = false
            }
        }
    }

    private fun handleError(error: Throwable?) {
        when (error) {
            null -> errorMsg.value = getString(R.string.error_unknown)
            is SocketTimeoutException -> errorMsg.value = getString(R.string.error_timed_out_from_response)
            is ConnectException -> {
                errorMsg.value = getString(R.string.error_no_internet_connection)
            }
            is IOException -> errorMsg.value = getString(R.string.error_problem_with_internet_connection)
            is FailedHttpRequestException -> {
                if (error.code() == RESPONSE_CODE_NOT_FOUND)
                    errorMsg.value = getString(R.string.error_user_not_found)
                else
                    errorMsg.value = "Error: ${error.code()}: ${error.message()}"

            }
            is HttpException -> errorMsg.value = "Error: ${error.code()}: ${error.message()}"

            else -> errorMsg.value = error.getErrorMessage()
        }
    }

    fun clearData() {
        loginData.value = LoginData.EMPTY_LOGIN
    }

    fun setLogin(login: String, flags: LoginFragment.LoginFlags) {
        loginData.value = loginData.value?.copy(login = login)
        checkEnable(flags)
    }

    fun setPassword(password: String, flags: LoginFragment.LoginFlags) {
        loginData.value = loginData.value?.copy(password = password)
        checkEnable(flags)
    }

    fun setConfirmPassword(password: String, flags: LoginFragment.LoginFlags) {
        loginData.value = loginData.value?.copy(confirmPassword = password)
        checkEnable(flags)
    }

    fun setName(name: String?, flags: LoginFragment.LoginFlags) {
        loginData.value = loginData.value?.copy(name = name)
        checkEnable(flags)
    }

    private fun checkEnable(flags: LoginFragment.LoginFlags) {
        loginData.value?.let { data ->
            when (flags) {
                LoginFragment.LoginFlags.LOGIN -> {
                    data.login.isNotBlankOrEmpty() && data.password.isNotBlankOrEmpty().also { enabled ->
                        isBtnEnabled.value = enabled
                    }
                }
                LoginFragment.LoginFlags.SIGNUP -> {
                    data.login.isNotBlankOrEmpty() && data.password.isNotBlankOrEmpty() && data.name?.isNotBlankOrEmpty() == true &&
                            data.confirmPassword?.isNotBlankOrEmpty() == true && (data.password == data.confirmPassword).also { enabled ->
                        isBtnEnabled.value = enabled
                    }
                }
            }
        }
    }

}

@Parcelize
data class LoginData(
    val login: String,
    val password: String,
    val name: String?,
    val confirmPassword: String?,
    val avatar: Photo?
) : Parcelable {
    companion object {
        val EMPTY_LOGIN = LoginData(
            login = "",
            password = "",
            name = null,
            confirmPassword = null,
            avatar = null
        )
    }
}