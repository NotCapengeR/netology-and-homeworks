package ru.netology.nmedia.utils

import android.os.SystemClock
import android.view.View
import java.text.DecimalFormat

private val df = DecimalFormat("###.#")

fun Int.toPostText(): String = when(this) {
    in 0..999 -> this.toString()
    in 1_000..99_999 -> "${df.format(this.toDouble() / 1000.0)}K"
    in 100_000..999_999 -> "${this / 1000}K"
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