package ru.netology.nmedia.ui.fragments.edit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.text.util.Linkify
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.database.dto.Post
import ru.netology.nmedia.database.dto.Post.Companion.POST_DATE_PATTERN
import ru.netology.nmedia.ui.adapter.PostAdapter.Companion.YOUTUBE_URL
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.databinding.EditFragmentBinding
import ru.netology.nmedia.utils.*
import timber.log.Timber
import javax.inject.Inject

class EditFragment : BaseFragment<EditFragmentBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val args: EditFragmentArgs by navArgs()
    private val viewModel: EditViewModel by activityViewModels {
        viewModelFactory
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> EditFragmentBinding
        get() = EditFragmentBinding::inflate

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
        setHasOptionsMenu(true)
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            toolbar.setupWithNavController(this, appBarConfiguration)
        }
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        val id = args.postId
        if (id != viewModel.post.value?.id) {
            viewModel.loadPost(id)
        }

        viewModel.post.observe(viewLifecycleOwner) { post ->
            if (post != null) {
                if (post.text != tvPostText.text.toString()) {
                    tvPostText.setText(post.text)
                }
                tvPostTitle.text = post.title
                tvDateTime.text = Post.parseEpochSeconds(post.date)
                ivLikes.tag = post.id
                ytCancel.tag = post.id
                ivComments.tag = post.id
                ivShare.tag = post.id
                ivLikes.text = post.likes.toPostText()
                ivComments.text = post.comments.toPostText()
                Glide.with(requireContext())
                    .load(post.avatarId)
                    .centerCrop()
                    .into(ivPostAvatar)
                Timber.d("Post avatar: ${post.avatarId}")
                ivShare.text = post.shared.toPostText()
                ivLikes.isChecked = post.isLiked

                if (post.isLiked) {
                    ivLikes.setIconResource(R.drawable.heart)
                } else {
                    ivLikes.setIconResource(R.drawable.heart_outline)
                }
                if (post.video != null) {
                    yTLayout.setVisibility(true)
                    ytVideoDuration.text = post.video.duration
                    ytAuthor.text = post.video.author
                    ytTitle.text = post.video.title
                    Glide.with(requireContext())
                        .load(post.video.thumbnailUrl)
                        .centerCrop()
                        .into(ytThumbnail)
                    ytThumbnail.setDebouncedListener {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW, Uri.parse(
                                    "$YOUTUBE_URL${post.video.id}"
                                )
                            )
                        )
                    }
                    ytCancel.setDebouncedListener(50L) {
                        viewModel.removeLink(it.tag as Long)
                        yTLayout.setVisibility(false)
                    }

                } else {
                    yTLayout.setVisibility(false)
                }
            }
        }

        cancelButton.setOnClickListener {
            onBackPressed()
        }
        ivLikes.setDebouncedListener(50L) {
            saveState()
            viewModel.likePost(it.tag as Long)
        }
        ivShare.setDebouncedListener(50L) {
            saveState()
            viewModel.sharePost(it.tag as Long)
        }
        ivComments.setDebouncedListener(50L) {
            saveState()
            viewModel.commentPost(it.tag as Long)
        }
        cardViewSendPost.setDebouncedListener {
            when {
                !tvPostText.text.toString().checkIfNotEmpty()
                        || !tvPostTitle.text.toString().checkIfNotEmpty() ->
                    showToast(R.string.text_is_unfilled)
                else -> {
                    Linkify.addLinks(tvPostText, Linkify.WEB_URLS)
                    val url: String? =
                        if (tvPostText.urls.isNotEmpty()) tvPostText.urls.first().url else null
                    viewModel.editPost(
                        id,
                        tvPostText.text.toString().trim(),
                        url
                    )
                    clearKeyboard()
                    onBackPressed()
                }
            }
        }
    }

    override fun onDestroyView() {
        saveState()
        super.onDestroyView()
    }

    private fun saveState() {
        viewModel.saveText(binding.tvPostText.text.toString())
        viewModel.saveTitle(binding.tvPostTitle.text.toString())
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.empty, menu)
    }
}