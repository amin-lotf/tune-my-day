package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.business.domain.model.*
import com.aminook.tunemyday.framework.datasource.cache.model.*
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    suspend fun cancelCurrentRoutineAlarms(routineId: Long): Boolean
    suspend fun scheduleCurrentRoutineAlarms(routineId: Long): Boolean
    suspend fun rescheduleAlarmsForNewRoutine(prevRoutineId:Long,currentRoutineId:Long):Boolean

    suspend fun scheduleUpcomingAlarmsByRoutine(routineId: Long): Boolean
    suspend fun getAlarmsById(alarmIds:List<Long>):List<Alarm>
    suspend fun getAlarmById(alarmId:Long):Alarm
    suspend fun getNotificationScheduleByAlarmId(alarmId:Long):Schedule

    suspend fun insertRoutine(routineEntity: RoutineEntity,curRoutine: Long):Long
    fun getRoutine(routineId:Long):Flow<RoutineEntity>
    fun getAllRoutines():Flow<List<RoutineEntity>>
    suspend fun updateRoutine(routineEntity: RoutineEntity):Int
    suspend fun deleteRoutine(routineId: Long,curRoutine: Long):Int

    suspend fun updateProgram(program: Program):Int
    suspend fun insertProgram(program: Program):Long
    fun getAllPrograms(): Flow<List<Program>>
    fun getAllProgramsDetail(): Flow<List<ProgramDetail>>
    fun getProgramDetail(id:Long):Flow<ProgramDetail>
    fun getProgram(id:Long):Flow<Program>
    suspend fun deleteAllPrograms(): Int
    suspend fun deleteProgram(program: ProgramDetail): Int
    suspend fun undoDeletedProgram(program: ProgramDetail,curRoutine: Long):Long?

    fun getDaysOfWeek(chosenDay:Int=1):List<Day>

    suspend fun insertModifySchedule(schedule: Schedule, conflictedSchedule:List<Schedule>, requestType:String,curRoutine:Long):Long?
    suspend fun checkIfOverwrite(schedule: Schedule):List<Schedule>
    fun getAllSchedules(routineId:Long):Flow<List<Schedule>>
    suspend fun deleteSchedule(schedule:Schedule):Int
    suspend fun getSchedule(scheduleId:Long):Schedule

    suspend fun getDetailedSchedule(scheduleId:Long):Schedule

    fun getDailySchedules(dayIndex:Int,routineId:Long,curTime:Int):Flow<List<Schedule>>


    suspend fun insertTodo(todo:Todo):Long
    suspend fun updateTodos(todos:List<Todo>):Int
    suspend fun updateTodo(todo: Todo):Int
    suspend fun deleteTodo(todo: Todo):Int
    fun getScheduleTodos(scheduleId: Long):Flow<List<Todo>>
    suspend fun deleteAndRetrieveTodos(todo: Todo):List<Todo>
    suspend fun insertAndRetrieveTodos(todo: Todo):List<Todo>
    suspend fun updateAndRetrieveTodos(todo: Todo):List<Todo>
    suspend fun updateListAndRetrieveTodos(todos: List<Todo>,scheduleId: Long):List<Todo>

}