package com.aminook.tunemyday.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.framework.presentation.common.TodoAdapter

class DragManageAdapter<T>(adapter: T, val context: Context, dragDirs: Int, swipeDirs: Int)
    : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) where T:ItemMoveCallback
{
    var nameAdapter = adapter

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        //recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition,target.adapterPosition)

        return true
    }

    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {

        nameAdapter.onItemSwap(viewHolder.adapterPosition,target.adapterPosition)
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)

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
//        if (dY==0f) {
//            val itemView = viewHolder.itemView
//            val icon = ContextCompat.getDrawable(
//                viewHolder.itemView.context,
//                R.drawable.ic_delete
//            )
//            val background = ColorDrawable(ContextCompat.getColor(context, R.color.colorDelete))
//
//            val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
//            val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
//            val iconBottom = iconTop + icon.intrinsicHeight
//
//            val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
//            val iconRight = itemView.right - iconMargin
//
//            icon.setBounds(
//                iconLeft,
//                iconTop,
//                iconRight,
//                iconBottom
//            )
//
//            background.setBounds(
//                (itemView.right + dX).toInt(),
//                itemView.top,
//                itemView.right,
//                itemView.bottom
//            )
//
//            if (dX.toInt() == 0) { // view is unSwiped
//                background.setBounds(0, 0, 0, 0)
//            }
//
//            background.draw(c)
//
//            if (-dX > (icon.intrinsicWidth + iconMargin)) // Draw icon only on full visibility
//                icon.draw(c)
//        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}