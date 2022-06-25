package ru.netology.nmedia.ui.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.database.dto.Post
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.network.results.NetworkResult
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

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainBinding
        get() = FragmentMainBinding::inflate

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val args: MainFragmentArgs by navArgs()
    private val viewModel: PostViewModel by activityViewModels {
        viewModelFactory
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
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

            override fun onRemoved(id: Long) {
                viewModel.removePost(id)
            }

            override fun onEdit(
                id: Long,
                currentText: String,
                currentTitle: String
            ): Boolean = with(binding) {
                mainNavController?.navigate(
                    MainFragmentDirections
                        .actionMainFragmentToEditFragment(currentText, currentTitle, id)
                )
                return mainNavController?.currentDestination?.id == R.id.editFragment
            }

            override fun onLiked(id: Long) {
                viewModel.likePost(id)
            }

            override fun onShared(id: Long) {
                viewModel.sharePost(id)
            }

            override fun onCommented(id: Long) {
                viewModel.commentPost(id)
            }

            override fun onLinkPressed(url: String) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

            override fun onLinkRemoved(id: Long) {
                viewModel.removeLink(id)
            }

            override fun onItemPressed(
                id: Long,
                currentText: String,
                currentTitle: String
            ) {
                mainNavController?.navigate(
                    MainFragmentDirections.actionMainFragmentToDetailsFragment(
                        currentText,
                        currentTitle,
                        id
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
                mainNavController?.navigate(R.id.action_mainFragment_to_addFragment)
            }
            refreshPostLayout.setOnRefreshListener {
                refreshPostLayout.isRefreshing = true
                viewModel.updateLiveData()
                lifecycleScope.launch {
                    delay(1000L)
                    refreshPostLayout.isRefreshing = false
                }
            }
        }
        viewModel.updateLiveData()
        viewModel.postsList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.postProgress.setVisibility(false)
                    adapter.submitList(result.data
                        .map { Post.parser(it) }
                        .reversed()
                    )
                }
                is NetworkResult.Loading -> {
                    binding.postProgress.setVisibility(true)
                }
                is NetworkResult.Error -> {
                    binding.postProgress.setVisibility(false)
                    showToast(result.message)
                }
            }
        }
    }
}