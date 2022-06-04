package ru.netology.nmedia.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.ui.adapter.PostAdapter
import ru.netology.nmedia.ui.adapter.PostListener
import ru.netology.nmedia.ui.adapter.decorators.LinearVerticalSpacingDecoration
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.*
import timber.log.Timber
import javax.inject.Inject

class MainFragment : BaseFragment<FragmentMainBinding>() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: PostViewModel by activityViewModels {
        viewModelFactory
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainBinding
        get() = FragmentMainBinding::inflate


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


    override fun clearKeyboard(editText: EditText?): Boolean = with(binding) {
        super.clearKeyboard(editText)
        cardViewEditMessage.setVisibility(false)
        editableMessageContainer.setVisibility(false)
        editText?.text?.clear()
        return !editText?.text.toString().checkIfNotEmpty()
    }


    private fun initView() {
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        mainNavController?.apply {
            val appBarConfiguration = AppBarConfiguration(graph)
            binding.toolbar.setupWithNavController(this, appBarConfiguration)
        }
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        val adapter = PostAdapter(object : PostListener {
            override fun onAdded(title: String, text: String): Long {
                return viewModel.addPost(title, text)
            }

            override fun onRemoved(id: Long): Boolean {
                Timber.d("Removed post with id $id")
                return viewModel.removePost(id)
            }

            override fun onEdit(id: Long, currentText: String): Boolean {
                with(binding) {
                    cardViewEditMessage.setVisibility(true)
                    editableMessageContainer.setVisibility(true)
                    tvPreviousText.text = currentText
                    etPostEdit.setText(currentText)
                    AndroidUtils.showKeyboard(etPostEdit, requireContext())
                    ivEditPostSend.setDebouncedListener(50L) {
                        when {
                            etPostEdit.text.toString()
                                .checkIfNotEmpty() && etPostEdit.text.toString() != currentText -> {
                                viewModel.editPost(id, etPostEdit.text.toString().trim())
                                clearKeyboard(etPostEdit)
                            }
                            !etPostEdit.text.toString().checkIfNotEmpty() -> Toast.makeText(
                                requireContext(),
                                requireContext().getString(R.string.text_is_unfilled),
                                Toast.LENGTH_SHORT
                            ).show()

                            etPostEdit.text.toString().trim() == currentText -> Toast.makeText(
                                requireContext(),
                                requireContext().getString(R.string.text_is_equal),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    return true
                }
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
        with(binding)
        {
            rcViewPost.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            rcViewPost.adapter = adapter
            rcViewPost.addItemDecoration(
                LinearVerticalSpacingDecoration(
                    AndroidUtils.dpToPx(activity as AppCompatActivity, 5)
                )
            )
            cardViewAddPost.setDebouncedListener(500L) {
                clearKeyboard(etPostEdit)
                mainNavController?.navigate(R.id.action_mainFragment_to_addFragment)
            }
            ivEditCancel.setDebouncedListener(50L) {
                clearKeyboard(etPostEdit)
            }
        }
        viewModel.postsList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}