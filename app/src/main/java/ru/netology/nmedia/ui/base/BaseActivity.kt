package ru.netology.nmedia.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.PopupMenu
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.utils.AndroidUtils

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater) -> VB
    private var popupMenu: PopupMenu? = null
    protected val mainNavController: NavController? by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                as NavHostFragment).navController
    }
    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = requireNotNull(_binding) as VB

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater).also { setContentView(it.root) }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    protected fun showToast(message: String?, isLong: Boolean = false) {
        AndroidUtils.showToast(this, message, isLong)
    }

    protected fun showToast(@StringRes msgResId: Int, isLong: Boolean = false) {
        showToast(getString(msgResId), isLong)
    }

    protected fun openUrl(url: String?) {
        AndroidUtils.openUrl(this, url)
    }


    protected fun clearKeyboard(editText: EditText? = null) {
        AndroidUtils.hideKeyboard(this)
        AndroidUtils.clearEditText(editText)
    }

    protected fun showKeyboard(mEtSearch: EditText) {
        AndroidUtils.showKeyboard(mEtSearch, this)
    }

    protected fun showPopupMenu(inflater: () -> PopupMenu) {
        popupMenu = inflater.invoke()
        popupMenu?.show()
    }

    protected fun showSnackbar(text: String, isLong: Boolean = false) = AndroidUtils.showShackbar(
        this,
        binding.root,
        text,
        isLong
    )


    protected fun Int.dpTpPx(): Int = AndroidUtils.dpToPx(this@BaseActivity, this)
}
