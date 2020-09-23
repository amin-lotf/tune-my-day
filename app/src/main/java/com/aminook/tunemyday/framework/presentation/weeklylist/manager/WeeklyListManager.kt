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
    private val _refinedSchedules = MutableLiveData<List<Schedule>>()

    val refinedSchedules: LiveData<List<Schedule>>
        get() = _refinedSchedules


    fun processSchedules(rawSchedules:List<Schedule>){
        //CoroutineScope(Default).launch{
            val tmpList= mutableListOf<Schedule>()

            rawSchedules.forEachIndexed { _, schedule ->
                tmpList.add(schedule)

                if(!rawSchedules.any { it.startInSec==schedule.endInSec} && rawSchedules.any { it.startDay==schedule.startDay && it.startInSec>schedule.startInSec }){
                    tmpList.add(Schedule(id = -1,startDay = schedule.startDay))
                }
            }
//                withContext(Main) {
                    _refinedSchedules.value = tmpList
//                }

        }

    //}

}