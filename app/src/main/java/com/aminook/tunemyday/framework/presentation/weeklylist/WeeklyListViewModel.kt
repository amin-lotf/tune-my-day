package com.aminook.tunemyday.framework.presentation.weeklylist

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.framework.presentation.weeklylist.manager.WeeklyListManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect


class WeeklyListViewModel @ViewModelInject constructor(
    val scheduleInteractors: ScheduleInteractors
) : BaseViewModel() {
    private val TAG = "aminjoon"
    private val activeScope = Dispatchers.IO + viewModelScope.coroutineContext

    private val weeklyListManager=WeeklyListManager()

    private val _daysOfWeek = MutableLiveData<List<Day>>()
    private val _schedules = MutableLiveData<List<Schedule>>()
    var selectedDay: Int? = null

    val daysOfWeek: LiveData<List<Day>>
        get() = _daysOfWeek

    val schedules: LiveData<List<Schedule>>
        get() = weeklyListManager.refinedSchedules



    fun getAllSchedules() {
        CoroutineScope(activeScope).launch {
            scheduleInteractors.getAllSchedules().collect { dataState ->
                processResponse(dataState?.stateMessage)

                dataState?.data?.let { allSchedules ->

                    weeklyListManager.processSchedules(allSchedules)
                }
            }
        }
    }


    fun catchDaysOfWeek() {
        CoroutineScope(activeScope).launch {
            scheduleInteractors.getDaysOfWeek().collect { dataState ->
                Log.d(TAG, "catchDaysOfWeek: ")
                processResponse(dataState?.stateMessage)
                dataState?.data?.let { daysList ->
                    _daysOfWeek.value = daysList
                }
            }
        }
    }
}