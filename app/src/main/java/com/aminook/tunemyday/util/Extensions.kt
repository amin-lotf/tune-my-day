package com.aminook.tunemyday.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

fun <T> RecyclerView.Adapter<*>.autoNotify(oldList:List<T>,newList:List<T>,compare:(T,T)->Boolean){
    val diff=DiffUtil.calculateDiff(object :DiffUtil.Callback(){
        override fun getOldListSize()=oldList.size

        override fun getNewListSize()=newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
           return compare(oldList[oldItemPosition],newList[newItemPosition])
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition]==newList[newItemPosition]
        }

    })

    diff.dispatchUpdatesTo(this)
}

fun Activity.hideKeyboard() {
    val inputMethodManager =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // Check if no view has focus
    val currentFocusedView = currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, 0)
}