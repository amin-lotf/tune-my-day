package com.aminook.tunemyday.framework.presentation.weeklylist

import android.os.Bundle
import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.routine.RoutineInteractors
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.framework.presentation.weeklylist.manager.WeeklyListManager
import com.aminook.tunemyday.util.DAY_INDEX
import com.aminook.tunemyday.util.ROUTINE_INDEX
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*


class WeeklyViewModel @ViewModelInject constructor(
    val scheduleInteractors: ScheduleInteractors,
    val dateUtil: DateUtil,
    @DataStoreCache dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache, dataStoreSettings) {
    private val TAG = "aminjoon"
    var fragmentDayIndex: Int = 0
    var fragmentRoutineIndex: Long = 0

    private val weeklyListManager = WeeklyListManager()


    fun getFragmentSchedules(): LiveData<List<Schedule>> {
        return scheduleInteractors.getDailySchedules(
            fragmentDayIndex,
            fragmentRoutineIndex,
            dateUtil.getStartOfDayInSec(fragmentDayIndex)
        )
            .debounce(200)
            .map { dataState ->
                processResponse(dataState?.stateMessage)

                dataState?.data?.let { allSchedules ->
                    weeklyListManager.processSchedules(allSchedules)

                } ?: emptyList()
            }
            .flowOn(Default)
            .asLiveData()
    }


    override fun onCleared() {
        Log.d(TAG, "onCleared: weeklyviewmodel")
        super.onCleared()
    }


}