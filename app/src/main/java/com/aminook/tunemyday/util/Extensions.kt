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


fun <T> RecyclerView.Adapter<*>.autoNotify(
    oldList: List<T>,
    newList: List<T>,
    compare: (T, T) -> Boolean
){
    val diff=DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return compare(oldList[oldItemPosition], newList[newItemPosition])
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

    })

    diff.dispatchUpdatesTo(this)
}

fun NestedScrollView.setTransition(){
    val tr=LayoutTransition()
    tr.enableTransitionType(LayoutTransition.CHANGING)
    tr.setStartDelay(LayoutTransition.CHANGING, 3000)
    layoutTransition=tr
//    this.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
//    this.layoutTransition.setDuration(300)
//    this.layoutTransition.setStartDelay()
}

fun ConstraintLayout.setTransition(){
    val tr=LayoutTransition()
    tr.enableTransitionType(LayoutTransition.CHANGING)
    tr.setStartDelay(LayoutTransition.CHANGING, 3000)
    layoutTransition=tr
//    this.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
//    this.layoutTransition.setDuration(300)
//    this.layoutTransition.setStartDelay()
}

// fun setHeightChangeAnimation(animatedLayout: ViewGroup) {
//
//        var vg = animatedLayout
//        while (vg is ViewGroup) {
//            vg = vg.parent as ViewGroup
//            if (vg is ViewGroup && vg.layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
//                val animatedLayoutLt = animatedLayout.layoutTransition
//                val lt = LayoutTransition()
//                lt.enableTransitionType(LayoutTransition.CHANGING)
//                lt.setDuration(animatedLayoutLt.getDuration(LayoutTransition.CHANGE_APPEARING))
//                lt.setStartDelay(
//                    LayoutTransition.CHANGING,
//                    animatedLayoutLt.getStartDelay(LayoutTransition.APPEARING)
//                )
//                val finalVg = vg
//                val oldLt = finalVg.layoutTransition
//                lt.addTransitionListener(object : LayoutTransition.TransitionListener {
//                    override fun startTransition(
//                        transition: LayoutTransition,
//                        container: ViewGroup,
//                        view: View,
//                        transitionType: Int
//                    ) {
//                    }
//
//                    override fun endTransition(
//                        transition: LayoutTransition,
//                        container: ViewGroup,
//                        view: View,
//                        transitionType: Int
//                    ) {
//                        finalVg.layoutTransition = oldLt
//                    }
//                })
//                finalVg.layoutTransition = lt
//                break
//            }
//
//    }
//}


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