package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.model.Schedule
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

    suspend fun insertSchedule(schedule: Schedule,conflictedSchedule:List<Schedule>):Long?
    suspend fun checkIfOverwrite(schedule: Schedule):List<Schedule>
    fun getDailySchedules():Flow<List<Schedule>>
    suspend fun getUpcomingAlarms(startDay:Int,endDay:Int):List<Alarm>

    suspend fun getSchedule(scheduleId:Long):Schedule
}