package com.aminook.tunemyday.framework.presentation.addschedule

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.model.Time
import com.aminook.tunemyday.business.interactors.program.ProgramInteractors
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*


class AddScheduleViewModel @ViewModelInject constructor(
    val programInteractors: ProgramInteractors,
    val scheduleInteractors: ScheduleInteractors,
    val addScheduleManager: AddScheduleManager
) : BaseViewModel() {

    private val TAG = "aminjoon"

    private val activeScope = IO + viewModelScope.coroutineContext

    private var _chosenDay = MutableLiveData<Day>()
    private var _selectedProgram = MutableLiveData<Program>()
    private var _allPrograms = MutableLiveData<List<Program>>()

    val selectedProgram: LiveData<Program>
        get() = _selectedProgram

    val allPrograms: LiveData<List<Program>>
        get() = _allPrograms

    val daysOfWeek: List<Day>
        get() = addScheduleManager.getBufferedDays()

    val chosenDay: LiveData<Day>
        get() = addScheduleManager.getChosenDay()

    val startTime: LiveData<Time>
        get() = addScheduleManager.startTime

    val endTime: LiveData<Time>
        get() = addScheduleManager.endTime

    fun setTime(hour:Int,minute:Int, type: Int){
        addScheduleManager.setTime(hour,minute, type)
    }

    fun updateBufferedDays(updatedDay: Day) {
        //_chosenDay.value=updatedDay
        addScheduleManager.setChosenDay(updatedDay)
    }

    fun catchDaysOfWeek(chosenDay: Int) {
        CoroutineScope(activeScope).launch {
            scheduleInteractors.getDaysOfWeek(chosenDay).collect { dataState ->
                dataState?.data?.let { daysList ->
                    addScheduleManager.addDaysToBuffer(daysList)
                    daysList.forEach { day ->
                        if (day.isChosen) {
                            addScheduleManager.setChosenDay(day)
                        }
                    }
                }
            }
        }
    }


    fun addProgram(program: Program) {
        //_selectedProgram= programInteractors.insertProgram(program).asLiveData(IO+viewModelScope.coroutineContext) as MutableLiveData<DataState<Program?>>
        CoroutineScope(activeScope).launch {
            programInteractors.insertProgram(program).collect { dataState ->
                //TODO(save stateMessage if an then send it main activity to show dialog and ...)
                dataState?.data?.let {
                    addScheduleManager.addProgramToBuffer(it)
                    _selectedProgram.value = it
                }

            }
        }
    }

    fun bufferChosenProgram(program: Program) {
        addScheduleManager.addProgramToBuffer(program)
        _selectedProgram.value = program
    }

    fun getAllPrograms() {
        CoroutineScope(activeScope).launch {
            programInteractors.getAllPrograms().collect { dataState ->
                Log.d(
                    TAG,
                    "getAllPrograms: ${dataState?.stateMessage?.peekContent()?.response?.message}"
                )
                dataState?.data?.let {

                    _allPrograms.value = dataState.data
                }

            }
        }
    }

}