package com.aminook.tunemyday.framework.presentation.addschedule.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.aminook.tunemyday.business.domain.model.*


class AddScheduleManager {
    //private val TAG = "aminjoon"

    private val _buffSchedule = Schedule()
    private var _chosenProgram = MutableLiveData<Program>()
    private var _daysOfWeek = mutableListOf<Day>()
    private var _chosenDay = MutableLiveData<Day>()
    private var _startTime = MutableLiveData<Time>()
    private var _endTime = MutableLiveData<Time>()
    private var _listChanged = MutableLiveData<String>()
    private var _alarmModifiedPosition = 0
    private val _scheduleLoaded = MutableLiveData<Boolean>()
    private val _numberOfTodos = MutableLiveData<Int>()
    private val _alarmCounter = MutableLiveData<Int>()
    private val _isNextDay = MutableLiveData<Boolean>()

    init {

        _scheduleLoaded.value = false
    }

    fun initializeSchedule() {
        _listChanged.value = ALARM_LIST_ADDED
    }


    fun getScheduleStatus(): LiveData<Boolean> = _scheduleLoaded

    fun setScheduleStatus(status: Boolean) {
        _scheduleLoaded.value = status
    }


    val alarmCounter: LiveData<Int>
        get() = _alarmCounter


    val numberOfTodos: LiveData<Int>
        get() = _numberOfTodos

    val buffSchedule: Schedule
        get() = _buffSchedule

    val startTime: LiveData<Time>
        get() = _startTime

    val endTime: LiveData<Time>
        get() = _endTime

    val alarms: List<Alarm>
        get() = _buffSchedule.alarms


    val listChanged: LiveData<String>
        get() = _listChanged

    val alarmModifiedPosition: Int
        get() = _alarmModifiedPosition

    val chosenProgram: LiveData<Program>
        get() = _chosenProgram

    val daysOfWeek: List<Day>
        get() = _daysOfWeek

    val isNextDay: LiveData<Boolean>
        get() = _isNextDay

    fun setScheduleId(id: Long) {
        _buffSchedule.id = id
    }

    fun removeAlarm(alarm: Alarm) {
        _buffSchedule.alarms.remove(alarm)
        _alarmModifiedPosition = alarm.index
        _listChanged.value = ALARM_REMOVED
        _alarmCounter.value = _buffSchedule.alarms.size

    }


    fun addTodos(todos: List<Todo>) {
        _buffSchedule.finishedTodos.clear()
        _buffSchedule.finishedTodos.addAll(todos.filter { it.isDone })

        _buffSchedule.unfinishedTodos.clear()
        _buffSchedule.unfinishedTodos.addAll(todos.filter { !it.isDone })

    }


    fun setRoutineId(routineId: Long) {
        _buffSchedule.routineId = routineId
    }

    fun addAlarms(alarms: List<Alarm>) {
        _buffSchedule.alarms.addAll(alarms)
        _listChanged.value = ALARM_LIST_ADDED
        _alarmCounter.value = _buffSchedule.alarms.size
    }

    fun addAlarm(alarm: Alarm) {
        alarm.routineId = _buffSchedule.routineId
        if (_buffSchedule.alarms.any { it.hourBefore == alarm.hourBefore && it.minuteBefore == alarm.minuteBefore }) {
            return
        }
        alarm.scheduleId = _buffSchedule.id
        _buffSchedule.alarms.add(alarm)
        _buffSchedule.alarms.sortWith(compareBy<Alarm> { it.hourBefore }.thenBy { it.minuteBefore })
        val index = _buffSchedule.alarms.indexOf(alarm)
        _alarmModifiedPosition = index
        _listChanged.value = ALARM_ADDED
        _alarmCounter.value = _buffSchedule.alarms.size
    }

    fun getChosenDay() = _chosenDay

    fun addProgramToBuffer(program: Program) {
        _buffSchedule.program = program
        _chosenProgram.value = program
    }


    fun addDaysToBuffer(days: List<Day>) {
        _daysOfWeek.clear()
        _daysOfWeek.addAll(days)
    }

    fun setTime(hour: Int, minute: Int, type: Int) {
        when (type) {
            TIME_START -> {
                _buffSchedule.startTime.hour = hour
                _buffSchedule.startTime.minute = minute
                _startTime.value = _buffSchedule.startTime
            }
            TIME_END -> {
                val tmpHour = if (hour == 0 && minute == 0) 24 else hour
                _buffSchedule.endTime.hour = tmpHour
                _buffSchedule.endTime.minute = minute
                _endTime.value = _buffSchedule.endTimeFormatted
            }
        }
        _isNextDay.value = _buffSchedule.endTime.hour != 24 &&
                _buffSchedule.startTime.hour >= _buffSchedule.endTime.hour &&
                _buffSchedule.startTime.minute >= _buffSchedule.endTime.minute
    }

    fun setChosenDay(chosenDay: Day) {
        _buffSchedule.startDay = chosenDay.dayIndex
        _chosenDay.value = chosenDay
        for (day in _daysOfWeek) {
            day.isChosen = day === chosenDay
        }
    }


    companion object {
        const val TIME_START = 0
        const val TIME_END = 1
        const val ALARM_LIST_ADDED = "list of alarms added"
        const val ALARM_ADDED = "item added to alarm list"
        const val ALARM_REMOVED = "item removed from alarm list"
    }

}