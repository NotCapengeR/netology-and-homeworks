package ru.netology.nmedia.utils

import java.text.DecimalFormat

private val df = DecimalFormat("###.#")

fun Int.toPostText(): String = when(this) {
    in 0..999 -> this.toString()
    in 1_000..99_999 -> "${df.format(this.toDouble() / 1000.0)} K"
    in 100_000..999_999 -> "${this / 1000} K"
    else -> "${df.format(this.toDouble() / 1_000_000)} M"
}