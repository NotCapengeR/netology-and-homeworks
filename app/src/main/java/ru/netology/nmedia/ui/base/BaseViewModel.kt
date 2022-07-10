package ru.netology.nmedia.ui.base

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import ru.netology.nmedia.App
import ru.netology.nmedia.utils.AndroidUtils

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    protected fun showToast(message: String?, isLong: Boolean = false) {
        AndroidUtils.showToast(this.getApplication(), message, isLong)
    }

    protected fun showToast(@StringRes msgResId: Int, isLong: Boolean = false) {
        showToast(this.getApplication<App>().getString(msgResId), isLong)
    }
}