package com.aminook.tunemyday.framework.presentation.dailylist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DailyViewModel @ViewModelInject constructor(
    val dateUtil:DateUtil,
    val scheduleInteractors: ScheduleInteractors
):BaseViewModel() {

    val activeScope= Dispatchers.IO + viewModelScope.coroutineContext

    private val _schedules=MutableLiveData<List<Schedule>>()

    val schedules:LiveData<List<Schedule>>
    get() = _schedules

    fun getDailySchedules(fragmentIndex:Int){
        var dayIndex=dateUtil.curDayIndex+fragmentIndex
        dayIndex= if(dayIndex>6) dayIndex-7 else dayIndex
        CoroutineScope(activeScope).launch {
            scheduleInteractors.getDailySchedules(dayIndex).collect {dataState->
                processResponse(dataState?.stateMessage)

                dataState?.data?.let {schedules->
                    _schedules.value=schedules
                }
            }
        }
    }

    fun addTodo(scheduleId: Long, task: String) {

    }
}