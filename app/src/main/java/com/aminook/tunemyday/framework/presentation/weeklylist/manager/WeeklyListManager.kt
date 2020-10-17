package com.aminook.tunemyday.framework.presentation.weeklylist.manager

import android.view.ScaleGestureDetector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aminook.tunemyday.business.domain.model.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeeklyListManager {

    fun processSchedules(rawSchedules: List<Schedule>): List<Schedule> {
        val tmpList = mutableListOf<Schedule>()
        rawSchedules.forEachIndexed { index, schedule ->
            if(index>0 && tmpList[index-1].endInSec!=schedule.startInSec){
                val prevSchedule=tmpList[index-1]
                tmpList.add(Schedule(
                    id = -1,
                    startDay = schedule.startDay,
                    startTime =prevSchedule.startTime,
                endTime = schedule.startTime,
                ))
            }
            tmpList.add(schedule)

//            if (!rawSchedules.any { it.startInSec == schedule.endInSec } && rawSchedules.any { it.startDay == schedule.startDay && it.startInSec > schedule.startInSec }) {
//                tmpList.add(Schedule(id = -1, startDay = schedule.startDay))
//            }
        }
        return tmpList
    }

}