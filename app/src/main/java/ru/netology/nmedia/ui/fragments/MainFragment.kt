package ru.netology.nmedia.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.dto.Post.Companion.POST_ID
import ru.netology.nmedia.dto.Post.Companion.POST_TEXT
import ru.netology.nmedia.dto.Post.Companion.POST_TITLE
import ru.netology.nmedia.ui.adapter.PostAdapter
import ru.netology.nmedia.ui.adapter.PostListener
import ru.netology.nmedia.ui.adapter.decorators.LinearVerticalSpacingDecoration
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.utils.getAppComponent
import ru.netology.nmedia.utils.setDebouncedListener
import ru.netology.nmedia.utils.setVisibility
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


    override fun clearKeyboard(editText: EditText?): Unit = with(binding) {
        super.clearKeyboard(editText)
        cardViewEditMessage.setVisibility(false)
        editableMessageContainer.setVisibility(false)
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

            override suspend fun onRemoved(id: Long): Boolean {
                return withContext(lifecycleScope.coroutineContext + Dispatchers.Main) {
                    viewModel.removePost(id)
                }
            }

            override fun onEdit(
                id: Long,
                currentText: String,
                currentTitle: String
            ): Boolean = with(binding) {
                mainNavController?.navigate(
                    R.id.action_mainFragment_to_editFragment, bundleOf(
                        POST_ID to id,
                        POST_TEXT to currentText,
                        POST_TITLE to currentTitle
                    )
                )
                return mainNavController?.currentDestination?.id == R.id.editFragment
            }

            override suspend fun onLiked(id: Long): Boolean {
                return withContext(lifecycleScope.coroutineContext + Dispatchers.Main) {
                    viewModel.likePost(id)
                }
            }

            override suspend fun onShared(id: Long): Int {
                return withContext(lifecycleScope.coroutineContext + Dispatchers.Main) {
                    viewModel.sharePost(id)
                }
            }

            override suspend fun onCommented(id: Long): Int {
                return withContext(lifecycleScope.coroutineContext + Dispatchers.Main) {
                    viewModel.commentPost(id)
                }
            }

            override fun onPostMoved(id: Long, movedBy: Int): Boolean {
                return viewModel.movePost(id, movedBy)
            }

            override fun onLinkPressed(url: String) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

            override suspend fun onLinkRemoved(id: Long): Boolean {
                return withContext(lifecycleScope.coroutineContext + Dispatchers.Main) {
                    viewModel.removeLink(id)
                }
            }

            override fun onItemPressed(
                id: Long,
                currentText: String,
                currentTitle: String
            ) {
                mainNavController?.navigate(
                    R.id.action_mainFragment_to_detailsFragment,
                    bundleOf(
                        POST_ID to id,
                        POST_TEXT to currentText,
                        POST_TITLE to currentTitle
                    )
                )
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
            binding.progressEmpty.setVisibility(it.isEmpty())
            adapter.submitList(it)
        }
    }
}