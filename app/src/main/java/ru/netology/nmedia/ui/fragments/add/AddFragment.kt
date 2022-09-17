package ru.netology.nmedia.ui.fragments.add

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.text.util.Linkify
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.AddFragmentBinding
import ru.netology.nmedia.repository.dto.Post.Companion.POST_DATE_PATTERN
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodels.ViewModelFactory
import ru.netology.nmedia.utils.*
import java.util.*
import javax.inject.Inject

class AddFragment : BaseFragment<AddFragmentBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var prefs: SharedPreferences
    private val args: AddFragmentArgs by navArgs()
    private val viewModel: AddViewModel by activityViewModels {
        viewModelFactory
    }
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
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> AddFragmentBinding
        get() = AddFragmentBinding::inflate

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() = with(binding) {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        viewModel.text.value.let { text ->
            if (!text.isNullOrBlank()) {
                tvPostText.setText(text)
                Linkify.addLinks(tvPostText, Linkify.WEB_URLS)
            }
        }
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            toolbar.setupWithNavController(this, appBarConfiguration)
        }
        Glide.with(requireContext())
            .load(R.drawable.ic_netology)
            .placeholder(R.drawable.ic_baseline_account_circle_24)
            .error(R.drawable.alert_circle)
            .centerCrop()
            .timeout(10_000)
            .into(ivPostAvatar)
        viewModel.clearErrorMsg()
        viewModel.errorMsg.observe(viewLifecycleOwner) { message ->
            if (message != null && !message.isBlankOrEmpty()) {
                showToast(message)
            }
        }
        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo.uri != null) {
                ivAttachment.setVisibility(true)
                Glide.with(requireContext())
                    .load(photo.uri)
                    .placeholder(R.drawable.push_nmedia)
                    .error(R.drawable.alert_circle)
                    .centerCrop()
                    .timeout(10_000)
                    .into(ivAttachment)
            } else {
                ivAttachment.setVisibility(false)
                Glide.with(requireContext()).clear(ivAttachment)
            }
        }
        tvPostText.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank()) {
                viewModel.setText(text)
                Linkify.addLinks(tvPostText, Linkify.WEB_URLS)
            }
        }
        tvDateTime.text = DateFormat.format(POST_DATE_PATTERN, Date().time)
        cardViewSendPost.setDebouncedListener(600L) {
            if (tvPostText.text.toString().checkIfNotEmpty()) {
                viewModel.addPost(
                    tvPostTitle.text.toString().trim(),
                    tvPostText.text.toString().trim(),
                )
            } else {
                showToast(R.string.text_is_unfilled)
            }
        }
        cancelButton.setDebouncedListener(50L) {
            onBackPressed()
        }
    }

    private fun initViewModel() {
        viewModel.isLoaded.observe(viewLifecycleOwner) { isLoaded ->
            if (isLoaded) {
                onBackPressed {
                    viewModel.clearText()
                }
            }
        }
        viewModel.isUpdating.observe(viewLifecycleOwner) {
            binding.postProgress.setVisibility(it)
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.image, menu)
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
}