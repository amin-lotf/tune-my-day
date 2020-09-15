package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Program(
    val id:Int= -1,
    val name:String,
    val color:Int
):Parcelable