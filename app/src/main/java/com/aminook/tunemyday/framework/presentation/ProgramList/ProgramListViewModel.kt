package com.aminook.tunemyday.framework.presentation.ProgramList

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.business.interactors.program.ProgramInteractors
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch


class ProgramListViewModel @ViewModelInject constructor(
    val programInteractors: ProgramInteractors
):BaseViewModel() {


    fun getAllPrograms():LiveData<List<ProgramDetail>>{
        return   programInteractors.getAllDetailedPrograms()
            .map {
                processResponse(it?.stateMessage)
                it?.data?: emptyList()
            }
            .flowOn(Default)
            .asLiveData()
    }


    fun deleteProgram(program:ProgramDetail){
        CoroutineScope(Default).launch {
            programInteractors.deleteProgram(
                program,
                object :SnackbarUndoCallback{
                    override fun undo() {
                       undoDeletedProgram(program)
                    }
                }
            )
                .map {
                    processResponse(it?.stateMessage)
                }
                .single()
        }

    }


    fun undoDeletedProgram(program: ProgramDetail){
        CoroutineScope(Default).launch {
            programInteractors.undoDeletedProgram(program)
                .map {
                    processResponse(it?.stateMessage)
                }
                .single()
        }
    }



}