package com.aminook.tunemyday.util

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.aminook.tunemyday.worker.NotificationReceiver.Companion.CHANNEL_ID
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.coroutineContext


fun NotificationManager.checkIfNotificationEnabled(): Boolean {
    if (!areNotificationsEnabled()) {
        return false
    }
//        val channels: List<NotificationChannel> = notificationChannels
//        for (channel in channels) {
//            if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
//                return false
//            }
//        }
    if (getNotificationChannel(CHANNEL_ID).importance == NotificationManager.IMPORTANCE_NONE) {
        return false
    }
    return true
}

suspend inline fun <T> Flow<T>.safeCollect(crossinline action: suspend (T) -> Unit) {
    collect{
        coroutineContext.ensureActive()
        action(it)
    }
}

fun NotificationManager.getNotificationSettings(): HashMap<String, Boolean> {
    val res = hashMapOf<String, Boolean>()
    res["Sound"] =
        getNotificationChannel(CHANNEL_ID).importance >= NotificationManager.IMPORTANCE_DEFAULT

    res["Vibrate"]= getNotificationChannel(CHANNEL_ID).vibrationPattern!= null
    return res
}

fun NavController.navigateWithSourcePopUp(from: Int, to: Int) {
    val navOption = NavOptions.Builder()
        .setPopUpTo(from, true)
        .build()
    navigate(to, null, navOption)
}

fun NavController.navigateWithDestinationPopUp(from: Int, to: Int) {
    val navOption = NavOptions.Builder()
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