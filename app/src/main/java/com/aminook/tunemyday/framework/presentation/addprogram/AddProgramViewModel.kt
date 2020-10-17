package com.aminook.tunemyday.framework.presentation.addprogram

import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.interactors.program.ProgramInteractors
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddProgramViewModel @ViewModelInject constructor(
    val programInteractors: ProgramInteractors,
    @DataStoreCache dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache,dataStoreSettings) {

    private var _savedProgram:ProgramDetail?=null
    private var _newProgramId=MutableLiveData<Long>()

    val savedProgram:ProgramDetail?
    get() = _savedProgram

    val newProgramId:LiveData<Long>
    get() = _newProgramId

    fun addProgram(program: Program) {
        CoroutineScope(Dispatchers.Default).launch {
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
        CoroutineScope(Dispatchers.Default).launch {
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

}