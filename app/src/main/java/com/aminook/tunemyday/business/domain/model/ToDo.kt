package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class ToDo(
    var id: Long=0L,
    var title: String="",
    var isDone: Boolean=false,
    var priorityIndex: Int=0,
    val subTodos:MutableList<SubToDo> = mutableListOf(),
    var isOneTime:Boolean=false,
    var lastChecked:Int=0 //YYMMddhhmmss
) : Parcelable

@Parcelize
data class SubToDo(
    val id: Long,
    val title: String,
    val isDone: Boolean,
    val dateAdded:Int //YYMMddhhmmss
):Parcelable