package ru.netology.nmedia.ui.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.netology.nmedia.utils.AndroidUtils

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    protected val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    protected fun Int.dpToPx(): Int = AndroidUtils.dpToPx(itemView.context, this)
}