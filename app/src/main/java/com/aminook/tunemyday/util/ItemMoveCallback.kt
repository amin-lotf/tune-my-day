package com.aminook.tunemyday.util

import com.aminook.tunemyday.business.domain.model.Todo

interface ItemMoveCallback {
    fun onItemSwap(fromPosition: Int, toPosition: Int)
    fun onItemSwipe(itemPosition:Int,direction:Int)

}