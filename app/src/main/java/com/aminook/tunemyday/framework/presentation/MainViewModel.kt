package com.aminook.tunemyday.framework.presentation

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.business.domain.util.DayFactory
import com.aminook.tunemyday.business.interactors.alarm.AlarmInteractors
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_NEW
import com.aminook.tunemyday.util.TodoCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class MainViewModel @ViewModelInject constructor(
    val dateUtil: DayFactory,
    val alarmInteractors: AlarmInteractors,
    val scheduleInteractors: ScheduleInteractors
) : BaseViewModel() {

    private val TAG="aminjoon"
    private val alarmRange = 2
    private val activeScope = Dispatchers.IO + viewModelScope.coroutineContext
    private val _upcomingAlarms = MutableLiveData<List<Alarm>>()

    val upcomingAlarms: LiveData<List<Alarm>>
        get() = _upcomingAlarms

    fun deleteSchedule(schedule:Schedule){
        CoroutineScope(activeScope).launch {
            delay(100)
            scheduleInteractors.deleteSchedule(
                schedule.id,
                object : SnackbarUndoCallback {
                    override fun undo() {
                       saveSchedule(schedule)
                    }

                },
                object : TodoCallback {
                    override fun execute() {
                        Log.d(TAG, "execute: snackbar dismissed")
                    }

                }
            ).collect {dataState->
                processResponse(dataState?.stateMessage)

            }
        }

    }

    private fun saveSchedule(schedule: Schedule) {
        CoroutineScope(activeScope).launch {
            scheduleInteractors.insertSchedule(schedule, listOf(), SCHEDULE_REQUEST_NEW).collect{dataState->
                processResponse(dataState?.stateMessage)

            }
        }
    }


    fun getUpcomingAlarms() {
        CoroutineScope(activeScope).launch {
            val todayIndex = dateUtil.curDayIndex
            alarmInteractors.getUpcomingAlarms(
                startDay = todayIndex,
                endDay = todayIndex + alarmRange
            ).collect { dataState ->
                Log.d(TAG, "catchDaysOfWeek: ")
                processResponse(dataState?.stateMessage)
                dataState?.data?.let { alarms ->
                    _upcomingAlarms.value = alarms

                }
            }
        }
    }
}