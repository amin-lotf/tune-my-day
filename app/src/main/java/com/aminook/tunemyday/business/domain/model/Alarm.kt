package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Alarm(
    var id: Int = 0,
    var index: Int = -1,
    var scheduleId:Int= -1,
    var programId:Long= -1,
    var hourBefore: Int = 0,
    var minuteBefore: Int = 0,
    var day:Int=0,
    var inEditMode:Boolean=false
) : Parcelable {

    val isAtStart:Boolean
    get() = (hourBefore == 0 && minuteBefore == 0)

    override fun toString(): String {
        val h = if (hourBefore == 1) " hour" else " hours "
        val m = if (minuteBefore == 1) " minute" else " minutes"
        return if (hourBefore == 0 && minuteBefore == 0) {
            "Remind me at the start of the activity"
        } else if (hourBefore == 0) {
            minuteBefore.toString() + m +
                    " before activity starts"
        }else if(minuteBefore==0){
            hourBefore.toString() + h +
                    " before activity starts"
        } else {

            hourBefore.toString() + h +
                    minuteBefore.toString() + m +
                    " before activity starts"
        }
    }
}