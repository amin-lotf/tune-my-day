package com.aminook.tunemyday.framework.presentation.addschedule

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.interactors.program.ProgramInteractors
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch


class AddScheduleViewModel @ViewModelInject constructor(
    val programInteractors: ProgramInteractors
):BaseViewModel() {

    private val TAG="aminjoon"

    private var _selectedProgram= MutableLiveData<Program>()

    val selectedProgram:LiveData<Program>
        get()=_selectedProgram

    fun addProgram(program:Program){
        //_selectedProgram= programInteractors.insertProgram(program).asLiveData(IO+viewModelScope.coroutineContext) as MutableLiveData<DataState<Program?>>
        CoroutineScope(IO+viewModelScope.coroutineContext).launch {

           programInteractors.insertProgram(program).collect { dataState->

               //TODO(save stateMessage if an then send it main activity to show dialog and ...)

               dataState.data?.let {
                   _selectedProgram.value=it
               }
           }
        }


    }




}