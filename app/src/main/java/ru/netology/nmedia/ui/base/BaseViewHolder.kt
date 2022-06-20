package ru.netology.nmedia.ui.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    protected val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
}