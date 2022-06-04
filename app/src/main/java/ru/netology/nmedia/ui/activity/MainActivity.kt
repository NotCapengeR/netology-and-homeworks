package ru.netology.nmedia.ui.activity

import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.utils.FragmentObserver


class MainActivity : AppCompatActivity(R.layout.activity_main), FragmentObserver {

    override fun onStartFragment() {}

    override fun onStopFragment() {}
}
