package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Program(
    var id:Long= 0L,
    val name:String="",
    val color:Int=0
):Parcelable