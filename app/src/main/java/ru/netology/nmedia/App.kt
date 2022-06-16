package ru.netology.nmedia

import android.app.Application
import ru.netology.nmedia.di.AppComponent
import ru.netology.nmedia.di.DaggerAppComponent
import ru.netology.nmedia.di.modules.AppModule
import ru.netology.nmedia.utils.getAppComponent
import timber.log.Timber

class App : Application() {

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
    }
}