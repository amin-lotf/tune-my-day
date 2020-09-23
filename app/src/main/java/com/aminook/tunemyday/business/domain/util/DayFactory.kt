package com.aminook.tunemyday.business.domain.util

import com.aminook.tunemyday.business.domain.model.Day
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class DayFactory @Inject constructor() {
    private val TAG = "aminjoon"


    val curDayIndex: Int
        get() {
            val calendar = Calendar.getInstance()
            var index = calendar.get(Calendar.DAY_OF_WEEK) - 2
            if (index == -1) {
                index = 6
            }
            return index
        }

    val curTimeInSec: Int
        get() {
            val today = Date()
            val hour=SimpleDateFormat("HH", Locale.US).format(today.time).toInt()
            val minute=SimpleDateFormat("mm", Locale.US).format(today.time).toInt()
            val second=SimpleDateFormat("ss", Locale.US).format(today.time).toInt()
           return 86400 * curDayIndex + hour * 60 * 60 + minute * 60+second
        }

    val curTimeInMillis: Int
        get() {
            val today = Date()
            val hour=SimpleDateFormat("HH", Locale.US).format(today.time).toInt()
            val minute=SimpleDateFormat("mm", Locale.US).format(today.time).toInt()
            val second=SimpleDateFormat("ss", Locale.US).format(today.time).toInt()
            val mills=SimpleDateFormat("SSS", Locale.US).format(today.time).toInt()
            return (86400 * curDayIndex + hour * 60 * 60 + minute * 60+second)*1000+mills
        }

    fun getDaysOfWeek(chosenDay: Int = curDayIndex): List<Day> {
        val calendar = Calendar.getInstance(Locale.US)
        val today = Date()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val days = mutableListOf<Day>()
        for (i in 0..6) {
            val d = calendar.time

            days.add(
                Day(
                    shortName = SimpleDateFormat("EE", Locale.US).format(d.time),
                    fullName = SimpleDateFormat("EEEE", Locale.US).format(d.time),
                    date = SimpleDateFormat("MM/dd", Locale.US).format(d.time),
                    dayIndex = i
                ).apply {
                    if (chosenDay != -1) {
                        isChosen = i == chosenDay
                    } else {
                        if (d == today) {
                            isChosen = true
                        }
                    }
                }
            )
            calendar.add(Calendar.DATE, 1)
        }
        return days
    }

    fun getNextNDays(date: Date, n: Int = 7): List<Day> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val days = mutableListOf<Day>()

        for (i in 1..n) {
            val d = calendar.time

            days.add(
                Day(
                    shortName = SimpleDateFormat("EE", Locale.getDefault()).format(d.time),
                    fullName = SimpleDateFormat("EEEE", Locale.getDefault()).format(d.time),
                    date = SimpleDateFormat("MM/dd", Locale.getDefault()).format(d.time),
                    dayIndex = calendar.get(Calendar.DAY_OF_WEEK)
                )
            )
            calendar.add(Calendar.DATE, 1)
        }
        return days
    }

    fun getCurrentDay(): Day {
        val calendar = Calendar.getInstance()
        val today = calendar.time

        return Day(
            shortName = SimpleDateFormat("EE", Locale.getDefault()).format(today.time),
            fullName = SimpleDateFormat("EEEE", Locale.getDefault()).format(today.time),
            date = SimpleDateFormat("MM/dd", Locale.getDefault()).format(today.time),
            dayIndex = calendar.get(Calendar.DAY_OF_WEEK)
        )
    }

    fun getDay(date: Date): Day {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return Day(
            shortName = SimpleDateFormat("EE", Locale.getDefault()).format(date.time),
            fullName = SimpleDateFormat("EEEE", Locale.getDefault()).format(date.time),
            date = SimpleDateFormat("MM/dd", Locale.getDefault()).format(date.time),
            dayIndex = calendar.get(Calendar.DAY_OF_WEEK)
        )
    }

    fun getTimeDifferenceInMills(day:Int,startInSec:Int):Long{
        return if(day==curDayIndex || startInSec>curTimeInSec){
            (startInSec)*1000L-curTimeInMillis
        }else{
            (SECS_IN_WEEK+startInSec)*1000L-curTimeInMillis
        }
    }

    companion object{
        val SECS_IN_DAY=86400
        val SECS_IN_WEEK=604800
    }


}