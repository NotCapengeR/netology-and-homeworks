package ru.netology.nmedia.ui.base

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.viewbinding.ViewBinding
import ru.netology.nmedia.R
import ru.netology.nmedia.utils.AndroidUtils

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
    protected val mainNavController: NavController? by lazy { activity?.findNavController(R.id.nav_host_fragment) }

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = requireNotNull(_binding) as VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        (activity as FragmentObserver).onStartFragment()
        clearKeyboard()
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
        (activity as FragmentObserver).onStopFragment()
        _binding = null
    }

    protected open fun clearKeyboard(editText: EditText? = null) {
        AndroidUtils.hideKeyboard(activity as AppCompatActivity)
        editText?.text?.clear()
        editText?.clearFocus()
    }

    protected open fun showKeyboard(mEtSearch: EditText) {
        AndroidUtils.showKeyboard(mEtSearch, requireContext())
    }

    protected fun onBackPressed() {
        (activity as AppCompatActivity).onBackPressed()
    }

    fun showToast(message: String?, isLong: Boolean = false) {
        if (message == null) return
        if (isLong) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showToast(@StringRes msgResId: Int, isLong: Boolean = false) {
        showToast(getString(msgResId), isLong)
    }
}