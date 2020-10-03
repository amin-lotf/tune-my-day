package com.aminook.tunemyday.util

import android.content.Context
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class DragManageAdapter<T>(adapter: T, context: Context, dragDirs: Int, swipeDirs: Int)
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

//    override fun getMovementFlags(
//        recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder
//    ): Int {
//        val dragFlags=ItemTouchHelper.DOWN or ItemTouchHelper.UP
//        return makeMovementFlags(dragFlags,0)
//    }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        nameAdapter.onItemSwipe(viewHolder.adapterPosition,direction)
    }

}