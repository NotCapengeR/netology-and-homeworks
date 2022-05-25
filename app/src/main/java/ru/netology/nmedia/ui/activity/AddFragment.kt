package ru.netology.nmedia.ui.activity

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.AddFragmentBinding
import ru.netology.nmedia.databinding.FragmentMainBinding
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.checkIfNotEmpty
import ru.netology.nmedia.utils.clickWithDebounce
import ru.netology.nmedia.utils.getAppComponent
import ru.netology.nmedia.utils.setDebouncedListener
import java.util.*
import javax.inject.Inject

class AddFragment : Fragment(), View.OnClickListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var _binding: AddFragmentBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: PostViewModel by activityViewModels() {
        viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddFragmentBinding.inflate(inflater, container, false)
        getAppComponent().inject(this)
        viewModel.currentTag(ADD_FRAGMENT_TAG)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tvDateTime.text = DateFormat.format("d MMMM yyyy, HH:mm", Date().time)
            cardViewSendPost.setDebouncedListener(600L, this@AddFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddFragment()

        const val ADD_FRAGMENT_TAG: String = "Add fragment"
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.cardViewSendPost -> {
                with(binding) {
                    if (tvPostTitle.text.toString().checkIfNotEmpty() && tvPostText.text.toString().checkIfNotEmpty()) {
                        viewModel.addPost(tvPostTitle.text.toString(), tvPostText.text.toString())
                        activity?.onBackPressed()
                    }
                }

            }
            else -> {/* do nothing */}
        }
    }
}