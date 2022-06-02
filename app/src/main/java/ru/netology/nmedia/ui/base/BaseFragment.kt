package ru.netology.nmedia.ui.base

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import ru.netology.nmedia.R
import ru.netology.nmedia.ui.activity.FragmentInteractor
import ru.netology.nmedia.ui.activity.MainActivity
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.utils.checkIfNotEmpty

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB


    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = requireNotNull(_binding) as VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        (activity as MainActivity).onStartFragment()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.delete -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as FragmentInteractor).onStopFragment()
        _binding = null
    }

    open fun clearKeyboard(editText: EditText? = null): Boolean? {
        AndroidUtils.hideKeyboard(activity as AppCompatActivity)
        return if (editText != null) {
            !editText.text.toString().checkIfNotEmpty()
        } else null
    }

    open fun onBackPressed(): String? {
        activity?.onBackPressed()
        val index =
            parentFragmentManager.fragments.indexOf(parentFragmentManager.fragments.last()) - 1
        return if (index >= 0) {
            parentFragmentManager.fragments[index].tag
        } else null
    }
}