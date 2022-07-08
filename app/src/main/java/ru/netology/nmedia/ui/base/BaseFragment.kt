package ru.netology.nmedia.ui.base

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
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
    private var popupMenu: PopupMenu? = null
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
        clearKeyboard()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = ""
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
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
        _binding = null
    }

    protected fun clearKeyboard(editText: EditText? = null) {
        AndroidUtils.hideKeyboard(activity as AppCompatActivity)
        AndroidUtils.clearEditText(editText)
    }

    protected fun showKeyboard(mEtSearch: EditText) {
        AndroidUtils.showKeyboard(mEtSearch, requireContext())
    }

    protected open fun onBackPressed() {
        mainNavController?.navigateUp()
    }

    protected fun showToast(message: String?, isLong: Boolean = false) {
        AndroidUtils.showToast(requireContext(), message, isLong)
    }

    protected fun showToast(@StringRes msgResId: Int, isLong: Boolean = false) {
        showToast(getString(msgResId), isLong)
    }

    protected fun showPopupMenu(view: View, inflater: (View) -> PopupMenu) {
        popupMenu = inflater.invoke(view)
        popupMenu?.show()
    }

    protected fun openUrl(url: String?) {
        AndroidUtils.openUrl(requireContext(), url)
    }
}