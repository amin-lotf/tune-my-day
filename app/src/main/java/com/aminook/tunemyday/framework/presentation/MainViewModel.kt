package com.aminook.tunemyday.framework.presentation

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.business.domain.util.DayFactory
import com.aminook.tunemyday.business.interactors.alarm.AlarmInteractors
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class MainViewModel @ViewModelInject constructor(
    val dateUtil: DayFactory,
    val alarmInteractors: AlarmInteractors
) : BaseViewModel() {

    private val TAG="aminjoon"
    private val alarmRange = 2
    private val activeScope = Dispatchers.IO + viewModelScope.coroutineContext
    private val _upcomingAlarms = MutableLiveData<List<Alarm>>()

    val upcomingAlarms: LiveData<List<Alarm>>
        get() = _upcomingAlarms

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