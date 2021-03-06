package ru.netology.nmedia.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.ParseException
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.viewbinding.ViewBinding
import ru.netology.nmedia.R
import timber.log.Timber
import java.util.regex.Pattern
import kotlin.math.roundToInt


object AndroidUtils {
    private val EMAIL_ADDRESS_PATTERN: Pattern = Pattern
        .compile(
            "[a-zA-Z0-9+._%\\-]{1,256}" + "@"
                    + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
                    + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+"
        )

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(mEtSearch: EditText, context: Context) {
        mEtSearch.requestFocus()
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    fun clearEditText(editText: EditText?) {
        if (editText == null) return
        editText.text.clear()
        editText.clearFocus()
    }

    fun dpToPx(context: Context, dp: Int): Int {
        val r: Resources = context.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            r.displayMetrics
        ).roundToInt()
    }

    @JvmStatic
    fun secondsToMin(leftSec: Int): String {
        val min: Int = leftSec / 60
        val sec: Int = leftSec % 60
        var s = if (min >= 10) min.toString() else "0$min"
        s = "$s:"
        s += if (sec >= 10) sec.toString() else "0$sec"
        return s
    }

    fun fromHtml(source: String?): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(source)
        }
    }

    fun validEmail(email: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    fun showToast(context: Context, message: String?, isLong: Boolean = false) {
        if (message == null) return
        if (isLong) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showToast(context: Context, @StringRes msgResId: Int, isLong: Boolean = false) {
        showToast(context, context.getString(msgResId), isLong)
    }

    fun openUrl(context: Context, uri: Uri) = with(context) {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        if (this !is Activity) browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        browserIntent.addCategory(Intent.CATEGORY_BROWSABLE)
        if (browserIntent.resolveActivity(packageManager) != null) {
            startActivity(browserIntent)
        }
    }

    fun openUrl(context: Context, uri: String?) = with(context) {
        if (uri == null) return@with
        openUrl(this, Uri.parse(uri))
    }
}