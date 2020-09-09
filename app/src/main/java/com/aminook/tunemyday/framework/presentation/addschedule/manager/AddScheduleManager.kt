package com.aminook.tunemyday.framework.presentation.addschedule.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aminook.tunemyday.business.domain.model.*


class AddScheduleManager {
    private val TAG="aminjoon"

    val buffSchedule = Schedule()
    private var _chosenProgram=MutableLiveData<Program>()
    private var _daysOfWeek = mutableListOf<Day>()
    private var _chosenDay =MutableLiveData<Day>()
    private var _startTime=MutableLiveData<Time>()
    private var _endTime=MutableLiveData<Time>()
    private var _listChanged=MutableLiveData<String>()
    private var _alarmModifiedPosition=0



    val startTime:LiveData<Time>
    get()= _startTime

    val endTime:LiveData<Time>
    get() = _endTime

    val alarms:List<Alarm>
    get() = buffSchedule.alarms


    val listChanged:LiveData<String>
    get() = _listChanged

    val alarmModifiedPosition:Int
    get() = _alarmModifiedPosition

    val chosenProgram:LiveData<Program>
    get() = _chosenProgram

    fun removeAlarm(alarm: Alarm){
        buffSchedule.alarms.remove(alarm)
        _alarmModifiedPosition=alarm.index
        _listChanged.value= ALARM_LIST_REMOVED

    }

    fun addAlarm(alarm: Alarm){
        if(!alarm.inEditMode) {
            buffSchedule.alarms.add(alarm)
            buffSchedule.alarms.sortWith(compareBy<Alarm> { it.hourBefore }.thenBy { it.minuteBefore })
            val index = buffSchedule.alarms.indexOf(alarm)
            alarm.index=index

            _alarmModifiedPosition = index
            _listChanged.value = ALARM_LIST_ADDED
        }else{
            if(alarm.index !=-1){

                if (alarm.index<buffSchedule.alarms.size){

                    buffSchedule.alarms.removeAt(alarm.index)
                    _alarmModifiedPosition=alarm.index
                    _listChanged.value= ALARM_LIST_REMOVED

                    buffSchedule.alarms.add(alarm)

                    buffSchedule.alarms.sortWith(compareBy<Alarm>{it.hourBefore}.thenBy { it.minuteBefore })
                    val newIndex=buffSchedule.alarms.indexOf(alarm)
                    alarm.index=newIndex
                    _alarmModifiedPosition=newIndex
                    _listChanged.value= ALARM_LIST_ADDED
                }

            }
        }
    }

    fun getChosenDay() = _chosenDay

    fun addProgramToBuffer(program: Program) {
        buffSchedule.program = program
        _chosenProgram.value=program
    }

    fun removeProgramFromBuffer() {
        buffSchedule.program = null
    }

    val isValid: Boolean
        get() = buffSchedule.program != null

    fun addDaysToBuffer(days: List<Day>) {
        _daysOfWeek.clear()
        _daysOfWeek.addAll(days)
    }

    fun setTime(hour:Int,minute:Int, type: Int){
        when(type){
            TIME_START->{
                buffSchedule.startTime.hour=hour
                buffSchedule.startTime.minute= minute
                _startTime.value=buffSchedule.startTime
            }
            TIME_END->{
                buffSchedule.endTime.hour=hour
                buffSchedule.endTime.minute= minute
                _endTime.value=buffSchedule.endTime
            }
        }
    }

    fun setChosenDay(chosenDay: Day) {
        buffSchedule.startDay=chosenDay.dayIndex
        _chosenDay.value=chosenDay
        for (day in _daysOfWeek) {
            day.isChosen = day === chosenDay
        }
    }

    fun getBufferedDays(): List<Day> = _daysOfWeek



    companion object{
        val TIME_START=0
        val TIME_END=1
        val ALARM_LIST_ADDED="item added to alarm list"
        val ALARM_LIST_REMOVED="item removed from alarm list"
    }

}