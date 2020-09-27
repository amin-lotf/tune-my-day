package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.business.domain.model.*
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    suspend fun updateProgram(program: Program):Int
    suspend fun insertProgram(program: Program):Long
    fun getAllPrograms(): Flow<List<Program>>
    fun selectProgram(id:Int):Flow<Program?>
    suspend fun deleteAllPrograms(): Int
    suspend fun deleteProgram(program: Program): Int

    fun getDaysOfWeek(chosenDay:Int=1):List<Day>

    suspend fun insertModifySchedule(schedule: Schedule, conflictedSchedule:List<Schedule>, requestType:String):Long?
    suspend fun checkIfOverwrite(schedule: Schedule):List<Schedule>
    fun getAllSchedules():Flow<List<Schedule>>
    suspend fun getUpcomingAlarms(startDay:Int,endDay:Int):List<Alarm>
    suspend fun deleteSchedule(scheduleId:Long):Int
    suspend fun getSchedule(scheduleId:Long):Schedule
    fun getDailySchedules(dayIndex:Int):Flow<List<Schedule>>


    suspend fun insertTodo(todo:Todo)

}