package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Schedule(
    val id: Int,
    val day: Int,
    val program: String,
    val duration: String,
    val start: String,
    val end: String,
    val hasToDo: Boolean,
    val hasAlarm: Boolean
) : Parcelable {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (this.javaClass != other?.javaClass) return false

        other as Schedule

        return this.id == other.id &&
                this.day == other.day &&
                this.program == other.program &&
                this.duration == other.duration &&
                this.start == other.start &&
                this.end == other.end &&
                this.hasToDo == other.hasToDo &&
                this.hasAlarm == other.hasAlarm

    }

    override fun hashCode(): Int {
        var result=id.hashCode()
        result=31*result+day.hashCode()
        result=31*result+program.hashCode()
        result=31*result+duration.hashCode()
        result=31*result+start.hashCode()
        result=31*result+end.hashCode()
        result=31*result+hasToDo.hashCode()
        result=31*result+hasAlarm.hashCode()
        return result
    }

}