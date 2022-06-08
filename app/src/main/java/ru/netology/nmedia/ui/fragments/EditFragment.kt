package ru.netology.nmedia.ui.fragments

import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.ui.AppBarConfiguration
import ru.netology.nmedia.databinding.EditFragmentBinding
import androidx.navigation.ui.setupWithNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.checkIfNotEmpty
import ru.netology.nmedia.utils.getAppComponent
import ru.netology.nmedia.utils.makeToast
import ru.netology.nmedia.utils.setDebouncedListener
import javax.inject.Inject

class EditFragment : BaseFragment<EditFragmentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> EditFragmentBinding
        get() = EditFragmentBinding::inflate

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: PostViewModel by activityViewModels {
        viewModelFactory
    }

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
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            toolbar.setupWithNavController(this, appBarConfiguration)
        }
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        val id = requireArguments().get("post_id") as Long
        val currentText = requireArguments().get("post_text") as String
        val currentTitle = requireArguments().get("post_title") as String
        val date = viewModel.getDate(id)
        if (date != null) tvDateTime.text = date
        tvPostText.setText(currentText)
        tvPostTitle.setText(currentTitle)
        cancelButton.setOnClickListener {
            onBackPressed()
        }
        cardViewSendPost.setDebouncedListener {
            when {
                !tvPostText.text.toString().checkIfNotEmpty()
                        || !tvPostTitle.text.toString().checkIfNotEmpty() ->
                    makeToast(requireContext().getString(R.string.text_is_unfilled))
                else -> {
                    Linkify.addLinks(tvPostText, Linkify.WEB_URLS)
                    val url: String? =
                        if (tvPostText.urls.isNotEmpty()) tvPostText.urls.first().url else null
                    viewModel.editPost(
                        id,
                        tvPostText.text.toString().trim(),
                        tvPostTitle.text.toString().trim(),
                        url
                    )
                    clearKeyboard(tvPostText)
                    onBackPressed()
                }
            }
        }
    }
}