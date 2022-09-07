package ru.netology.nmedia.ui.base

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import ru.netology.nmedia.App
import ru.netology.nmedia.utils.AndroidUtils

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    fun getString(@StringRes msgResId: Int): String {
        return this.getApplication<App>().getString(msgResId)
    }

    fun getString(@StringRes msgResId: Int, vararg formatArgs: Any): String {
        return this.getApplication<App>().getString(msgResId, formatArgs)
    }
}