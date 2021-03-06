package ru.netology.nmedia.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.netology.nmedia.utils.getErrorMessage
import java.lang.ClassCastException
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>,
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
            throw UnknownModelClassException("Unknown model class: $modelClass")
        }
        try {
            @Suppress("UNCHECKED_CAST")
            return creator.get() as T
        } catch (t: Throwable) {
            throw ViewModelCreateException(message = t.getErrorMessage(), cause = t)
        }
    }
}

class UnknownModelClassException(message: String? = null) : IllegalArgumentException(message)

class ViewModelCreateException(message: String? = null, cause: Throwable? = null)
    : RuntimeException(message, cause)