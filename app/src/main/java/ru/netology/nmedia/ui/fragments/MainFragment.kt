package ru.netology.nmedia.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.network.exceptions.FailedHttpRequestException
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.ui.adapter.PostAdapter
import ru.netology.nmedia.ui.adapter.PostListener
import ru.netology.nmedia.ui.adapter.decorators.LinearVerticalSpacingDecoration
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.fragments.login.LoginFragment
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
            binding.toolbar.setupWithNavController(this, appBarConfiguration)
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
                if (viewModel.getAuthId() == 0L) {
                    activity?.let {
                        AlertDialog.Builder(it).setTitle(R.string.note)
                            .setMessage(R.string.auth_please_signin)
                            .setPositiveButton(R.string.log_in) { _, _ ->
                                mainNavController?.navigate(
                                    MainFragmentDirections.actionMainFragmentToLoginFragment(
                                        LoginFragment.LoginFlags.LOGIN
                                    )
                                )
                            }
                            .setNegativeButton(R.string.sign_up) { _, _ ->
                                mainNavController?.navigate(
                                    MainFragmentDirections.actionMainFragmentToLoginFragment(
                                        LoginFragment.LoginFlags.SIGNUP
                                    )
                                )
                            }
                            .setNeutralButton(
                                com.github.dhaval2404.imagepicker.R.string.action_cancel,
                                null
                            )
                            .setIcon(R.drawable.ic_netology)
                            .show()
                    }
                    return
                }
                viewModel.likePost(id)
                lastUpdateTime = SystemClock.elapsedRealtime()
            }

            override fun onShared(text: String) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                if (shareIntent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(shareIntent)
                }
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

            override fun onImageDetails(id: Long, uri: String) {
                mainNavController?.navigate(
                    MainFragmentDirections.actionMainFragmentToImageDetailsFragment(
                        id,
                        uri
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
                if (viewModel.getAuthId() == 0L) {
                    activity?.let {
                        AlertDialog.Builder(it).setTitle(R.string.note)
                            .setMessage(R.string.auth_please_signin)
                            .setPositiveButton(R.string.log_in) { _, _ ->
                                mainNavController?.navigate(
                                    MainFragmentDirections.actionMainFragmentToLoginFragment(
                                        LoginFragment.LoginFlags.LOGIN
                                    )
                                )
                            }
                            .setNegativeButton(R.string.sign_up) { _, _ ->
                                mainNavController?.navigate(
                                    MainFragmentDirections.actionMainFragmentToLoginFragment(
                                        LoginFragment.LoginFlags.SIGNUP
                                    )
                                )
                            }
                            .setNeutralButton(
                                com.github.dhaval2404.imagepicker.R.string.action_cancel,
                                null
                            )
                            .setIcon(R.drawable.ic_netology)
                            .show()
                    }
                    return@setDebouncedListener
                }
                mainNavController?.navigate(R.id.action_mainFragment_to_addFragment)
            }
            refreshPostLayout.setOnRefreshListener {
                refreshPostLayout.isRefreshing = true
                viewModel.fetchData()
                lifecycleScope.launch {
                    delay(1000L)
                    try {
                        refreshPostLayout.isRefreshing = false
                    } catch (ex: IllegalArgumentException) {
                        Timber.e(ex)
                    }
                }
            }
            btnNewer.setDebouncedListener(500L) {
                viewModel.insertNewer()
                btnNewer.setVisibility(false)
                lifecycleScope.launch {
                    delay(50L)
                    rcViewPost.smoothScrollToPosition(0)
                }
            }
        }
        viewModel.authData.observe(viewLifecycleOwner) { auth ->
            activity?.invalidateMenu()
            adapter.notifyAuth(auth.id)
        }
        binding.refreshPostLayout.isRefreshing = false
        viewModel.newerPosts.observe(viewLifecycleOwner) { latest ->
            Timber.d("Live data posts: $latest")
            binding.btnNewer.setVisibility(latest.isNotEmpty())

        }
        viewModel.postsList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.refreshPostLayout.isRefreshing = false
                    binding.postProgress.setVisibility(false)
                    adapter.submitList(result.data.map { response ->
                        Post.parser(response).also { post ->
                            post?.isOwner = viewModel.getAuthId() == post?.authorId
                        }
                    }
                    )
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
                        is SocketTimeoutException -> showToast(R.string.error_timed_out_from_response)
                        is ConnectException -> if (SystemClock.elapsedRealtime() - lastUpdateTime > args.updateDebounce) {
                            showToast(R.string.error_no_internet_connection)
                        }
                        is IOException -> showToast(R.string.error_problem_with_internet_connection)
                        is FailedHttpRequestException -> showToast(
                            getString(
                                R.string.error_server_respond_error,
                                it.code().toString(),
                                it.message()
                            )
                        )
                        is HttpException -> showToast(
                            getString(
                                R.string.error_server_respond_error,
                                it.code().toString(),
                                it.message()
                            )
                        )
                        else -> showToast(getString(R.string.error_error, it.getErrorMessage()))
                    }
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main, menu)
        menu.setGroupVisible(R.id.unauthorized, viewModel.authData.value?.id == 0L)
        menu.setGroupVisible(R.id.authorized, viewModel.authData.value?.id != 0L)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out -> {
                activity?.let { activity ->
                    AlertDialog.Builder(activity).setTitle(R.string.sign_out)
                        .setMessage(R.string.auth_are_sure)
                        .setPositiveButton(R.string.sign_out) { _, _ ->
                            viewModel.clearAuth()
                        }
                        .setNegativeButton(
                            com.github.dhaval2404.imagepicker.R.string.action_cancel,
                            null
                        )
                        .setIcon(R.drawable.ic_netology)
                        .show()
                }
                true
            }
            R.id.signup -> {
                mainNavController?.navigate(
                    MainFragmentDirections.actionMainFragmentToLoginFragment(LoginFragment.LoginFlags.SIGNUP)
                )
                true
            }
            R.id.login -> {
                mainNavController?.navigate(
                    MainFragmentDirections.actionMainFragmentToLoginFragment(LoginFragment.LoginFlags.LOGIN)
                )
                true
            }
            else -> super.onMenuItemSelected(item)
        }
    }

    private companion object {
        private const val INNER_SPACING_RC_VIEW: Int = 5
    }
}