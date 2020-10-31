package com.aminook.tunemyday.framework.presentation.addroutine

import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.interactors.routine.DeleteRoutine
import com.aminook.tunemyday.business.interactors.routine.RoutineInteractors
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.util.ROUTINE_INDEX
import com.aminook.tunemyday.util.SCREEN_BLANK
import com.aminook.tunemyday.util.SCREEN_TYPE
import com.aminook.tunemyday.util.SCREEN_WEEKLY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddRoutineViewModel @ViewModelInject constructor(
    val routineInteractors: RoutineInteractors,
    @DataStoreCache dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache, dataStoreSettings) {

    val activeScope = viewModelScope.coroutineContext + Default
    var routineInEditId: Long = 0

    private var _activeRoutineUpdated = MutableLiveData<Boolean>()

    val activeRoutineUpdated: LiveData<Boolean>
        get() = _activeRoutineUpdated


    private suspend fun saveNewCache(routineId: Long) {

        dataStoreCache.edit { cache ->
            cache[ROUTINE_INDEX] = routineId
            cache[SCREEN_TYPE]= if (routineId==0L) SCREEN_BLANK else SCREEN_WEEKLY
            withContext(Main){
                _activeRoutineUpdated.value=true
            }
        }


    }

    fun addRoutine(routineName: String) {

        val routine = RoutineEntity(routineName)
        CoroutineScope(activeScope).launch {
            routineInteractors.insertRoutine(routine, routineId)
                .map {
                    it?.data?.let { routineId ->
                      saveNewCache(routineId)
                    }
                    processResponse(it?.stateMessage)
                }.collect()
        }
    }

    fun updateRoutine(routineName: String) {
        val routine = RoutineEntity(routineName).apply {
            id = routineInEditId
        }
        CoroutineScope(activeScope).launch {
            routineInteractors.updateRoutine(routine).map {
                processResponse(it?.stateMessage)
            }.collect()
        }
    }

    fun deleteRoutine(activeRoutineId: Long) {
        CoroutineScope(activeScope).launch {
            routineInteractors.deleteRoutine(
                routineInEditId,
                activeRoutineId = routineId
            ).map {
                processResponse(it?.stateMessage)

                if (it?.data == DeleteRoutine.ROUTINE_DELETE_SUCCESS) {
                    if(activeRoutineId == routineInEditId) {
                        saveNewCache(0)
                    }else{
                        withContext(Main) {
                            _activeRoutineUpdated.value = true
                        }
                    }
                }
            }.collect()
        }
    }


}