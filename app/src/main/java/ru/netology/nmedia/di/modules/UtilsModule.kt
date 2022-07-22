package ru.netology.nmedia.di.modules

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


@Module
class UtilsModule {

    @Provides
    fun provideScope(): CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
}