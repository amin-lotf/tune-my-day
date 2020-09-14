package com.aminook.tunemyday.business.domain.model

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DayFactory @Inject constructor(){
    private val TAG="aminjoon"
    fun getDaysOfWeek(chosenDay:Int= -1):List<Day>{
        val calendar=Calendar.getInstance(Locale.US)
        val today=Date()
        calendar.firstDayOfWeek=Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK,calendar.firstDayOfWeek)
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
                    if (chosenDay!= -1) {
                        isChosen = i == chosenDay
                    }else{
                        if (d==today){
                            isChosen=true
                        }
                    }
                }
            )
            calendar.add(Calendar.DATE, 1)
        }
        return days
    }

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
                    date = SimpleDateFormat("MM/dd", Locale.getDefault()).format(d.time),
                    dayIndex = calendar.get(Calendar.DAY_OF_WEEK)
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
            date = SimpleDateFormat("MM/dd", Locale.getDefault()).format(today.time),
            dayIndex = calendar.get(Calendar.DAY_OF_WEEK)
        )
    }

    fun getDay(date: Date):Day {
        val calendar = Calendar.getInstance()
        calendar.time=date
        return Day(
            shortName = SimpleDateFormat("EE", Locale.getDefault()).format(date.time),
            fullName = SimpleDateFormat("EEEE", Locale.getDefault()).format(date.time),
            date = SimpleDateFormat("MM/dd", Locale.getDefault()).format(date.time),
            dayIndex = calendar.get(Calendar.DAY_OF_WEEK)
        )
    }


}