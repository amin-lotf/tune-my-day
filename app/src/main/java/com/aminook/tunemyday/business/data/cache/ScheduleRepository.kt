package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramEntity
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleAndProgram
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    suspend fun updateProgram(program: Program):Int
    suspend fun insertProgram(program: Program):Long
    fun getAllPrograms(): Flow<List<Program>>
    fun selectProgram(id:Int):Flow<Program?>
    suspend fun deleteAllPrograms(): Int
    suspend fun deleteProgram(program: Program): Int
    fun getDaysOfWeek(chosenDay:Int=1):List<Day>

    suspend fun insertSchedule(schedule: Schedule,conflictedSchedule:List<Schedule>):Long
    fun checkIfOverwrite(schedule: Schedule):Flow<List<Schedule>?>
}