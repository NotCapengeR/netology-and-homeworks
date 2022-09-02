package ru.netology.nmedia.service.work_manager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ru.netology.nmedia.ui.viewmodels.UnknownModelClassException
import ru.netology.nmedia.utils.getErrorMessage
import javax.inject.Inject
import javax.inject.Provider

class DaggerWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<ChildWorkerFactory>>
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        val foundEntry = workerFactories.entries.find {
                Class.forName(workerClassName).isAssignableFrom(it.key)
            }
        val factoryProvider = foundEntry?.value ?: throw UnknownModelClassException("Unknown worker class name: $workerClassName")

        try {
            return factoryProvider.get().create(appContext, workerParameters)
        } catch (t: Throwable) {
            throw WorkerCreateException(message = t.getErrorMessage(), cause = t)
        }
    }
}

class WorkerCreateException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)

interface ChildWorkerFactory {
    fun create(appContext: Context, workerParameters: WorkerParameters): ListenableWorker
}
