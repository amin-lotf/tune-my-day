package com.aminook.tunemyday.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R

class DragManageAdapter<T>(adapter: T, val context: Context, dragDirs: Int, swipeDirs: Int)
    : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) where T:ItemMoveCallback
{
    var nameAdapter = adapter

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        nameAdapter.onItemSwap(viewHolder.adapterPosition,target.adapterPosition)
        return true
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        nameAdapter.onItemSwipe(viewHolder.adapterPosition,direction)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (dY==0f) {
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top

            val background = ColorDrawable(Color.RED)
            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)

            val icon = ContextCompat.getDrawable(context, R.drawable.ic_delete)

            val inWidth = icon?.intrinsicWidth ?: 0
            val inHeight = icon?.intrinsicHeight ?: 0
            // Calculate position of delete icon
            val iconTop = itemView.top + (itemHeight - inHeight) / 2
            val iconMargin = (itemHeight - inHeight) / 2
            val iconLeft = itemView.right - iconMargin - inWidth
            val iconRight = itemView.right - iconMargin
            val iconBottom = iconTop + inHeight

            // Draw the delete icon

            icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            icon?.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}