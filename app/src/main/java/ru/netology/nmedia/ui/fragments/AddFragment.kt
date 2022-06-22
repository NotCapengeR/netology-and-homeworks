package ru.netology.nmedia.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateFormat
import android.text.util.Linkify
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.AddFragmentBinding
import ru.netology.nmedia.dto.Post.Companion.POST_DATE_PATTERN
import ru.netology.nmedia.dto.Post.Companion.POST_TEXT
import ru.netology.nmedia.dto.Post.Companion.POST_TITLE
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.checkIfNotEmpty
import ru.netology.nmedia.utils.getAppComponent
import ru.netology.nmedia.utils.setDebouncedListener
import java.util.*
import javax.inject.Inject

class AddFragment : BaseFragment<AddFragmentBinding>(), View.OnClickListener {

    @Inject lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: PostViewModel by activityViewModels {
        viewModelFactory
    }
    @Inject lateinit var prefs: SharedPreferences
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> AddFragmentBinding
        get() = AddFragmentBinding::inflate

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getAppComponent().inject(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = with(binding) {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    prefs.edit {
                        putString(POST_TITLE, binding.tvPostTitle.text.toString())
                        putString(POST_TEXT, binding.tvPostText.text.toString())
                    }
                    onBackPressed()
                }
            })
        tvPostText.setText(prefs.getString(POST_TEXT, ""))
        tvPostTitle.setText(prefs.getString(POST_TITLE, ""))
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            toolbar.setupWithNavController(this, appBarConfiguration)
        }
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        tvDateTime.text = DateFormat.format(POST_DATE_PATTERN, Date().time)
        cardViewSendPost.setDebouncedListener(600L, this@AddFragment)
        cancelButton.setDebouncedListener(50L, this@AddFragment)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.cardViewSendPost -> {
                with(binding) {
                    if (tvPostTitle.text.toString().checkIfNotEmpty() &&
                        tvPostText.text.toString().checkIfNotEmpty()
                    ) {
                        Linkify.addLinks(tvPostText, Linkify.WEB_URLS)
                        val url: String? =
                            if (tvPostText.urls.isNotEmpty()) tvPostText.urls.first().url else null
                        lifecycleScope.launch {
                            viewModel.addPost(
                                tvPostTitle.text.toString().trim(),
                                tvPostText.text.toString().trim(),
                                url
                            )
                        }
                        onBackPressed()
                        prefs.edit {
                            putString(POST_TITLE, " ")
                            putString(POST_TEXT, " ")
                        }
                    } else {
                        showToast(R.string.text_is_unfilled)
                    }
                }
            }

            R.id.cancel_button -> onBackPressed()
            else -> {/* do nothing */
            }
        }
    }

    override fun onBackPressed() {
        prefs.edit {
            putString(POST_TITLE, binding.tvPostTitle.text.toString())
            putString(POST_TEXT, binding.tvPostText.text.toString())
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.empty, menu)
    }
}