package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Program(
    val id:Int?=null,
    val name:String,
    val color:Int
):Parcelable