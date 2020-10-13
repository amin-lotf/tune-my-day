package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.business.domain.model.*
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    suspend fun insertRoutine(routineEntity: RoutineEntity):Long
    fun getRoutine(routineId:Long):Flow<RoutineEntity>
    fun getAllRoutines():Flow<List<RoutineEntity>>
    suspend fun updateRoutine(routineEntity: RoutineEntity):Int
    suspend fun deleteRoutine(routineEntity: RoutineEntity):Int

    suspend fun updateProgram(program: Program):Int
    suspend fun insertProgram(program: Program):Long
    fun getAllPrograms(): Flow<List<Program>>
    fun getAllProgramsDetail(): Flow<List<ProgramDetail>>
    fun selectProgram(id:Int):Flow<Program?>
    suspend fun deleteAllPrograms(): Int
    suspend fun deleteProgram(program: ProgramDetail): Int
    suspend fun undoDeletedProgram(program: ProgramDetail):Long?

    fun getDaysOfWeek(chosenDay:Int=1):List<Day>

    suspend fun insertModifySchedule(schedule: Schedule, conflictedSchedule:List<Schedule>, requestType:String):Long?
    suspend fun checkIfOverwrite(schedule: Schedule):List<Schedule>
    fun getAllSchedules(routineId:Long):Flow<List<Schedule>>
    suspend fun getUpcomingAlarms(startDay:Int,endDay:Int):List<Alarm>
    suspend fun deleteSchedule(scheduleId:Long):Int
    suspend fun getSchedule(scheduleId:Long):Schedule
    fun getDailySchedules(dayIndex:Int,routineId:Long):Flow<List<Schedule>>


    suspend fun insertTodo(todo:Todo):Long
    suspend fun deleteTodo(todo: Todo):Int
    suspend fun getScheduleTodos(scheduleId: Long):List<Todo>
    suspend fun deleteAndRetrieveTodos(todo: Todo):List<Todo>
    suspend fun insertAndRetrieveTodos(todo: Todo):List<Todo>
    suspend fun updateAndRetrieveTodos(todo: Todo):List<Todo>
    suspend fun updateListAndRetrieveTodos(todos: List<Todo>,scheduleId: Long):List<Todo>
}