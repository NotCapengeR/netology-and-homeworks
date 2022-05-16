package ru.netology.nmedia.ui.activity

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.clickWithDebounce
import ru.netology.nmedia.utils.setDebouncedListener
import ru.netology.nmedia.utils.toPostText

class MainFragment : Fragment() {

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
        val post = Post(1, "fashkjfshjkhjfas", "124hjk14hj2k2", 0, 0)
        with(binding) {
            tvCommentsCount.text = post.commentsCount.toPostText()
            tvLikesCount.text = post.likesCount.toPostText()
            tvShareCount.text = post.shareCount.toPostText()
            tvViewsCount.text = post.views.toPostText()
            tvPostText.text = post.text
            tvPostTitle.text = post.title
            tvDateTime.text = DateFormat.format("d MMMM yyyy, HH:mm", post.date)
            ivPostAvatar.setImageResource(post.avatarId)
            ivLikes.clickWithDebounce(300L) {
                if (!post.isLiked) {
                    post.likesCount++
                    ivLikes.setImageResource(R.drawable.heart)
                } else {
                    post.likesCount--
                    ivLikes.setImageResource(R.drawable.heart_outline)
                }
                post.isLiked = !post.isLiked
                tvLikesCount.text = post.likesCount.toPostText()
            }
            ivShare.setDebouncedListener(300L) {
                post.shareCount++
                tvLikesCount.text = post.likesCount.toPostText()
            }
            ivLikes.setImageResource(R.drawable.heart_outline)
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