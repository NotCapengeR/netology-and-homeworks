package ru.netology.nmedia.utils

import android.content.Context
import android.os.SystemClock
import android.view.View
import androidx.fragment.app.Fragment
import ru.netology.nmedia.App
import ru.netology.nmedia.di.AppComponent
import ru.netology.nmedia.database.dto.Post
import java.text.DecimalFormat
import java.text.SimpleDateFormat

private val df = DecimalFormat("###.#")

fun Int.toPostText(): String = when (this) {
    in 0..999 -> this.toString()
    in 1_000..10_000 -> "${df.format(this.toDouble() / 1000.0)}K"
    in 10_001..999_999 -> "${this / 1000}K"
    else -> "${df.format(this.toDouble() / 1_000_000)}M"
}

fun View.clickWithDebounce(debounceTime: Long = 600L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action()

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

fun View.setDebouncedListener(debounceTime: Long = 600L, onClickListener: View.OnClickListener) {
    var lastClickTime: Long = 0
    val clickWithDebounce: (view: View) -> Unit = {

        if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) { /* do nothing */
        } else onClickListener.onClick(it)
    }
    lastClickTime = SystemClock.elapsedRealtime()
    this.setOnClickListener(clickWithDebounce)
}

fun View.setVisibility(visible: Boolean?) = when (visible) {
    true -> this.visibility = View.VISIBLE
    false -> this.visibility = View.GONE
    null -> this.visibility = View.INVISIBLE
}


fun Fragment.getAppComponent(): AppComponent =
    (this.requireContext().applicationContext as App).appComponent

fun Context.getAppComponent(): AppComponent = when (this) {
    is App -> appComponent
    else -> (this.applicationContext as App).appComponent
}

fun String.checkIfNotEmpty(): Boolean = this.trim().isNotEmpty()

fun String.toDateTime(): Long? {
    val formatter = SimpleDateFormat(Post.POST_DATE_ABSOLUTE)
    return formatter.parse(this)?.time
}

fun Boolean.toSQL(): String = when (this) {
    true -> "TRUE"
    false -> "FALSE"
}

fun String.toSQLBoolean(): Boolean? = when (this.uppercase().trim()) {
    "TRUE" -> true
    "FALSE" -> false
    else -> null
}