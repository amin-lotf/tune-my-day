package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Todo(
    var id: Long=0L,
    var title: String="",
    var scheduleId:Long=0L,
    var programId:Long=0L,
    var isDone: Boolean=false,
    var dateAdded:Int,
    var priorityIndex: Int=0,
    val subTodos:MutableList<SubTodo> = mutableListOf(),
    var isOneTime:Boolean=false,
    var lastChecked:Int=0
) : Parcelable

@Parcelize
data class SubTodo(
    var id: Long,
    var title: String,
    var isDone: Boolean,
    var dateAdded:Int,
    var todoId:Long
):Parcelable