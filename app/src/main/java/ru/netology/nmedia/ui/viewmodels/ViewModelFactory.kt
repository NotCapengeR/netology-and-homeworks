package ru.netology.nmedia.ui.viewmodels

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.navGraphViewModels
import ru.netology.nmedia.utils.getErrorMessage
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

class ArgFactory <VM : ViewModel> (
    private val create: () -> VM,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return create.invoke() as T
    }
}

inline fun <reified VM : ViewModel> Fragment.withArgsViewModel(
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline create: () -> VM,
) = viewModels<VM>(ownerProducer = ownerProducer, extrasProducer = extrasProducer) {
    ArgFactory(create)
}

inline fun <reified VM : ViewModel> Fragment.withArgsActivityViewModel(
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline create: () -> VM,
) = activityViewModels<VM>(extrasProducer = extrasProducer) {
    ArgFactory(create)
}

inline fun <reified VM : ViewModel> Fragment.withArgsNavGraphViewModel(
    @IdRes navGraphId: Int,
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline create: () -> VM,
) = navGraphViewModels<VM>(navGraphId = navGraphId, extrasProducer = extrasProducer) {
    ArgFactory(create)
}

inline fun <reified VM : ViewModel> Fragment.withArgsNavGraphViewModel(
    navGraphRoute: String,
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline create: () -> VM,
) = navGraphViewModels<VM>(navGraphRoute = navGraphRoute, extrasProducer = extrasProducer) {
    ArgFactory(create)
}

inline fun <reified VM : ViewModel> ComponentActivity.withArgsViewModel(
    noinline extrasProducer: (() -> CreationExtras)? = null,
    noinline create: () -> VM,
) = viewModels<VM>(extrasProducer = extrasProducer) {
    ArgFactory(create)
}

class UnknownModelClassException(message: String? = null) : IllegalArgumentException(message)

class ViewModelCreateException(message: String? = null, cause: Throwable? = null)
    : RuntimeException(message, cause)