package ru.netology.nmedia.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.HttpException
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.ui.adapter.PostAdapter
import ru.netology.nmedia.ui.adapter.PostListener
import ru.netology.nmedia.ui.adapter.decorators.LinearVerticalSpacingDecoration
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodels.ViewModelFactory
import ru.netology.nmedia.utils.*
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

class MainFragment : BaseFragment<FragmentMainBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var lastUpdateTime: Long = 0L
    private val args: MainFragmentArgs by navArgs()
    private val viewModel: PostViewModel by activityViewModels {
        viewModelFactory
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainBinding
        get() = FragmentMainBinding::inflate


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
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            binding.toolbar.setupWithNavController(this, appBarConfiguration).also {
                (activity as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.app_name)
            }
        }
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
                lastUpdateTime = SystemClock.elapsedRealtime()
            }

            override fun onShared(id: Long) {
                viewModel.sharePost(id)
            }

            override fun onCommented(id: Long) {
                viewModel.commentPost(id)
            }

            override fun onLinkPressed(url: String) {
                openUrl(url)
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
                LinearVerticalSpacingDecoration(INNER_SPACING_RC_VIEW.dpTpPx())
            )
            cardViewAddPost.setDebouncedListener(500L) {
                mainNavController?.navigate(R.id.action_mainFragment_to_addFragment)
            }
            refreshPostLayout.setOnRefreshListener {
                binding.refreshPostLayout.isRefreshing = true
                viewModel.updateLiveData()
            }
        }
        viewModel.updateLiveData()
        viewModel.postsList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.refreshPostLayout.isRefreshing = false
                    binding.postProgress.setVisibility(false)
                    adapter.submitList(result.data.map { Post.parser(it) })
                }
                is NetworkResult.Loading -> {
                    if (SystemClock.elapsedRealtime() - lastUpdateTime < args.updateDebounce) {
                        return@observe
                    } else {
                        binding.postProgress.setVisibility(true)
                    }
                }
                is NetworkResult.Error -> {
                    binding.postProgress.setVisibility(false)
                    Timber.e("Error: ${result.error}")
                    when (val it = result.error) {
                        is SocketTimeoutException -> showToast("Timed out waiting for a response from the server")
                        is ConnectException -> if (SystemClock.elapsedRealtime() - lastUpdateTime > args.updateDebounce) {
                            showToast("Error: No Internet connection!")
                        }
                        is IOException -> showToast("Error: Problem with Internet connection!")
                        is HttpException -> showToast("Error (${it.code()}): ${it.message()}")
                        else -> showToast("Error: ${it.getErrorMessage()}")
                    }
                }
            }
        }
    }
    private companion object {
        private const val INNER_SPACING_RC_VIEW: Int = 5
    }
}