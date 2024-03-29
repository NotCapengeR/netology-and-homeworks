package ru.netology.nmedia.ui.fragments.image

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ImageDetailsFragmentBinding
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodels.ViewModelFactory
import ru.netology.nmedia.utils.getAppComponent
import ru.netology.nmedia.utils.toPostText
import javax.inject.Inject

class ImageDetailsFragment : BaseFragment<ImageDetailsFragmentBinding>() {

    private val args: ImageDetailsFragmentArgs by navArgs()
    @Inject lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: ImageDetailsViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ImageDetailsFragmentBinding
        get() = ImageDetailsFragmentBinding::inflate

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
        viewModel.loadPost(args.postId)
        viewModel.post.observe(viewLifecycleOwner) {
            ivLikes.text = it.likes.toPostText()
        }
        Glide.with(requireContext())
            .load(args.uri)
            .placeholder(R.drawable.push_nmedia)
            .error(R.drawable.alert_circle)
            .override(1600, 1200)
            .fitCenter()
            .timeout(10_000)
            .into(ivAttachment)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.empty, menu)
    }
}