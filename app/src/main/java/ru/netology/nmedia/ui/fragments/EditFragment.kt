package ru.netology.nmedia.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.text.util.Linkify
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.EditFragmentBinding
import ru.netology.nmedia.dto.Post.Companion.POST_DATE_PATTERN
import ru.netology.nmedia.ui.adapter.PostAdapter.Companion.YOUTUBE_URL
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.*
import timber.log.Timber
import javax.inject.Inject

class EditFragment : BaseFragment<EditFragmentBinding>() {

    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var prefs: SharedPreferences
    private val args: EditFragmentArgs by navArgs()
    private val viewModel: PostViewModel by activityViewModels {
        viewModelFactory
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> EditFragmentBinding
        get() = EditFragmentBinding::inflate

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
        val id = args.postId
        val post = viewModel.getPostById(id)
        if (!(prefs.getString(EDIT_FRAGMENT_TEXT, ""))?.trim().isNullOrBlank()
            && !(prefs.getString(EDIT_FRAGMENT_TITLE, ""))?.trim().isNullOrBlank()
            && prefs.getLong(EDIT_FRAGMENT_POST_ID, 0L) == id
        ) {
            tvPostText.setText(prefs.getString(EDIT_FRAGMENT_TEXT, " "))
            tvPostTitle.setText(prefs.getString(EDIT_FRAGMENT_TITLE, " "))
        } else {
            tvPostText.setText(post?.text)
            tvPostTitle.setText(post?.title)
        }
        prefs.edit {
            putLong(EDIT_FRAGMENT_POST_ID, id)
        }
        if (post != null) {
            tvDateTime.text = DateFormat.format(POST_DATE_PATTERN, post.date)
            ivLikes.tag = post.id
            ytCancel.tag = post.id
            ivComments.tag = post.id
            ivShare.tag = post.id
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
                    saveState()
                }
                ytCancel.setDebouncedListener(50L) {
                    lifecycleScope.launch {
                        viewModel.removeLink(it.tag as Long)
                        yTLayout.setVisibility(false)
                    }
                }

            } else {
                yTLayout.setVisibility(false)
            }
        }
        cancelButton.setOnClickListener {
            onBackPressed()
        }
        ivLikes.setDebouncedListener(50L) {
            lifecycleScope.launch {
                viewModel.likePost(it.tag as Long)
                val newPost = viewModel.getPostById(it.tag as Long)
                ivLikes.text = newPost?.likes?.toPostText()
                ivLikes.isChecked = newPost?.isLiked ?: false

                if (newPost?.isLiked == true) {
                    ivLikes.setIconResource(R.drawable.heart)
                } else {
                    ivLikes.setIconResource(R.drawable.heart_outline)
                }
            }
        }
        ivShare.setDebouncedListener(50L) {
            lifecycleScope.launch {
                viewModel.sharePost(it.tag as Long)
                val newPost = viewModel.getPostById(it.tag as Long)
                ivShare.text = newPost?.shared?.toPostText()
            }
        }
        ivComments.setDebouncedListener(50L) {
            lifecycleScope.launch {
                viewModel.commentPost(it.tag as Long)
                val newPost = viewModel.getPostById(it.tag as Long)
                ivComments.text = newPost?.comments?.toPostText()
            }
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
                    lifecycleScope.launch {
                        viewModel.editPost(
                            id,
                            tvPostText.text.toString().trim(),
                            tvPostTitle.text.toString().trim(),
                            url
                        )
                    }
                    clearKeyboard()
                    onBackPressed()
                    prefs.edit {
                        putString(EDIT_FRAGMENT_TEXT, " ")
                        putString(EDIT_FRAGMENT_TITLE, " ")
                        putLong(EDIT_FRAGMENT_POST_ID, 0L)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        if (prefs.getLong(EDIT_FRAGMENT_POST_ID, 0L) != 0L) {
            saveState()
        }
        super.onDestroyView()
    }


    private fun saveState() = with(binding) {
        Timber.d("State has been saved!!!")
        prefs.edit {
            putString(EDIT_FRAGMENT_TEXT, tvPostText.text.toString())
            putString(EDIT_FRAGMENT_TITLE, tvPostTitle.text.toString())
            putLong(EDIT_FRAGMENT_POST_ID, args.postId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.empty, menu)
    }


    private companion object {
        private const val EDIT_FRAGMENT_POST_ID: String ="edit_fragment_post_id"
        private const val EDIT_FRAGMENT_TEXT: String = "edit_fragment_text"
        private const val EDIT_FRAGMENT_TITLE: String = "edit_fragment_title"
    }

}