package com.aminook.tunemyday.framework.presentation.addschedule.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aminook.tunemyday.business.domain.model.*


class AddScheduleManager {
    private val TAG="aminjoon"

    private val _buffSchedule = Schedule()
    private var _chosenProgram=MutableLiveData<Program>()
    private var _daysOfWeek = mutableListOf<Day>()
    private var _chosenDay =MutableLiveData<Day>()
    private var _startTime=MutableLiveData<Time>()
    private var _endTime=MutableLiveData<Time>()
    private var _listChanged=MutableLiveData<String>()
    private var _alarmModifiedPosition=0


    val buffSchedule:Schedule
    get()=_buffSchedule

    val startTime:LiveData<Time>
    get()= _startTime

    val endTime:LiveData<Time>
    get() = _endTime

    val alarms:List<Alarm>
    get() = _buffSchedule.alarms


    val listChanged:LiveData<String>
    get() = _listChanged

    val alarmModifiedPosition:Int
    get() = _alarmModifiedPosition

    val chosenProgram:LiveData<Program>
    get() = _chosenProgram

    val daysOfWeek: List<Day>
        get() = _daysOfWeek

    fun setScheduleId(id:Long){
        _buffSchedule.id=id
    }

    fun removeAlarm(alarm: Alarm){
        _buffSchedule.alarms.remove(alarm)
        _alarmModifiedPosition=alarm.index
        _listChanged.value= ALARM_REMOVED

    }

    fun addAlarm(alarm: Alarm){
        if (_buffSchedule.alarms.any { it.hourBefore==alarm.hourBefore && it.minuteBefore==alarm.minuteBefore }){
            return
        }
        alarm.scheduleId=_buffSchedule.id
        if(!alarm.inEditMode) {
            _buffSchedule.alarms.add(alarm)
            _buffSchedule.alarms.sortWith(compareBy<Alarm> { it.hourBefore }.thenBy { it.minuteBefore })
            val index = _buffSchedule.alarms.indexOf(alarm)
            alarm.index=index

            _alarmModifiedPosition = index
            _listChanged.value = ALARM_ADDED
        }else{
            if(alarm.index !=-1){

                if (alarm.index<_buffSchedule.alarms.size){

                    _buffSchedule.alarms.removeAt(alarm.index)
                    _alarmModifiedPosition=alarm.index
                    _listChanged.value= ALARM_REMOVED

                    _buffSchedule.alarms.add(alarm)

                    _buffSchedule.alarms.sortWith(compareBy<Alarm>{it.hourBefore}.thenBy { it.minuteBefore })
                    val newIndex=_buffSchedule.alarms.indexOf(alarm)
                    alarm.index=newIndex
                    _alarmModifiedPosition=newIndex
                    _listChanged.value= ALARM_ADDED
                }

            }
        }
    }

    fun getChosenDay() = _chosenDay

    fun addProgramToBuffer(program: Program) {
        _buffSchedule.program = program
        _chosenProgram.value=program
        Log.d(TAG, "addProgramToBuffer: ${_buffSchedule.program?.id}")
    }





    fun addDaysToBuffer(days: List<Day>) {
        _daysOfWeek.clear()
        _daysOfWeek.addAll(days)
    }

    fun setTime(hour:Int,minute:Int, type: Int){
        when(type){
            TIME_START->{
                _buffSchedule.startTime.hour=hour
                _buffSchedule.startTime.minute= minute
                _startTime.value=_buffSchedule.startTime
            }
            TIME_END->{
                _buffSchedule.endTime.hour=hour
                _buffSchedule.endTime.minute= minute
                _endTime.value=_buffSchedule.endTime
            }
        }
    }

    fun setChosenDay(chosenDay: Day) {
        _buffSchedule.startDay=chosenDay.dayIndex
        _chosenDay.value=chosenDay
        for (day in _daysOfWeek) {
            day.isChosen = day === chosenDay
        }
    }





    companion object{
        val TIME_START=0
        val TIME_END=1
        val ALARM_LIST_ADDED="list of alarms added"
        val ALARM_ADDED="item added to alarm list"
        val ALARM_REMOVED="item removed from alarm list"
    }

}