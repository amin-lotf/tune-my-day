package com.aminook.tunemyday.framework.presentation.addschedule

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.*
import com.aminook.tunemyday.business.domain.state.AreYouSureCallback
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.interactors.program.ProgramInteractors
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_NEW
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion



class AddScheduleViewModel @ViewModelInject constructor(
    val programInteractors: ProgramInteractors,
    val scheduleInteractors: ScheduleInteractors,

    ) : BaseViewModel() {
    private val TAG = "aminjoon"
    val addScheduleManager = AddScheduleManager()
    private var conflictedSchedules = listOf<Schedule>()
    private val activeScope = IO + viewModelScope.coroutineContext
    private var _allPrograms = MutableLiveData<List<Program>>()
    private var job:Job?=null


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

    val alarmModifiedPosition: Int
        get() = addScheduleManager.alarmModifiedPosition


    fun validateSchedule(areYouSureCallback: AreYouSureCallback) {
        // Log.d(TAG, "validateSchedule time: ${addScheduleManager.buffSchedule.startTime.minute}-${addScheduleManager.buffSchedule.endTime.minute}")
        Log.d(
            TAG,
            "validateSchedule:${addScheduleManager.buffSchedule.startDay} ${addScheduleManager.buffSchedule.startInSec}-${addScheduleManager.buffSchedule.endInSec}"
        )

        if (addScheduleManager.buffSchedule.program == null) {
            handleLocalError("Please choose an activity")
        } else {
             job=CoroutineScope(activeScope).launch {
                scheduleInteractors.validateSchedule(
                    schedule = addScheduleManager.buffSchedule, areYouSureCallback
                )
                    .collect { dataState ->
                    Log.d(TAG, "validateSchedule: ")
                    processResponse(dataState?.stateMessage)

                    dataState?.data?.let {
                        if (it.isNullOrEmpty()) {

                            saveSchedule(emptyList())
                        } else {
//                            it.forEach {s->
//                                Log.d(TAG, "validateSchedule: s day:${s.startDay} starts:${s.startTime.hour}:${s.startTime.minute} ")
//                            }
                            conflictedSchedules = it
                        }

                    }
                }
            }
          //  handleJob(job)
        }
    }

    fun saveSchedule(confSchedules:List<Schedule> = conflictedSchedules) {
        if (addScheduleManager.buffSchedule.program == null) {
            handleLocalError("Please choose an activity")
        } else {
            Log.d(TAG, "saveSchedule: id of program: ${addScheduleManager.buffSchedule.program?.id}")
            job=CoroutineScope(activeScope).launch {
                scheduleInteractors.insertSchedule(
                    addScheduleManager.buffSchedule,
                    confSchedules
                )
                    .collect { dataState ->
                    Log.d(TAG, "saveSchedule: ")
                    processResponse(dataState?.stateMessage)


                }
            }
        }
    }

    fun processRequest(request: String) {
        when (request) {
            SCHEDULE_REQUEST_NEW -> {

            }
        }
    }


    fun removeAlarm(alarm: Alarm) {
        addScheduleManager.removeAlarm(alarm)
    }

    fun getAlarms() = addScheduleManager.alarms


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
                Log.d(TAG, "catchDaysOfWeek: ")
                processResponse(dataState?.stateMessage)
                dataState?.data?.let { daysList ->
                    addScheduleManager.addDaysToBuffer(daysList)
                    daysList.forEach { day ->
                        if (day.isChosen) {
                            withContext(Main) {
                                addScheduleManager.setChosenDay(day)
                            }
                        }
                    }
                }
            }
        }
    }


    fun addProgram(program: Program) {
        CoroutineScope(activeScope).launch {

            programInteractors.insertProgram(program).collect { dataState ->

                processResponse(dataState?.stateMessage)
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
        Log.d(TAG, "getAllPrograms: out ")
        job=CoroutineScope(activeScope).launch {
            programInteractors.getAllPrograms()
                .collect { dataState ->
                Log.d(TAG, "getAllPrograms: ")
                processResponse(dataState?.stateMessage)
                dataState?.data?.let {

                    _allPrograms.postValue(dataState.data)
                }

            }
        }
    }


    private fun handleJob(job:Job?){
        job?.let {
            CoroutineScope(activeScope).launch {
                job.cancelAndJoin()
            }
        }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: viewmodel")
        super.onCleared()
    }


}