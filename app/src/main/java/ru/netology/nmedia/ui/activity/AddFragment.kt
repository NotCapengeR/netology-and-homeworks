package ru.netology.nmedia.ui.activity

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.AddFragmentBinding
import ru.netology.nmedia.ui.base.BaseFragment
import ru.netology.nmedia.ui.viewmodel.PostViewModel
import ru.netology.nmedia.ui.viewmodel.ViewModelFactory
import ru.netology.nmedia.utils.checkIfNotEmpty
import ru.netology.nmedia.utils.getAppComponent
import ru.netology.nmedia.utils.setDebouncedListener
import java.util.*
import javax.inject.Inject

class AddFragment : BaseFragment<AddFragmentBinding>(), View.OnClickListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: PostViewModel by activityViewModels() {
        viewModelFactory
    }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> AddFragmentBinding
        get() = AddFragmentBinding::inflate

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getAppComponent().inject(this)
        viewModel.currentTag(ADD_FRAGMENT_TAG)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tvDateTime.text = DateFormat.format("d MMMM yyyy, HH:mm", Date().time)
            cardViewSendPost.setDebouncedListener(600L, this@AddFragment)
        }
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
                        viewModel.addPost(tvPostTitle.text.toString().trim(), tvPostText.text.toString().trim())
                        activity?.onBackPressed()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            requireContext().getString(R.string.text_is_unfilled),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            else -> {/* do nothing */}
        }
    }
}