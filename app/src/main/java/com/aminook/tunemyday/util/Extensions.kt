package com.aminook.tunemyday.util

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView



fun NavController.navigateWithSourcePopUp(from: Int, to: Int){
    val navOption=NavOptions.Builder()
        .setPopUpTo(from, true)
        .build()
    navigate(to, null, navOption)
}

fun NavController.navigateWithDestinationPopUp(from: Int, to: Int){
    val navOption=NavOptions.Builder()
        .setPopUpTo(to, true)
        .build()
    navigate(to, null, navOption)
}


fun Activity.hideKeyboard() {
    val inputMethodManager =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // Check if no view has focus
    val currentFocusedView = currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, 0)
}