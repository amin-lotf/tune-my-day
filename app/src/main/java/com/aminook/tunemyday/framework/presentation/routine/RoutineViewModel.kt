package com.aminook.tunemyday.framework.presentation.routine

import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.interactors.alarm.AlarmInteractors
import com.aminook.tunemyday.business.interactors.routine.RoutineInteractors
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.framework.presentation.routine.manager.RoutineManager
import com.aminook.tunemyday.util.ROUTINE_INDEX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RoutineViewModel @ViewModelInject constructor(
    val routineInteractors: RoutineInteractors,
    val alarmInteractors: AlarmInteractors,
    @DataStoreCache  dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache,dataStoreSettings) {

    private val activeScope = Dispatchers.IO + viewModelScope.coroutineContext
    private val _routineLoaded=MutableLiveData<Boolean>()

    private val routineManager=RoutineManager()

    val routineLoaded:LiveData<Boolean>
    get() = _routineLoaded

    fun getRoutines(): LiveData<List<RoutineEntity>> {
        return routineInteractors.getAllRoutine()
            .map {
                processResponse(it?.stateMessage)
                routineManager.processRoutines(it?.data ?: emptyList())
            }
            .flowOn(Default)
            .asLiveData()
    }

    fun updateRoutine(routineEntity: RoutineEntity) {

        CoroutineScope(activeScope).launch {
            routineInteractors.updateRoutine(routineEntity).map {
                processResponse(it?.stateMessage)
            }.collect()
        }
    }



    fun deleteRoutine(routineEntity: RoutineEntity) {
        CoroutineScope(activeScope).launch {
            routineInteractors.deleteRoutine(routineEntity,routineId).map {
                processResponse(it?.stateMessage)
            }.collect()
        }
    }

    fun addRoutine(routineName: String) {
        val routine = RoutineEntity(routineName)
        CoroutineScope(activeScope).launch {
            routineInteractors.insertRoutine(routine,routineId)
                .map {
                    processResponse(it?.stateMessage)
                    it?.data?.let { routineId ->
                        saveRoutineIndex(routineId)
                    }
                }.collect()
        }
    }

    fun saveRoutineIndex(routineId: Long) {
        _routineLoaded.value=false
        CoroutineScope(activeScope).launch {
            dataStoreCache.edit { cache ->
                cache[ROUTINE_INDEX] = routineId
                withContext(Main) {
                    _routineLoaded.value = true
                }
            }

        }
    }


}