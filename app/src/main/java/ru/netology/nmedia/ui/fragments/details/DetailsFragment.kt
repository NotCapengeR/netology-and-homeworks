package ru.netology.nmedia.ui.fragments.details

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.database.dto.Post
import ru.netology.nmedia.databinding.DetailsFragmentBinding
import ru.netology.nmedia.ui.adapter.PostAdapter
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.getAppComponent
import ru.netology.nmedia.utils.setDebouncedListener
import ru.netology.nmedia.utils.setVisibility
import ru.netology.nmedia.utils.toPostText
import javax.inject.Inject

class DetailsFragment : BaseFragment<DetailsFragmentBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val args: DetailsFragmentArgs by navArgs()
    private val viewModel: DetailsViewModel by activityViewModels {
        viewModelFactory
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DetailsFragmentBinding
        get() = DetailsFragmentBinding::inflate

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
        viewModel.loadPost(id)
        viewModel.post.observe(viewLifecycleOwner) { post ->
            if (post != null) {
                tvDateTime.text = Post.parseEpochSeconds(post.date)
                ivLikes.tag = post.id
                ytCancel.tag = post.id
                ivComments.tag = post.id
                ivShare.tag = post.id
                menuButton.tag = post.id
                ivLikes.text = post.likes.toPostText()
                ivComments.text = post.comments.toPostText()
                Glide.with(requireContext())
                    .load(post.avatarId)
                    .centerCrop()
                    .into(ivPostAvatar)
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
                yTLayout.setVisibility(post.video != null)
                if (post.video != null) {
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
                                    "${PostAdapter.YOUTUBE_URL}${post.video.id}"
                                )
                            )
                        )
                    }
                    ytCancel.setDebouncedListener(50L) {
                        viewModel.removeLink(it.tag as Long)
                        yTLayout.setVisibility(false)
                    }

                }
            }
        }

        ivLikes.setDebouncedListener(50L) {
            viewModel.likePost(it.tag as Long)
        }
        ivShare.setDebouncedListener(50L) {
            viewModel.sharePost(it.tag as Long)
        }
        ivComments.setDebouncedListener(50L) {
            viewModel.commentPost(it.tag as Long)
        }
        menuButton.setDebouncedListener(50L) {
            showPopupMenu(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.empty, menu)
    }

    override fun showPopupMenu(view: View, key: String?) {
        val id: Long = view.tag as Long
        popupMenu = PopupMenu(view.context, view)
        popupMenu?.menu?.add(0, REMOVE_ID, Menu.NONE, getString(R.string.post_remove))
        popupMenu?.menu?.add(0, EDIT_ID, Menu.NONE, getString(R.string.edit))


        popupMenu?.setOnMenuItemClickListener {
            when (it.itemId) {

                REMOVE_ID -> {
                    viewModel.removePost(id)
                    onBackPressed()
                }

                EDIT_ID -> mainNavController?.navigate(
                    DetailsFragmentDirections.actionDetailsFragmentToEditFragment(
                        args.postText,
                        args.postTitle,
                        id
                    )
                )
            }
            return@setOnMenuItemClickListener true
        }
        super.showPopupMenu(view, key)
    }

    private companion object {
        private const val REMOVE_ID: Int = 1
        private const val EDIT_ID: Int = 2
    }
}