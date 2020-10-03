package com.aminook.tunemyday.util

interface ItemMoveCallback {
    fun onItemSwap(fromPosition: Int, toPosition: Int)
    fun onItemSwipe(itemPosition:Int,direction:Int)
}