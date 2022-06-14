package ru.netology.nmedia.ui.activity

import android.view.LayoutInflater
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.ui.base.BaseActivity


class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate
}
