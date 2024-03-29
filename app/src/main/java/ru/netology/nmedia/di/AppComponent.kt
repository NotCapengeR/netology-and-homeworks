package ru.netology.nmedia.di

import dagger.Component
import ru.netology.nmedia.App
import ru.netology.nmedia.di.modules.*
import ru.netology.nmedia.service.FCMService
import ru.netology.nmedia.ui.activity.MainActivity
import ru.netology.nmedia.ui.fragments.image.ImageDetailsFragment
import ru.netology.nmedia.ui.fragments.add.AddFragment
import ru.netology.nmedia.ui.fragments.details.DetailsFragment
import ru.netology.nmedia.ui.fragments.edit.EditFragment
import ru.netology.nmedia.ui.fragments.MainFragment
import ru.netology.nmedia.ui.fragments.login.LoginFragment
import javax.inject.Singleton

@Singleton
@Component(
    modules = [ViewModelModule::class, MemoryModule::class, ApiModule::class, UtilsModule::class, WorkManagerModule::class]
)
interface AppComponent {

    @Component.Builder
    interface Builder {

        fun appModule(appModule: AppModule): Builder

        fun build(): AppComponent
    }

    fun inject(application: App)
    fun inject(activity: MainActivity)
    fun inject(fragment: MainFragment)
    fun inject(fragment: AddFragment)
    fun inject(fragment: EditFragment)
    fun inject(fragment: ImageDetailsFragment)
    fun inject(fragment: DetailsFragment)
    fun inject(fragment: LoginFragment)
    fun inject(service: FCMService)
}










