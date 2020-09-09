package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Schedule(
    var id: Int? = null,
    var startDay: Int = 0,
    var program: Program? = null,
    var duration: String? = null,
    var startTime: Time = Time(),
    var endTime: Time = Time(),
    var hasToDo: Boolean = false,
    var hasAlarm: Boolean = false,
    var alarms: MutableList<Alarm> = mutableListOf()
) : Parcelable {
    val startInSec: Int
        get() = 86400 * startDay + startTime.hour * 60 * 60 + startTime.minute * 60

    val endInSec: Int
        get() = 86400 * endDay + endTime.hour * 60 * 60 + endTime.minute * 60

    val endDay: Int
        get() {
            return if (endTime.hour > startTime.hour || (endTime.hour == startTime.hour && endTime.minute > startTime.minute)) {
                startDay
            } else {
                (startDay + 1) % 7
            }
        }
}