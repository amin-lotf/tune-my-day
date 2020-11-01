package com.aminook.tunemyday.framework.presentation.addprogram

import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.state.AreYouSureCallback
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.business.interactors.program.ProgramInteractors
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single


class AddProgramViewModel @ViewModelInject constructor(
    val programInteractors: ProgramInteractors,
    @DataStoreCache dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache,dataStoreSettings) {

    private val activeScope=viewModelScope.coroutineContext+ Default

    private var _savedProgram:ProgramDetail?=null
    private var _newProgramId=MutableLiveData<Long>()

    val savedProgram:ProgramDetail?
    get() = _savedProgram

    val newProgramId:LiveData<Long>
    get() = _newProgramId

    fun addProgram(program: Program) {
        CoroutineScope(activeScope).launch {
            programInteractors.insertProgram(program)
                .map {
                    withContext(Main) {
                        _newProgramId.value = it?.data?.id ?: 0
                    }
                    processResponse(it?.stateMessage)

                }
                .collect()
        }
    }

    fun updateProgram(program: Program){
        CoroutineScope(activeScope).launch {
            programInteractors.updateProgram(program)
                .map {
                    processResponse(it?.stateMessage)
                }
                .collect()
        }
    }

    fun getProgram(programId:Long):LiveData<ProgramDetail?>{
        return programInteractors.getDetailedProgram(programId)
            .map {
                processResponse(it?.stateMessage)
                _savedProgram=it?.data
                it?.data
            }
            .asLiveData()
    }

    fun requestDelete(program: ProgramDetail){
       getConfirmation("All related schedules will be deleted",object :AreYouSureCallback{
           override fun proceed() {
               deleteProgram(program)
           }

           override fun cancel() {

           }
       })
    }

    fun deleteProgram(program: ProgramDetail){
        CoroutineScope(activeScope).launch {
            delay(300)
            programInteractors.deleteProgram(
                program = program)
                .map {
                    processResponse(it?.stateMessage)
                }.collect()
        }
    }

}