package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Schedule(
    var id: Long = 0,
    var routineId:Long=0,
    var startDay: Int = 0,
    var program: Program = Program(),
    var startTime: Time = Time(),
    var endTime: Time = Time(),
    var hasToDo: Boolean = false,
    var hasAlarm: Boolean = false,
    var alarms: MutableList<Alarm> = mutableListOf(),
    var unfinishedTodos:MutableList<Todo> = mutableListOf(),
    var finishedTodos:MutableList<Todo> = mutableListOf()
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

    val duration: String
    get() {
        val startSec=startTime.hour*3600+startTime.minute*60
        val endSec=(endTime.hour+ (if (endDay==startDay) 0 else 24))*3600+endTime.minute*60

        val totDuration=endSec-startSec
        val durationH=totDuration/3600
        val durationM=(totDuration-durationH*3600)/60
        return (if (durationH==0) "" else "${durationH}h")+(if (durationM==0) "" else " ${durationM}min")
    }


}