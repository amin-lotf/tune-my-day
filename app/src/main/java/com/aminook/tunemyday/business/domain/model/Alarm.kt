package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Alarm(
    var id: Long = 0L,
    var index: Int = -1,
    var scheduleId:Long= -1L,
    var programId:Long= -1,
    var programName:String="",
    var hourBefore: Int = 0,
    var minuteBefore: Int = 0,
    var day:Int=0,
    var inEditMode:Boolean=false,
    var startInSec:Int= -1
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