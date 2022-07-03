package ru.netology.nmedia.utils

import android.content.Context
import android.os.SystemClock
import android.view.View
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import ru.netology.nmedia.App
import ru.netology.nmedia.di.AppComponent
import timber.log.Timber
import java.lang.reflect.Type
import java.text.DecimalFormat
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

private val DECIMAL_FORMAT = DecimalFormat("###.#")

fun Int.toPostText(): String = when (this) {
    in 0..999 -> this.toString()
    in 1_000..10_000 -> "${DECIMAL_FORMAT.format(this.toDouble() / 1000.0)}K"
    in 10_001..999_999 -> "${this / 1000}K"
    else -> "${DECIMAL_FORMAT.format(this.toDouble() / 1_000_000)}M"
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

fun Throwable.getErrorMessage(): String = this.message ?: this.toString()

fun Throwable.multiCatch(
    vararg exceptions: KClass<out Throwable> = emptyArray(),
    catchBlock: (Throwable) -> Unit,
) {
    if (exceptions.isEmpty()) throw this
    return when {
        this::class in exceptions -> catchBlock.invoke(this)
        exceptions.any { error -> this::class.isSubclassOf(error) } -> catchBlock.invoke(this)
        else -> throw this
    }

}

fun <T> Throwable.multiCatch(
    vararg exceptions: KClass<out Throwable> = emptyArray(),
    catchBlock: (Throwable) -> T,
): T {
    if (exceptions.isEmpty()) throw this
    return when {
        this::class in exceptions -> catchBlock.invoke(this)
        exceptions.any { error -> this::class.isSubclassOf(error) } -> catchBlock.invoke(this)
        else -> throw this
    }
}

fun <T> Gson.fromJsonOrNull(json: String?, classOfT: Class<T>): T? {
    return try {
        fromJson(json, classOfT)
    } catch (ex: JsonSyntaxException) {
        Timber.e("Error occurred while parsing JSON: ${ex.getErrorMessage()}")
        null
    }
}

fun <T> Gson.fromJsonOrNull(json: String?, typeOf: Type): T? {
    return try {
        fromJson(json, typeOf)
    } catch (ex: JsonSyntaxException) {
        Timber.e("Error occurred while parsing JSON: ${ex.getErrorMessage()}")
        null
    }
}