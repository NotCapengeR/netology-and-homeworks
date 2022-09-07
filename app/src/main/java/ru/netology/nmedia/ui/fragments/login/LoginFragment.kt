package ru.netology.nmedia.ui.fragments.login

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toFile
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import kotlinx.android.parcel.Parcelize
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.LoginFragmentBinding
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodels.ViewModelFactory
import ru.netology.nmedia.utils.getAppComponent
import ru.netology.nmedia.utils.isBlankOrEmpty
import ru.netology.nmedia.utils.setDebouncedListener
import ru.netology.nmedia.utils.setVisibility
import javax.inject.Inject

class LoginFragment : BaseFragment<LoginFragmentBinding>() {

    @Inject
    lateinit var factory: ViewModelFactory

    @Inject
    lateinit var prefs: SharedPreferences
    private val args: LoginFragmentArgs by navArgs()
    private val viewModel: LoginViewModel by activityViewModels {
        factory
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> LoginFragmentBinding
        get() = LoginFragmentBinding::inflate
    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                ImagePicker.RESULT_ERROR -> {
                    showSnackbar(ImagePicker.getError(result.data), true)
                }
                Activity.RESULT_OK -> {
                    val uri: Uri? = result.data?.data
                    viewModel.setPhoto(uri?.toFile(), uri)
                }
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = with(binding) {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            toolbar.setupWithNavController(this, appBarConfiguration)
        }
        viewModel.clearState()
        viewModel.clearErrorMsg()
        btnAuth.text = args.loginFlag.name
        cardName.setVisibility(args.loginFlag == LoginFlags.SIGNUP)
        cardConfirmPass.setVisibility(args.loginFlag == LoginFlags.SIGNUP)
        viewModel.loginData.value?.let { login ->
            etLogin.setText(login.login)
            etName.setText(login.name)
            etConfirmPassword.setText(login.confirmPassword)
            etPassword.setText(login.password)
        }
        etLogin.doOnTextChanged { text, _, _, _ ->
            viewModel.setLogin(text.toString(), args.loginFlag)
        }
        etPassword.doOnTextChanged { text, _, _, _ ->
            viewModel.setPassword(text.toString(), args.loginFlag)
        }
        if (args.loginFlag == LoginFlags.SIGNUP) {
            etName.doOnTextChanged { text, _, _, _ ->
                viewModel.setName(text.toString(), args.loginFlag)
            }
            etConfirmPassword.doOnTextChanged { text, _, _, _ ->
                viewModel.setConfirmPassword(text.toString(), args.loginFlag)
            }
        }
        viewModel.errorMsg.observe(viewLifecycleOwner) { message ->
            if (message != null && !message.isBlankOrEmpty()) {
                showToast(message)
            }
        }
        viewModel.loginData.observe(viewLifecycleOwner) { login ->
            login.avatar?.uri?.let { avatarUri ->
                ivAvatar.setVisibility(true)
                Glide.with(requireContext())
                    .load(avatarUri)
                    .placeholder(R.drawable.push_nmedia)
                    .error(R.drawable.alert_circle)
                    .centerCrop()
                    .timeout(10_000)
                    .into(ivAvatar)
            } ?: run {
                ivAvatar.setVisibility(false)
                Glide.with(requireContext()).clear(ivAvatar)
            }
        }
        viewModel.isSuccess.observe(viewLifecycleOwner) {
            if (it) {
                onBackPressed {
                    viewModel.clearData()
                    prefs.edit {
                        putBoolean(LOGIN_REFRESH_KEY, true)
                    }
                }
            }
        }
        viewModel.isProgress.observe(viewLifecycleOwner) {
            postProgress.setVisibility(it)
        }
        viewModel.isBtnEnabled.observe(viewLifecycleOwner) {
            btnAuth.setVisibility(it)
        }
        btnAuth.setDebouncedListener(100L) {
            when (args.loginFlag) {
                LoginFlags.LOGIN -> viewModel.login(
                    etLogin.text.toString(),
                    etPassword.text.toString()
                )
                LoginFlags.SIGNUP -> viewModel.register(
                    etLogin.text.toString(),
                    etPassword.text.toString(),
                    etName.text.toString()
                )
            }
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.image, menu)
        if (args.loginFlag != LoginFlags.SIGNUP) {
            menu.setGroupVisible(R.id.image_group, false)
        }
    }


    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.image -> {
                ImagePicker.with(this)
                    .crop()
                    .compress(2048)
                    .provider(ImageProvider.GALLERY)
                    .galleryMimeTypes(arrayOf("image/png", "image/jpeg"))
                    .createIntent(pickPhotoLauncher::launch)
                true
            }
            R.id.photo -> {
                ImagePicker.with(this)
                    .crop()
                    .compress(2048)
                    .provider(ImageProvider.CAMERA)
                    .createIntent(pickPhotoLauncher::launch)
                true
            }
            else -> super.onMenuItemSelected(item)
        }
    }


    @Parcelize
    enum class LoginFlags : Parcelable {
        LOGIN, SIGNUP
    }

    companion object {
        const val LOGIN_REFRESH_KEY: String = "login_refresh_key"
    }
}