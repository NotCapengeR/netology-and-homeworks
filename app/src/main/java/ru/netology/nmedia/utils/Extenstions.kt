package ru.netology.nmedia.utils

import android.app.Dialog
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.google.gson.*
import ru.netology.nmedia.App
import ru.netology.nmedia.di.AppComponent
import timber.log.Timber
import java.io.Reader
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

fun Boolean.toByte(): Byte = if (this) 1 else 0

fun Boolean.toShort(): Short = if (this) 1 else 0

fun Boolean.toInt(): Int = if (this) 1 else 0

fun Boolean.toLong(): Long = if (this) 1L else 0L

fun Boolean.toFloat(): Float = if (this) 1.0F else 0.0F

fun Boolean.toDouble(): Double = if (this) 1.0 else 0.0

fun Boolean.toChar(): Char = if (this) '1' else '0'

fun View.clickWithDebounce(debounceTime: Long = 600L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action.invoke()

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

fun View.showPopupMenu(inflater: (View) -> PopupMenu) = inflater.invoke(this).show()

fun View.showDialog(inflater: (View) -> Dialog) = inflater.invoke(this).show()

fun Context.showDialog(inflater: (Context) -> Dialog) = inflater.invoke(this).show()

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
    (requireContext().applicationContext as App).appComponent

fun Context.getAppComponent(): AppComponent = when (this) {
    is App -> appComponent
    else -> (applicationContext as App).appComponent
}

fun String.checkIfNotEmpty(): Boolean = this.trim().isNotEmpty()

fun Throwable.getErrorMessage(): String = this.message ?: this.toString()

inline fun <T, E : Throwable> E.multiCatch(
    vararg exceptions: KClass<out Throwable>,
    catchBlock: (E) -> T,
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

fun <T> Gson.fromJsonOrNull(json: Reader, classOfT: Class<T>): T? {
    return try {
        fromJson(json, classOfT)
    } catch (ex: JsonParseException) {
        return ex.multiCatch(JsonIOException::class, JsonSyntaxException::class) {
            Timber.e("Error occurred while parsing JSON: ${it.getErrorMessage()}")
            null
        }
    }
}

fun <T> Gson.fromJsonOrNull(json: JsonElement, classOfT: Class<T>): T? {
    return try {
        fromJson(json, classOfT)
    } catch (ex: JsonSyntaxException) {
        Timber.e("Error occurred while parsing JSON: ${ex.getErrorMessage()}")
        null
    }
}

fun <T> Gson.fromJsonOrNull(json: JsonElement, typeOf: Type): T? {
    return try {
        fromJson(json, typeOf)
    } catch (ex: JsonSyntaxException) {
        Timber.e("Error occurred while parsing JSON: ${ex.getErrorMessage()}")
        null
    }
}