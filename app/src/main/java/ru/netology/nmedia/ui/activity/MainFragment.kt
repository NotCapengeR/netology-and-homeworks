package ru.netology.nmedia.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.ui.adapter.PostAdapter
import ru.netology.nmedia.ui.adapter.PostListener
import ru.netology.nmedia.ui.decorators.*
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.utils.AndroidUtils
import timber.log.Timber
import java.util.*

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostViewModel by activityViewModels()

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
        val adapter = PostAdapter(object : PostListener {
            override fun onAdded(title: String, text: String) {
                viewModel.addPost(title, text)
            }

            override fun onRemoved(id: Long) {
                viewModel.removePost(id)
                Timber.d("Fun: ${viewModel.removePost(id)}\n_______________________")
            }

            override fun onEdit(id: Long, newText: String) {
                viewModel.editPost(id, newText)
            }

            override fun onLiked(id: Long) {
                viewModel.likePost(id)
            }

            override fun onShared(id: Long) {
                viewModel.sharePost(id)
            }
        })
        with(binding) {
            rcViewPost.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            rcViewPost.adapter = adapter
            rcViewPost.addItemDecoration(
                LinearVerticalSpacingDecoration(
                    AndroidUtils.dpToPx(activity as AppCompatActivity, 5)
                )
            )
        }
        viewModel.liveData.observe(activity as AppCompatActivity) {
            adapter.submitList(it)
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