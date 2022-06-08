package ru.netology.nmedia.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.EditFragmentBinding
import ru.netology.nmedia.ui.adapter.PostAdapter.Companion.YOUTUBE_URL
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.*
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
        val post = viewModel.getPostById(id)
        if (post != null) {
            tvDateTime.text = DateFormat.format("d MMMM yyyy, HH:mm", post.date)
            ivLikes.text = post.likes.toPostText()
            ivComments.text = post.comments.toPostText()
            ivPostAvatar.setImageResource(post.avatarId)
            ivShare.text = post.shared.toPostText()
            ivLikes.isChecked = post.isLiked

            if (post.isLiked) {
                ivLikes.setIconResource(R.drawable.heart)
            } else {
                ivLikes.setIconResource(R.drawable.heart_outline)
            }
            if (post.video != null) {
                val video = post.video.items.first()
                val thumbnail = video.snippet.thumbnails.thumbnail
                ytCancel.setVisibility(false)
                ytVideoDuration.text =
                    video.contentDetails.duration
                        .replace("PT", "")
                        .replace('S', ' ')
                        .replace('H', ':')
                        .replace('M', ':')
                ytAuthor.text = video.snippet.channelTitle
                ytTitle.text = video.snippet.title
                Glide.with(requireContext())
                    .load(thumbnail.url)
                    .centerCrop()
                    .into(ytThumbnail)
                ytThumbnail.setDebouncedListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
                        "$YOUTUBE_URL${video.id}")))
                }

            }
        }
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