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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single


class WeeklyListViewModel @ViewModelInject constructor(
    val scheduleInteractors: ScheduleInteractors,
    val routineInteractors: RoutineInteractors,
    @DataStoreSettings val dataStoreSettings: DataStore<Preferences>,
    @DataStoreCache val dataStoreCache: DataStore<Preferences>,
    val dateUtil: DateUtil,
) : BaseViewModel() {
    private val TAG = "aminjoon"
    private val activeScope = Dispatchers.IO + viewModelScope.coroutineContext
    var savedDayIndex: Int = 0
    var curRoutineIndex=0

    private val _curRoutine = MutableLiveData<RoutineEntity?>()
//    private val _curRoutineId = MutableLiveData<Long>()
    private val _dayIndex = MutableLiveData<Int>()


    val routine: LiveData<RoutineEntity?>
        get() = _curRoutine
//
//    val routineId: LiveData<Long>
//        get() = _curRoutineId

    val curDayIndex: LiveData<Int>
        get() = _dayIndex

    fun saveRoutineIndex(routineId: Long) {
        CoroutineScope(activeScope).launch {
            dataStoreCache.edit { cache ->
                cache[ROUTINE_INDEX] = routineId
            }
        }
    }

    fun getRoutineIndex(): LiveData<Long> {
        return dataStoreCache.data
            .map {
                Log.d(TAG, "getRoutineId: viewmodl")
                it[ROUTINE_INDEX] ?: 0

            }.asLiveData()
    }

    fun getDayIndex(): LiveData<Int> {
        return dataStoreSettings.data
            .flowOn(IO)
            .map {
                it[DAY_INDEX] ?: dateUtil.curDayIndex
            }
            .asLiveData()
    }

    fun saveDayIndex(index:Int?) {
        Log.d(TAG, "saveDayIndex: dayIndex saved: $index")
        CoroutineScope(IO).launch {
            dataStoreSettings.edit { settings ->
                settings[DAY_INDEX] = index?:dateUtil.curDayIndex
            }
        }
    }

    fun getRoutine(routineId: Long):LiveData<RoutineEntity?> {
               return routineInteractors.getRoutine(routineId)
                    .map {
                         it?.data
                    }.asLiveData()
        }


    fun addRoutine(routineName: String) {

        val routine = RoutineEntity(routineName)
        CoroutineScope(activeScope).launch {
            routineInteractors.insertRoutine(routine)
                .map {
                    processResponse(it?.stateMessage)
                    it?.data?.let { routineId ->
                        getRoutine(routineId)

                        saveRoutineIndex(routineId)
                    }
                }.collect()
        }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: weeklyList ViewModel")
        super.onCleared()
    }
    fun bufferDayIndex(dayIndex: Int) {
        savedDayIndex = dayIndex
        _dayIndex.value = dayIndex
    }


}