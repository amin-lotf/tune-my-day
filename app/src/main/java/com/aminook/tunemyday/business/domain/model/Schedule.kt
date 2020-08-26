package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Schedule(
    var id: Int? = null,
    var day: Int? = null,
    var program: Program? = null,
    var duration: String? = null,
    var startTime: Time= Time(),
    var endTime: Time=Time(),
    var hasToDo: Boolean = false,
    var hasAlarm: Boolean = false
) : Parcelable