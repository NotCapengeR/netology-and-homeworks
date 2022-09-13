package ru.netology.nmedia.ui.fragments.edit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.util.Linkify
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.EditFragmentBinding
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.repository.dto.Post.Companion.ATTACHMENTS_BASE_URL
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodels.ViewModelFactory
import ru.netology.nmedia.utils.*
import javax.inject.Inject

class EditFragment : BaseFragment<EditFragmentBinding>() {

    @Inject lateinit var viewModelFactory: ViewModelFactory
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
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            toolbar.setupWithNavController(this, appBarConfiguration)
        }
        val id = args.postId
        if (id != viewModel.post.value?.id) {
            viewModel.loadPost(id)
        }
        viewModel.clearErrorMsg()
        viewModel.errorMsg.observe(viewLifecycleOwner) { message ->
            if (message != null && !message.isBlankOrEmpty()) {
                showToast(message)
            }
        }

        viewModel.post.observe(viewLifecycleOwner) { post ->
            if (post != null) {
                if (post.text != tvPostText.text.toString()) {
                    tvPostText.setText(post.text)
                }
                tvPostTitle.text = post.title
                tvDateTime.text = Mapper.parseEpochSeconds(post.date)
                ivLikes.tag = post.id
                ivComments.tag = post.id
                ivShare.tag = post.id
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
                ivLikes.isChecked = post.isLiked

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
                            EditFragmentDirections.actionEditFragmentToImageDetailsFragment(
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
        viewModel.isLoaded.observe(viewLifecycleOwner) { isLoaded ->
            if (isLoaded) {
                onBackPressed()
            }
        }
        viewModel.isUpdating.observe(viewLifecycleOwner) {
            binding.postProgress.setVisibility(it)
        }

        cancelButton.setOnClickListener {
            onBackPressed()
        }
        ivLikes.setDebouncedListener(50L) {
            saveState()
            viewModel.likePost(it.tag as Long, tvPostText.text.toString())
        }
        ivShare.setDebouncedListener(50L) {
            saveState()
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

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.empty, menu)
    }
}