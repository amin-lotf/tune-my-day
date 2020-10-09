package com.aminook.tunemyday.framework.presentation.weeklylist

import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.framework.presentation.weeklylist.manager.WeeklyListManager
import com.aminook.tunemyday.util.DAY_INDEX
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class WeeklyListViewModel @ViewModelInject constructor(
    val scheduleInteractors: ScheduleInteractors,
    val dataStore: DataStore<Preferences>,
    val dateUtil: DateUtil,
) : BaseViewModel() {
    private val TAG = "aminjoon"
    private val activeScope = Dispatchers.IO + viewModelScope.coroutineContext
    var savedDayIndex:Int=0
    var isFirstLoad=true




    private val weeklyListManager=WeeklyListManager()



    val schedules: LiveData<List<Schedule>>
        get() = weeklyListManager.refinedSchedules


    fun getSavedDayIndex():LiveData<Int>{
       return dataStore.data
            .flowOn(IO)
            .map {
                    savedDayIndex=it[DAY_INDEX]?:dateUtil.curDayIndex
                    savedDayIndex
            }
            .asLiveData()
    }

    fun setSavedDayIndex(){
        CoroutineScope(IO).launch {
            dataStore.edit { settings->
                settings[DAY_INDEX]=savedDayIndex
            }
        }
    }

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

}