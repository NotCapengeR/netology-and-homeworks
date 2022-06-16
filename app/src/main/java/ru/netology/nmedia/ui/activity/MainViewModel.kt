package ru.netology.nmedia.ui.activity

import android.app.Application
import ru.netology.nmedia.ui.base.BaseViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
    application: Application,
): BaseViewModel(application)
