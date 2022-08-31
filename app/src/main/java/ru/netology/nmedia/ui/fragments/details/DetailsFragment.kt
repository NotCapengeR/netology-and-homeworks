package ru.netology.nmedia.ui.fragments.details

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import ru.netology.nmedia.databinding.DetailsFragmentBinding
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.repository.dto.Post.Companion.ATTACHMENTS_BASE_URL
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.fragments.login.LoginFragment
import ru.netology.nmedia.ui.viewmodels.ViewModelFactory
import ru.netology.nmedia.utils.*
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
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            toolbar.setupWithNavController(this, appBarConfiguration)
        }
        val id = args.postId
        viewModel.loadPost(id)
        viewModel.post.observe(viewLifecycleOwner) { post ->
            if (post != null) {
                tvDateTime.text = Mapper.parseEpochSeconds(post.date)
                ivLikes.tag = post.id
                ivComments.tag = post.id
                ivShare.tag = post.id
                menuButton.tag = post.id
                ivLikes.text = post.likes.toPostText()
                ivComments.text = post.comments.toPostText()
                if (post.avatar.isNotBlank() && post.avatar.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load("${Post.AVATARS_BASE_URL}${post.avatar}")
                        .placeholder(R.drawable.ic_baseline_account_circle_24)
                        .error(R.drawable.alert_circle)
                        .circleCrop()
                        .timeout(10_000)
                        .into(ivPostAvatar)
                } else {
                    Glide.with(requireContext()).clear(ivPostAvatar)
                }
                ivShare.text = post.shared.toPostText()
                tvPostTitle.text = post.title
                tvPostText.text = post.text
                ivLikes.isChecked = post.isLiked
                menuButton.setVisibility(post.isOwner)
                Linkify.addLinks(tvPostText, Linkify.WEB_URLS)

                if (post.isLiked) {
                    ivLikes.setIconResource(R.drawable.heart)
                } else {
                    ivLikes.setIconResource(R.drawable.heart_outline)
                }
                ivAttachment.setVisibility(post.attachment != null)
                if (post.attachment != null && post.attachment.type == Attachment.AttachmentType.IMAGE) {
                    Glide.with(requireContext())
                        .load("$ATTACHMENTS_BASE_URL${post.attachment.name}")
                        .placeholder(R.drawable.push_nmedia)
                        .error(ColorDrawable(Color.RED))
                        .centerCrop()
                        .timeout(10_000)
                        .into(ivAttachment)
                    ivAttachment.setDebouncedListener(50L) {
                        mainNavController?.navigate(
                            DetailsFragmentDirections.actionDetailsFragmentToImageDetailsFragment(
                                args.postId,
                                "${ATTACHMENTS_BASE_URL}${post.attachment.name}"
                            )
                        )
                    }
                } else {
                    Glide.with(requireContext()).clear(ivAttachment)
                }
            }
        }

        ivLikes.setDebouncedListener(50L) {
            if (viewModel.getAuthId() == 0L) {
                activity?.let { activity ->
                    AlertDialog.Builder(activity).setTitle(R.string.note)
                        .setMessage(R.string.auth_please_signin)
                        .setPositiveButton(R.string.log_in) { _, _ ->
                            mainNavController?.navigate(
                                DetailsFragmentDirections.actionDetailsFragmentToLoginFragment(
                                    LoginFragment.LoginFlags.LOGIN
                                )
                            )
                        }
                        .setNegativeButton(R.string.sign_up) { _, _ ->
                            mainNavController?.navigate(
                                DetailsFragmentDirections.actionDetailsFragmentToLoginFragment(
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
            viewModel.likePost(it.tag as Long)
        }
        ivShare.setDebouncedListener(50L) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, binding.tvPostText.text.toString())
                type = "text/plain"
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val shareIntent =
                Intent.createChooser(intent, getString(R.string.chooser_share_post))
            if (shareIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(shareIntent)
            }
        }
        ivComments.setDebouncedListener(50L) {
            viewModel.commentPost(it.tag as Long)
        }
        menuButton.setDebouncedListener(50L) { view ->
            showPopupMenu {
                val postId: Long = view.tag as Long
                PopupMenu(view.context, view).apply {
                    menu?.add(0, REMOVE_ID, Menu.NONE, getString(R.string.post_remove))
                    menu?.add(0, EDIT_ID, Menu.NONE, getString(R.string.edit))
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {

                            REMOVE_ID -> {
                                viewModel.removePost(postId)
                                onBackPressed()
                            }

                            EDIT_ID -> mainNavController?.navigate(
                                DetailsFragmentDirections.actionDetailsFragmentToEditFragment(
                                    args.postText,
                                    args.postTitle,
                                    postId
                                )
                            )
                        }
                        return@setOnMenuItemClickListener true
                    }
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.empty, menu)
    }

    private companion object {
        private const val REMOVE_ID: Int = 1
        private const val EDIT_ID: Int = 2
    }
}