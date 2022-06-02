package ru.netology.nmedia.di

import dagger.Component
import ru.netology.nmedia.ui.activity.AddFragment
import ru.netology.nmedia.ui.activity.MainActivity
import ru.netology.nmedia.ui.activity.MainFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [ViewModelModule::class, AppModule::class])
interface AppComponent {

    fun inject(fragment: MainFragment)
    fun inject(fragment: AddFragment)
    fun inject(activity: MainActivity)

}










