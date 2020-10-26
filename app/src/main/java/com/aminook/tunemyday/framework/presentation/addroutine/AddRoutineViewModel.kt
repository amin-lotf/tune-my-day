package com.aminook.tunemyday.framework.presentation.addroutine

import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.interactors.routine.DeleteRoutine
import com.aminook.tunemyday.business.interactors.routine.RoutineInteractors
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.util.ROUTINE_INDEX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class AddRoutineViewModel @ViewModelInject constructor(
    val routineInteractors: RoutineInteractors,
    @DataStoreCache dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache,dataStoreSettings) {

    val activeScope=viewModelScope.coroutineContext+Default
    var routineInEditId:Long=0




    private suspend fun saveRoutineIndex(routineId: Long) {

            dataStoreCache.edit { cache ->
                cache[ROUTINE_INDEX] = routineId
            }

    }

    fun addRoutine(routineName: String) {

        val routine = RoutineEntity(routineName)
        CoroutineScope(activeScope).launch {
            routineInteractors.insertRoutine(routine,routineId)
                .map {
                    it?.data?.let { routineId ->
                        saveRoutineIndex(routineId)
                    }
                    processResponse(it?.stateMessage)
                }.collect()
        }
    }

    fun updateRoutine(routineName: String) {
        val routine = RoutineEntity(routineName).apply {
            id=routineInEditId
        }
        CoroutineScope(activeScope).launch {
            routineInteractors.updateRoutine(routine).map {
                processResponse(it?.stateMessage)
            }.collect()
        }
    }

    fun deleteRoutine() {
        CoroutineScope(activeScope).launch {
            routineInteractors.deleteRoutine(
                routineInEditId,
                activeRoutineId = routineId
            ).map {
                processResponse(it?.stateMessage)

                if (it?.data== DeleteRoutine.ROUTINE_DELETE_SUCCESS){
                    saveRoutineIndex(0)
                }
            }.collect()
        }
    }


}