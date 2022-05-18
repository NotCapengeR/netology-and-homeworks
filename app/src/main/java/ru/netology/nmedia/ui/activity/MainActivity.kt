package ru.netology.nmedia.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }
        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_main, MainFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }
}