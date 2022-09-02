package ru.netology.nmedia

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import ru.netology.nmedia.di.AppComponent
import ru.netology.nmedia.di.DaggerAppComponent
import ru.netology.nmedia.di.modules.AppModule
import ru.netology.nmedia.utils.getAppComponent
import timber.log.Timber
import javax.inject.Inject

class App : Application() {

    @Inject lateinit var factory: WorkerFactory

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        getAppComponent().inject(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        WorkManager.initialize(this, Configuration.Builder().setWorkerFactory(factory).build())
    }
}