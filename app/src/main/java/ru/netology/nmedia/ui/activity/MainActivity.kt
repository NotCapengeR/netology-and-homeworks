package ru.netology.nmedia.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.ui.base.BaseActivity
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.getAppComponent
import javax.inject.Inject


class MainActivity : BaseActivity<ActivityMainBinding>() {

    @Inject lateinit var factory: ViewModelFactory
    private val viewModel: MainViewModel by viewModels {
        factory
    }
    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getAppComponent().inject(this)
    }
}
