package ru.netology.nmedia.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.di.modules.FirebaseModule.Companion.FIREBASE_TAG
import ru.netology.nmedia.ui.base.BaseActivity
import ru.netology.nmedia.ui.viewmodels.ViewModelFactory
import ru.netology.nmedia.utils.getAppComponent
import timber.log.Timber
import javax.inject.Inject


class MainActivity : BaseActivity<ActivityMainBinding>() {

    @Inject lateinit var factory: ViewModelFactory
    @Inject lateinit var messaging: FirebaseMessaging
    @Inject lateinit var googleApi: GoogleApiAvailability
    private val viewModel: MainViewModel by viewModels {
        factory
    }
    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getAppComponent().inject(this)
        messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.tag(FIREBASE_TAG).d("Some stuff happened: ${task.exception}")
                return@addOnCompleteListener
            }

            Timber.tag(FIREBASE_TAG).d(task.result)
        }
        checkGoogleApiAvailability()
    }

    private fun checkGoogleApiAvailability() {
        with(googleApi) {
            val code = isGooglePlayServicesAvailable(this@MainActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@MainActivity, code, 9000)?.show()
                return
            }
            showToast(R.string.google_play_unavailable, true)
        }
    }
}
