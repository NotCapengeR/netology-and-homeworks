package ru.netology.nmedia.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.*
import dagger.multibindings.IntoMap
import ru.netology.nmedia.App
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.ui.activity.AddFragment
import ru.netology.nmedia.ui.activity.MainActivity
import ru.netology.nmedia.ui.activity.MainFragment
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
@Component(modules = [RepositoryModule::class, ViewModelModule::class, AppModule::class])
interface AppComponent {

    fun inject(fragment: MainFragment)
    fun inject(fragment: AddFragment)
    fun inject(activity: MainActivity)

}

@Module
interface AppModule {

    @Binds
    fun bindContext(application: App): Context

    @Binds
    @Singleton
    fun bindApp(application: App): Application
}

@Module
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindPostRepository(postRepository: PostRepositoryImpl): PostRepository
}


@Module
interface ViewModelModule {

    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(PostViewModel::class)
    fun bindPostViewModel(viewModel: PostViewModel): ViewModel
}

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)





