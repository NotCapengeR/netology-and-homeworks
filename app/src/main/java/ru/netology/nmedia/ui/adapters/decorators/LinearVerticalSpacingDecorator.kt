package ru.netology.nmedia.ui.adapters.decorators

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView

class LinearVerticalSpacingDecoration(@Px private val innerSpacing: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemPosition = parent.getChildAdapterPosition(view)

        outRect.top = if (itemPosition == 0) 0 else innerSpacing / 2
        outRect.bottom = if (itemPosition == state.itemCount - 1) 0 else innerSpacing / 2
    }
}