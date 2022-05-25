package ru.netology.nmedia.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nmedia.App
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.ui.adapter.PostAdapter
import ru.netology.nmedia.ui.adapter.PostListener
import ru.netology.nmedia.ui.decorators.LinearVerticalSpacingDecoration
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.AndroidUtils
import timber.log.Timber
import javax.inject.Inject

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = requireNotNull(_binding)
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: PostViewModel by activityViewModels() {
        viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        (requireContext().applicationContext as App).appComponent.inject(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PostAdapter(object : PostListener {
            override fun onAdded(title: String, text: String): Long {
                return viewModel.addPost(title, text)
            }

            override fun onRemoved(id: Long): Boolean {
                Timber.d("Removed post with id $id")
                return viewModel.removePost(id)
            }

            override fun onEdit(id: Long, newText: String): Boolean {
                return viewModel.editPost(id, newText)
            }

            override fun onLiked(id: Long): Boolean {
                return viewModel.likePost(id)
            }

            override fun onShared(id: Long): Int {
                return viewModel.sharePost(id)
            }

            override fun onCommented(id: Long): Int {
                return viewModel.commentPost(id)
            }

            override fun onPostMoved(id: Long, movedBy: Int): Boolean {
                return viewModel.movePost(id, movedBy)
            }
        })
        adapter.setHasStableIds(true)
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
        viewModel.postsList.observe(viewLifecycleOwner) {
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