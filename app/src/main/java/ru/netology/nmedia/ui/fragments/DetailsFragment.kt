package ru.netology.nmedia.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.text.util.Linkify
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.ui.adapter.PostAdapter
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.databinding.DetailsFragmentBinding
import ru.netology.nmedia.dto.Post.Companion.POST_DATE_PATTERN
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.getAppComponent
import ru.netology.nmedia.utils.setDebouncedListener
import ru.netology.nmedia.utils.setVisibility
import ru.netology.nmedia.utils.toPostText
import javax.inject.Inject

class DetailsFragment : BaseFragment<DetailsFragmentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DetailsFragmentBinding
        get() = DetailsFragmentBinding::inflate

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
        val id = requireArguments().get(Post.POST_ID) as Long
        val post = viewModel.getPostById(id)
        if (post != null) {
            tvDateTime.text = DateFormat.format(POST_DATE_PATTERN, post.date)
            ivLikes.tag = post.id
            ytCancel.tag = post.id
            ivComments.tag = post.id
            ivShare.tag = post.id
            menuButton.tag = post.id
            ivLikes.text = post.likes.toPostText()
            ivComments.text = post.comments.toPostText()
            ivPostAvatar.setImageResource(post.avatarId)
            ivShare.text = post.shared.toPostText()
            tvPostTitle.text = post.title
            tvPostText.text = post.text
            ivLikes.isChecked = post.isLiked
            Linkify.addLinks(tvPostText, Linkify.WEB_URLS)

            if (post.isLiked) {
                ivLikes.setIconResource(R.drawable.heart)
            } else {
                ivLikes.setIconResource(R.drawable.heart_outline)
            }
            if (post.video != null) {
                yTLayout.setVisibility(true)
                ytVideoDuration.text = post.video.duration
                ytAuthor.text = post.video.author
                ytTitle.text =  post.video.title
                Glide.with(requireContext())
                    .load(post.video.thumbnailUrl)
                    .centerCrop()
                    .into(ytThumbnail)
                ytThumbnail.setDebouncedListener {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse(
                                "${PostAdapter.YOUTUBE_URL}${post.video.id}"
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

        ivLikes.setDebouncedListener(50L) {
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
        ivShare.setDebouncedListener(50L) {
            viewModel.sharePost(it.tag as Long)
            val newPost = viewModel.getPostById(it.tag as Long)
            ivShare.text = newPost?.shared?.toPostText()
        }
        ivComments.setDebouncedListener(50L) {
            viewModel.commentPost(it.tag as Long)
            val newPost = viewModel.getPostById(it.tag as Long)
            ivComments.text = newPost?.comments?.toPostText()
        }
        menuButton.setDebouncedListener(50L) {
            showPopupMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.empty, menu)
    }

    override fun showPopupMenu(args: Bundle?) {
        val view = binding.menuButton
        val id: Long = view.tag as Long
        val currentText = requireArguments().get(Post.POST_TEXT) as String
        val currentTitle = requireArguments().get(Post.POST_TITLE) as String
        popupMenu = PopupMenu(view.context, view)
        popupMenu?.menu?.add(0, REMOVE_ID, Menu.NONE, getString(R.string.post_remove))
        popupMenu?.menu?.add(0, EDIT_ID, Menu.NONE, getString(R.string.edit))


        popupMenu?.setOnMenuItemClickListener {
            when (it.itemId) {
                REMOVE_ID ->  {
                    viewModel.removePost(id).also { isDeleted ->
                        if (isDeleted) {
                            showToast(R.string.post_deleted)
                            onBackPressed()
                        }
                    }
                }


                EDIT_ID -> mainNavController?.navigate(
                    R.id.action_detailsFragment_to_editFragment, bundleOf(
                        Post.POST_ID to id,
                        Post.POST_TEXT to currentText,
                        Post.POST_TITLE to currentTitle
                    )
                )
            }
            return@setOnMenuItemClickListener true
        }
        super.showPopupMenu(args)
    }

    private companion object {
        private const val REMOVE_ID: Int = 1
        private const val EDIT_ID: Int = 2
    }
}