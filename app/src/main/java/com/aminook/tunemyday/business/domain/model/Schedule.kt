package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Schedule(
    var id: Int?=null,
    var day: Int?=null,
    var program: Program?=null,
    var duration: String?=null,
    var start: String?=null,
    var end: String?=null,
    var hasToDo: Boolean=false,
    var hasAlarm: Boolean=false
) : Parcelable {


//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//
//        if (this.javaClass != other?.javaClass) return false
//
//        other as Schedule
//
//        return this.id == other.id &&
//                this.day == other.day &&
//                this.program == other.program &&
//                this.duration == other.duration &&
//                this.start == other.start &&
//                this.end == other.end &&
//                this.hasToDo == other.hasToDo &&
//                this.hasAlarm == other.hasAlarm
//
//    }
//
//    override fun hashCode(): Int {
//        var result=id.hashCode()
//        result=31*result+day.hashCode()
//        result=31*result+program.hashCode()
//        result=31*result+duration.hashCode()
//        result=31*result+start.hashCode()
//        result=31*result+end.hashCode()
//        result=31*result+hasToDo.hashCode()
//        result=31*result+hasAlarm.hashCode()
//        return result
//    }

}