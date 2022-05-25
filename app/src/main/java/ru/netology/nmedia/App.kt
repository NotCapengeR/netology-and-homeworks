package ru.netology.nmedia

import android.app.Application
import ru.netology.nmedia.di.AppComponent
import ru.netology.nmedia.di.DaggerAppComponent
import timber.log.Timber
import javax.inject.Inject

class App @Inject constructor(): Application() {

    val appComponent: AppComponent = DaggerAppComponent.create()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}