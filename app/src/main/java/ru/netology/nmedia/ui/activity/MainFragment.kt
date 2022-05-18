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
import ru.netology.nmedia.utils.clickWithDebounce
import ru.netology.nmedia.utils.toPostText
import ru.netology.nmedia.viewmodel.PostViewModel

class MainFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = requireNotNull(_binding)

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
        viewModel.post.observe(viewLifecycleOwner) {
            with(binding) {
                tvPostText.text = it.text
                tvPostTitle.text = it.title
                tvDateTime.text = DateFormat.format("d MMMM yyyy, HH:mm", it.date)
                tvCommentsCount.text = it.commentsCount.toPostText()
                tvLikesCount.text = it.likesCount.toPostText()
                tvShareCount.text = it.shareCount.toPostText()
                tvViewsCount.text = it.views.toPostText()
                ivPostAvatar.setImageResource(it.avatarId)
                if (it.isLiked) {
                    ivLikes.setImageResource(R.drawable.heart)
                } else {
                    ivLikes.setImageResource(R.drawable.heart_outline)
                }
                ivLikes.clickWithDebounce(50L) {
                    viewModel.likePost(it)
                }
                ivShare.clickWithDebounce(50L) {
                    viewModel.sharePost(it)
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