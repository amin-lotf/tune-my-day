package com.aminook.tunemyday.framework.presentation.addschedule.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.aminook.tunemyday.business.domain.model.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class AddScheduleManager @Inject constructor(
    val dayFactory: DayFactory
) {
    private val TAG="aminjoon"
    private val buffSchedule = Schedule()
    private var _daysOfWeek = mutableListOf<Day>()
    private var _chosenDay =MutableLiveData<Day>()
    private var _startTime=MutableLiveData<Time>()
    private var _endTime=MutableLiveData<Time>()



    val bufferedSchedule:LiveData<Schedule>
    get()= liveData {
        emit(buffSchedule)
    }

    val startTime:LiveData<Time>
    get()=_startTime

    val endTime:LiveData<Time>
    get() = _endTime



    fun getChosenDay() = _chosenDay

    fun addProgramToBuffer(program: Program) {
        buffSchedule.program = program
    }

    fun removeProgramFromBuffer() {
        buffSchedule.program = null
    }

    val isValid: Boolean
        get() = buffSchedule.program != null

    fun addDaysToBuffer(days: List<Day>) {
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
        buffSchedule.day=chosenDay.dayIndex
        _chosenDay.value=chosenDay
        for (day in _daysOfWeek) {
            day.isChosen = day === chosenDay
        }
    }

    fun getBufferedDays(): List<Day> = _daysOfWeek

    companion object{
        val TIME_START=0
        val TIME_END=1
    }

}