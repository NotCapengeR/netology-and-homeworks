package ru.netology.nmedia.ui.base

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.viewbinding.ViewBinding
import ru.netology.nmedia.R
import ru.netology.nmedia.utils.AndroidUtils

abstract class BaseFragment<VB : ViewBinding> : Fragment(), MenuProvider {

    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
    private var popupMenu: PopupMenu? = null
    private var dialog: Dialog? = null
    protected val mainNavController: NavController? by lazy { activity?.findNavController(R.id.nav_host_fragment) }

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = requireNotNull(_binding) as VB

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        clearKeyboard()
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).addMenuProvider(this)
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).removeMenuProvider(this)
        _binding = null
    }

    protected fun clearKeyboard(editText: EditText? = null) {
        AndroidUtils.hideKeyboard(activity as AppCompatActivity)
        AndroidUtils.clearEditText(editText)
    }

    protected fun showKeyboard(mEtSearch: EditText) {
        AndroidUtils.showKeyboard(mEtSearch, requireContext())
    }

    protected fun onBackPressed(callback: () -> Unit = {}) {
        callback.invoke()
        mainNavController?.navigateUp()
    }

    protected fun showToast(message: String?, isLong: Boolean = false) {
        AndroidUtils.showToast(requireContext(), message, isLong)
    }

    protected fun showToast(@StringRes msgResId: Int, isLong: Boolean = false) {
        showToast(getString(msgResId), isLong)
    }

    protected fun showPopupMenu(inflater: () -> PopupMenu) {
        popupMenu = inflater.invoke()
        popupMenu?.show()
    }

    protected fun showDialog(inflater: () -> Dialog) {
        dialog = inflater.invoke()
        dialog?.show()
    }

    protected fun openUrl(url: String?) {
        AndroidUtils.openUrl(requireContext(), url)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.delete -> {
                true
            }
            else -> false
        }
    }

    protected fun Int.dpTpPx(): Int = AndroidUtils.dpToPx(activity as AppCompatActivity, this)
}