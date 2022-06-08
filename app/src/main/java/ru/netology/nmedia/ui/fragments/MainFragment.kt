package ru.netology.nmedia.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.ui.adapter.PostAdapter
import ru.netology.nmedia.ui.adapter.PostListener
import ru.netology.nmedia.ui.adapter.decorators.LinearVerticalSpacingDecoration
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.*
import timber.log.Timber
import javax.inject.Inject

class MainFragment : BaseFragment<FragmentMainBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: PostViewModel by activityViewModels {
        viewModelFactory
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainBinding
        get() = FragmentMainBinding::inflate


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


    override fun clearKeyboard(editText: EditText?): Boolean = with(binding) {
        super.clearKeyboard(editText)
        cardViewEditMessage.setVisibility(false)
        editableMessageContainer.setVisibility(false)
        editText?.text?.clear()
        return !editText?.text.toString().checkIfNotEmpty()
    }


    private fun initView() {
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            binding.toolbar.setupWithNavController(this, appBarConfiguration)
        }
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        val adapter = PostAdapter(object : PostListener {
            override fun onAdded(title: String, text: String): Long {
                return viewModel.addPost(title, text)
            }

            override fun onRemoved(id: Long): Boolean {
                Timber.d("Removed post with id $id")
                return viewModel.removePost(id)
            }

            override fun onEdit(
                id: Long,
                currentText: String,
                currentTitle: String
            ): Boolean = with(binding) {
                mainNavController?.navigate(R.id.action_mainFragment_to_editFragment, bundleOf(
                    "post_id" to id,
                    "post_text" to currentText,
                    "post_title" to currentTitle
                ))
                return true
            }

            override fun onLiked(id: Long): Boolean {
                return viewModel.likePost(id)
            }

            override fun onShared(id: Long): Int {
                return viewModel.sharePost(id)
            }

            override fun onCommented(id: Long): Int {
                return viewModel.commentPost(id)
            }

            override fun onPostMoved(id: Long, movedBy: Int): Boolean {
                return viewModel.movePost(id, movedBy)
            }

            override fun onLinkPressed(url: String) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

            override fun onLinkRemoved(id: Long): Boolean {
                return viewModel.removeLink(id)
            }
        })
        adapter.setHasStableIds(true)
        with(binding) {
            rcViewPost.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            rcViewPost.adapter = adapter
            rcViewPost.addItemDecoration(
                LinearVerticalSpacingDecoration(
                    AndroidUtils.dpToPx(activity as AppCompatActivity, 5)
                )
            )
            cardViewAddPost.setDebouncedListener(500L) {
                clearKeyboard(etPostEdit)
                mainNavController?.navigate(R.id.action_mainFragment_to_addFragment)
            }
            ivEditCancel.setDebouncedListener(50L) {
                clearKeyboard(etPostEdit)
            }
        }
        viewModel.postsList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}