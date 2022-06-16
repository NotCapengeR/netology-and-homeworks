package ru.netology.nmedia.di

import dagger.Component
import ru.netology.nmedia.App
import ru.netology.nmedia.di.modules.ApiModule
import ru.netology.nmedia.di.modules.AppModule
import ru.netology.nmedia.di.modules.MemoryModule
import ru.netology.nmedia.di.modules.ViewModelModule
import ru.netology.nmedia.ui.activity.MainActivity
import ru.netology.nmedia.ui.fragments.AddFragment
import ru.netology.nmedia.ui.fragments.DetailsFragment
import ru.netology.nmedia.ui.fragments.EditFragment
import ru.netology.nmedia.ui.fragments.MainFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [ViewModelModule::class, MemoryModule::class, ApiModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {

        fun appModule(appModule: AppModule): Builder

        fun build(): AppComponent
    }

    fun inject(application: App)
    fun inject(fragment: MainFragment)
    fun inject(fragment: AddFragment)
    fun inject(fragment: EditFragment)
    fun inject(activity: MainActivity)
    fun inject(fragment: DetailsFragment)

}










