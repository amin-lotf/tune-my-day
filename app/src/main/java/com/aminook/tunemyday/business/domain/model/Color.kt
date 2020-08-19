package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import androidx.annotation.LayoutRes
import com.aminook.tunemyday.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Color(
    val value:Int,
    var isChosen:Boolean
):Parcelable {

}