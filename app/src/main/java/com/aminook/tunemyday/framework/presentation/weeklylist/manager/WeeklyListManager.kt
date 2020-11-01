package com.aminook.tunemyday.framework.presentation.weeklylist.manager

import android.util.Log
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
            if (index > 0 && tmpList.last().endInSec != schedule.startInSec) {
                val prevSchedule = tmpList.last()
                val tmp = Schedule(
                    id = -1,
                    startDay = schedule.startDay,
                    startTime = prevSchedule.endTime,
                    endTime = schedule.startTime,
                )
                tmpList.add(tmp)
            }
            tmpList.add(schedule)
        }
        tmpList.add(Schedule(id = -2))
        return tmpList
    }

}