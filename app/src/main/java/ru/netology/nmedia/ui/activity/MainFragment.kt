package ru.netology.nmedia.ui.activity

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.clickWithDebounce
import ru.netology.nmedia.utils.setDebouncedListener
import ru.netology.nmedia.utils.toPostText
import ru.netology.nmedia.viewmodel.PostViewModel
import java.util.*

class MainFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val post = viewModel.post.value
        with(binding) {
            ivLikes.tag = post
            tvShareCount.tag = post
            tvPostText.text = post?.text
            tvPostTitle.text = post?.title
            tvDateTime.text = DateFormat.format("d MMMM yyyy, HH:mm", post?.date ?: Date().time)
            ivPostAvatar.setImageResource(post?.avatarId ?: R.drawable.ic_launcher_foreground)
            ivLikes.setImageResource(R.drawable.heart_outline)
            ivLikes.clickWithDebounce(300L) {
                viewModel.likePost(ivLikes.tag as Post)
            }
            ivShare.setDebouncedListener(300L) {
                viewModel.sharePost(tvShareCount.tag as Post)
            }

            viewModel.post.observe(viewLifecycleOwner) {
                tvCommentsCount.text = it.commentsCount.toPostText()
                tvLikesCount.text = it.likesCount.toPostText()
                tvShareCount.text = it.shareCount.toPostText()
                tvViewsCount.text = it.views.toPostText()
                ivLikes.tag = it
                tvShareCount.tag = it
                if (it.isLiked)  {
                    ivLikes.setImageResource(R.drawable.heart)
                } else {
                    ivLikes.setImageResource(R.drawable.heart_outline)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}