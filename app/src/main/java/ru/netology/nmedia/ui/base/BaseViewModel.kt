package ru.netology.nmedia.ui.base

import android.app.Application
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import ru.netology.nmedia.App

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    protected fun showToast(message: String?, isLong: Boolean = false) {
        if (message == null) return
        if (isLong) {
            Toast.makeText(this.getApplication(), message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this.getApplication(), message, Toast.LENGTH_SHORT).show()
        }
    }

    protected fun showToast(@StringRes msgResId: Int, isLong: Boolean = false) {
        showToast(this.getApplication<App>().getString(msgResId), isLong)
    }
}