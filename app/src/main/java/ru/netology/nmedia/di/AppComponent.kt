package ru.netology.nmedia.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import ru.netology.nmedia.App
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.ui.activity.MainActivity
import ru.netology.nmedia.ui.activity.MainFragment
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.ui.viewmodel.ViewModelKey
import javax.inject.Singleton

@Singleton
@Component(modules = [RepositoryModule::class, ViewModelModule::class, AppModule::class])
interface AppComponent {

    fun inject(fragment: MainFragment)
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





