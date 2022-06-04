package ru.netology.nmedia.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.utils.getAppComponent


class MainActivity : AppCompatActivity(), FragmentInteractor {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getAppComponent().inject(this)
        setContentView(R.layout.activity_main)
    }

    override fun onStartFragment() {}

    override fun onStopFragment() {}
}

interface FragmentInteractor {

    fun onStartFragment()

    fun onStopFragment()
}