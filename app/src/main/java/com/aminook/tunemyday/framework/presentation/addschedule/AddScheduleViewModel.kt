package com.aminook.tunemyday.framework.presentation.addschedule

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.model.Time
import com.aminook.tunemyday.business.interactors.program.ProgramInteractors
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_NEW
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*


class AddScheduleViewModel @ViewModelInject constructor(
    val programInteractors: ProgramInteractors,
    val scheduleInteractors: ScheduleInteractors,

) : BaseViewModel() {
    private val TAG = "aminjoon"
    val addScheduleManager= AddScheduleManager()
    private val activeScope = IO + viewModelScope.coroutineContext

    private var _allPrograms = MutableLiveData<List<Program>>()
    private var _alarm=MutableLiveData<Alarm>()

    val selectedProgram: LiveData<Program>
        get() = addScheduleManager.chosenProgram

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


    val listChanged: LiveData<String>
        get() = addScheduleManager.listChanged

    val alarmModifiedPosition:Int
    get() = addScheduleManager.alarmModifiedPosition


    fun processRequest(request: String) {
        when(request){
            SCHEDULE_REQUEST_NEW->{
                addScheduleManager.clearBuffer()
            }
        }
    }

    fun checkReceivedAlarmFromDialog():LiveData<Alarm> = _alarm

    fun removeAlarm(alarm: Alarm){
        addScheduleManager.removeAlarm(alarm)
    }

    fun getAlarms()=addScheduleManager.alarms

    fun saveAlarmFromDialog(alarm: Alarm){
        Log.d(TAG, "saveAlarmFromDialog: ")
        _alarm.postValue(alarm)
    }

    fun setAlarm(alarm: Alarm) {

        addScheduleManager.addAlarm(alarm)
    }

    fun setTime(hour: Int, minute: Int, type: Int) {
        addScheduleManager.setTime(hour, minute, type)
    }

    fun updateBufferedDays(updatedDay: Day) {
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
        CoroutineScope(activeScope).launch {
            programInteractors.insertProgram(program).collect { dataState ->
                //TODO(save stateMessage if an then send it main activity to show dialog and ...)
                dataState?.data?.let {
                    bufferChosenProgram(it)
                }
            }
        }
    }

    fun bufferChosenProgram(program: Program) {
        addScheduleManager.addProgramToBuffer(program)

    }

    fun getAllPrograms() {
        CoroutineScope(activeScope).launch {
            programInteractors.getAllPrograms().collect { dataState ->

                dataState?.data?.let {

                    _allPrograms.postValue(dataState.data)
                }

            }
        }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: viewmodel")
        super.onCleared()
    }




}