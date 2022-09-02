package ru.netology.nmedia.di.modules

import android.content.Context
import androidx.work.*
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.service.work_manager.ChildWorkerFactory
import ru.netology.nmedia.service.work_manager.DaggerWorkerFactory
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Module(includes = [WorkManagerBinder::class])
class WorkManagerModule {

    @Provides
    @Singleton
    fun provideWorkManager(context: Context): WorkManager = WorkManager.getInstance(context)

}

@Module(includes = [AppModule::class])
interface WorkManagerBinder {

    @Binds
    fun bindDaggerWorkerFactory(factory: DaggerWorkerFactory): WorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(StubWorker::class)
    fun bindStubWorker(factory: StubWorker.Factory): ChildWorkerFactory

}


@MapKey
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerKey(val value: KClass<out ListenableWorker>)

class StubWorker (
    appContext: Context,
    params: WorkerParameters,
    private val repository: PostRepository
) : Worker(appContext, params) {
    override fun doWork(): Result {
        Timber.d("Work have done! ${repository.getAuthId()}")
        return Result.Success()
    }

    class Factory @Inject constructor(
        private val repository: PostRepository
    ) : ChildWorkerFactory {
        override fun create(appContext: Context, workerParameters: WorkerParameters): ListenableWorker {
            return StubWorker(
                appContext,
                workerParameters,
                repository
            )
        }
    }
}