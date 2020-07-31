package com.aminook.tunemyday.business.domain.model

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DayFactory @Inject constructor(){

    fun getNextSevenDays(date: Date): List<Day> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val days = mutableListOf<Day>()

        for (i in 1..7) {
            val d = calendar.time
            days.add(
                Day(
                    shortName = SimpleDateFormat("EE", Locale.getDefault()).format(d.time),
                    fullName = SimpleDateFormat("EEEE", Locale.getDefault()).format(d.time),
                    date = SimpleDateFormat("MM/dd", Locale.getDefault()).format(d.time)
                )
            )
            calendar.add(Calendar.DATE, 1)
        }
        return days
    }

    fun getCurrentDay():Day {
        val calendar = Calendar.getInstance()
        val today=calendar.time

        return Day(
            shortName = SimpleDateFormat("EE", Locale.getDefault()).format(today.time),
            fullName = SimpleDateFormat("EEEE", Locale.getDefault()).format(today.time),
            date = SimpleDateFormat("MM/dd", Locale.getDefault()).format(today.time)
        )
    }

    fun getDay(date: Date):Day {

        return Day(
            shortName = SimpleDateFormat("EE", Locale.getDefault()).format(date.time),
            fullName = SimpleDateFormat("EEEE", Locale.getDefault()).format(date.time),
            date = SimpleDateFormat("MM/dd", Locale.getDefault()).format(date.time)
        )
    }


}