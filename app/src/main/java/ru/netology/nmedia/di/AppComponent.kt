package ru.netology.nmedia.di

import dagger.Component
import ru.netology.nmedia.di.modules.AppModule
import ru.netology.nmedia.di.modules.ViewModelModule
import ru.netology.nmedia.ui.fragments.AddFragment
import ru.netology.nmedia.ui.activity.MainActivity
import ru.netology.nmedia.ui.fragments.MainFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [ViewModelModule::class, AppModule::class])
interface AppComponent {

    fun inject(fragment: MainFragment)
    fun inject(fragment: AddFragment)
    fun inject(activity: MainActivity)

}










