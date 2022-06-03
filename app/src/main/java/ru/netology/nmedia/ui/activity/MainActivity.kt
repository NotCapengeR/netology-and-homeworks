package ru.netology.nmedia.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.ui.activity.MainFragment.Companion.MAIN_FRAGMENT_TAG
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.getAppComponent
import javax.inject.Inject


class MainActivity : AppCompatActivity(), FragmentInteractor {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: PostViewModel by viewModels {
        viewModelFactory
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getAppComponent().inject(this)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.keyboard_backspace)
        supportActionBar?.title = getString(R.string.app_name)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance(), MAIN_FRAGMENT_TAG)
                .addToBackStack(MAIN_FRAGMENT_TAG)
                .commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.size < 2) {
            finish()
        } else {
            val tag =
                supportFragmentManager.fragments[supportFragmentManager.fragments.size - 2].tag
            viewModel.currentTag(tag)
            supportFragmentManager.popBackStack()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.delete -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStartFragment() {
        viewModel.currentTag(supportFragmentManager.fragments.last().tag)
    }

    override fun onStopFragment() {
        if (supportFragmentManager.fragments.size >= 2) {
            val tag =
                supportFragmentManager.fragments[supportFragmentManager.fragments.size - 2].tag
            viewModel.currentTag(tag)
        }
    }
}

interface FragmentInteractor {

    fun onStartFragment()

    fun onStopFragment()
}