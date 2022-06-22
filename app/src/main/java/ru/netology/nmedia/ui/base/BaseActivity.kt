package ru.netology.nmedia.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), FragmentObserver {

    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater) -> VB
    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = requireNotNull(_binding) as VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater).also { setContentView(it.root) }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStartFragment() {}

    override fun onStopFragment() {}

    fun showToast(message: String?, isLong: Boolean = false) {
        if (message == null) return
        if (isLong) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showToast(@StringRes msgResId: Int, isLong: Boolean = false) {
        showToast(getString(msgResId), isLong)
    }
}

interface FragmentObserver {

    fun onStartFragment()

    fun onStopFragment()
}
