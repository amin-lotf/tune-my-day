package com.aminook.tunemyday.business.data.cache

import android.util.Log
import com.aminook.tunemyday.business.data.util.getConflictedSchedules
import com.aminook.tunemyday.business.data.util.selectSchedulesToDelete
import com.aminook.tunemyday.business.data.util.updateSchedules
import com.aminook.tunemyday.business.domain.model.*
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.framework.datasource.cache.database.*
import com.aminook.tunemyday.framework.datasource.cache.mappers.Mappers
import com.aminook.tunemyday.framework.datasource.cache.model.AlarmEntity
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_EDIT
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_NEW
import com.aminook.tunemyday.worker.NotificationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

//TODO(use try catch for queries)
@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    val daoService: DaoService,
    val mappers: Mappers,
    val dateUtil: DateUtil,
    val scheduleDatabase: ScheduleDatabase,
    val notificationManager: NotificationManager
) : ScheduleRepository {
    private val TAG = "aminjoon"

    override  fun scheduleUpComingAlarms(alarms: List<Alarm>): Boolean {
        val alarmIds=alarms.map { it.id }
        return try {
            notificationManager.setNotifications(alarmIds)
            true
        } catch (e: Throwable) {
            Log.d(TAG, "doWorkk scheduleUpComingAlarms: error")
            false
        }
    }

    override suspend fun cancelCurrentRoutineAlarms(routineId: Long): Boolean {
        val alarmIds=getUpcomingAlarmIdsByRoutine(routineId)
        return try {
            notificationManager.removeNotifications(alarmIds)
            true
        } catch (e: Throwable) {
            Log.d(TAG, "doWorkk cancel UpComingAlarms by routine: error")
            false
        }
    }

    override suspend fun scheduleCurrentRoutineAlarms(routineId: Long): Boolean {
        val alarmIds=getUpcomingAlarmIdsByRoutine(routineId)
        return try {
            notificationManager.setNotifications(alarmIds)
            true
        } catch (e: Throwable) {
            Log.d(TAG, "doWorkk set UpComingAlarms by routine: error")
            false
        }
    }

    override fun getUpcomingAlarms(routineId: Long): Flow<List<Alarm>> {

        val dayRange = dateUtil.shortDayRange

        return daoService.alarmDao.selectUpcomingAlarmsDistinct(
            routineId,
            dayRange,
            dateUtil.curDayIndex,
            dateUtil.curTimeInSec
        ).map { alarms ->
                alarms.map { mappers.alarmCacheMapper.mapFromEntity(it) }
            }

    }

    override suspend fun getUpcomingAlarmIdsByRoutine(
        routineId: Long
    ): List<Long> {
        val dayRange =dateUtil.shortDayRange
        return daoService.alarmDao.selectUpcomingAlarmIds(dayRange, routineId)
    }

    override suspend fun getAlarmsById(alarmIds: List<Long>): List<Alarm> {
        return daoService.alarmDao.selectAlarms(alarmIds,dateUtil.curDayIndex,dateUtil.curTimeInSec).map { mappers.alarmCacheMapper.mapFromEntity(it) }
    }

    override suspend fun getAlarmById(alarmId: Long): Alarm {
        return mappers.alarmCacheMapper.mapFromEntity(
            daoService.alarmDao.getAlarmById(alarmId)
        )
    }

    override suspend fun updateProgram(program: Program): Int {
        return daoService.programDao.updateProgram(
            mappers.programCacheMapper.mapToEntity(program)
        )
    }

    override suspend fun insertRoutine(routineEntity: RoutineEntity,curRoutine: Long): Long {
//        if (curRoutine!=0L){
//            cancelCurrentRoutineAlarms(curRoutine)
//        }
        return daoService.routineDao.insertRoutine(routineEntity)
    }

    override fun getRoutine(routineId: Long): Flow<RoutineEntity> {
        return daoService.routineDao.getRoutine(routineId)
    }

    override fun getAllRoutines(): Flow<List<RoutineEntity>> {
        return daoService.routineDao.getAllRoutines()
    }

    override suspend fun updateRoutine(routineEntity: RoutineEntity): Int {
        Log.d(TAG, "updateRoutine: ${routineEntity.name}")
        return daoService.routineDao.updateRoutine(routineEntity)
    }

    override suspend fun deleteRoutine(routineEntity: RoutineEntity,curRoutine: Long): Int {
        Log.d(TAG, "deleteRoutine: delete id:${routineEntity.id}  cur:$curRoutine")
        if (routineEntity.id==curRoutine){
            cancelCurrentRoutineAlarms(curRoutine)
        }

        return daoService.routineDao.deleteRoutine(routineEntity)
    }

    override suspend fun insertProgram(program: Program): Long {

        return daoService.programDao.insertProgram(

            mappers.programCacheMapper.mapToEntity(program)

        )
    }

    override fun getProgram(id: Long): Flow<Program> {
        return daoService.programDao.selectProgram(id).map {
            mappers.programCacheMapper.mapFromEntity(it)
        }
    }

    override fun getAllPrograms(): Flow<List<Program>> {
        return daoService.programDao.getAllProgramsDistinctUntilChanged().map { entityList ->
            entityList.map { entity ->
                mappers.programCacheMapper.mapFromEntity(entity)
            }
        }
    }

    override fun getAllProgramsDetail(): Flow<List<ProgramDetail>> {
        return daoService.programDao.getProgramsSummary()
    }

    override fun getProgramDetail(id: Long): Flow<ProgramDetail> {
        return daoService.programDao.selectProgramDetail(id)

    }

    override suspend fun deleteAllPrograms(): Int {
        return daoService.programDao.deleteAllPrograms()
    }

    override suspend fun deleteProgram(program: ProgramDetail): Int {

        val alarmsToDelete = mutableListOf<Long>()

        program.schedules.forEach { s ->
            alarmsToDelete.addAll(s.alarms.map { it.id })
        }
        notificationManager.removeNotifications(alarmsToDelete)

        return daoService.programDao.deleteProgram(program.program)
    }

    override suspend fun undoDeletedProgram(program: ProgramDetail, curRoutine: Long): Long? {
        return try {
            val res = daoService.programDao.insertProgram(program.program)
            program.schedules.forEach {

                insertModifySchedule(
                    mappers.fullScheduleCacheMapper.mapFromEntity(it),
                    emptyList(),
                    SCHEDULE_REQUEST_NEW,
                    curRoutine
                )
            }
            res
        } catch (e: Throwable) {
            Log.d(TAG, "undoDeletedProgram: ${e.message}")
            null
        }

    }

    override fun getDaysOfWeek(chosenDay: Int): List<Day> {
        return dateUtil.getDaysOfWeek(chosenDay)
    }

    override suspend fun insertModifySchedule(
        schedule: Schedule,
        conflictedSchedule: List<Schedule>,
        requestType: String,
        curRoutine: Long
    ): Long? {
        val shortDayRange = dateUtil.shortDayRange
        val alarmIdsToCancel = mutableListOf<Long>()
        val alarmIdsToSchedule = mutableListOf<Long>()
        if (schedule.routineId == curRoutine) {
            alarmIdsToCancel.addAll(schedule.alarms.filter { it.id != 0L }.map { it.id })
        }

        conflictedSchedule.forEach { s ->
            if (s.routineId == curRoutine) {
                alarmIdsToCancel.addAll(s.alarms.map { it.id })
            }else{
                Log.d(TAG, "doWorkk newinsertModifySchedule: not same")
            }
        }

        val schedulesToDelete = selectSchedulesToDelete(
            conflictedSchedule,
            schedule
        )
//        Log.d(
//            TAG,
//            "insertSchedule: Target schedule : ${schedule.startDay} ${schedule.startInSec}--${schedule.endDay} ${schedule.endInSec} "
//        )


        val scheduleEntitiesToDelete = schedulesToDelete.map {
            mappers.fullScheduleCacheMapper.mapToEntity(it).schedule
        }



        schedulesToDelete.forEach {
            Log.d(
                TAG, "Schedules to delete: id:${it.id}- startDay=${it.startDay}" +
                        " startSec:${it.startInSec}-- endDay:${it.endDay} endSec:${it.endInSec}"
            )
        }
        val schedulesToUpdate = updateSchedules(
            conflictedSchedule.minus(schedulesToDelete),
            schedule
        )
        schedulesToUpdate.forEach {
            Log.d(
                TAG, "Schedules to update: id:${it.id}- startDay=${it.startDay}" +
                        " startSec:${it.startInSec}-- endDay:${it.endDay} endSec:${it.endInSec}"
            )
        }

        val scheduleEntitiesToUpdate = schedulesToUpdate.map {
            mappers.fullScheduleCacheMapper.mapToEntity(it).schedule
        }


        val alarmsToUpdate = mutableListOf<AlarmEntity>()
        schedulesToUpdate.forEach { s ->
            alarmsToUpdate.addAll(mappers.fullScheduleCacheMapper.mapToEntity(s).alarms)
        }


        val fullSchedule = mappers.fullScheduleCacheMapper.mapToEntity(schedule)


        val alarmsToInsert = fullSchedule.alarms.onEach { it.id = 0L }

        if (requestType == SCHEDULE_REQUEST_EDIT) {


            try {
                if (fullSchedule.todos.isNotEmpty() && fullSchedule.todos[0].programId != fullSchedule.program.id) {
                    val todos = fullSchedule.todos.onEach { it.programId = fullSchedule.program.id }
                    daoService.todoDao.updateTodos(todos)
                }
                notificationManager.removeNotifications(alarmIdsToCancel)

                val res = daoService.scheduleDao.updateTransaction(
                    scheduleEntity = fullSchedule.schedule,
                    schedulesToDelete = scheduleEntitiesToDelete,
                    schedulesToUpdate = scheduleEntitiesToUpdate,
                    alarmsToUpdate = alarmsToUpdate,
                    alarmsToInsert = alarmsToInsert
                )
                alarmIdsToSchedule.addAll(alarmsToUpdate.filter { it.routineId == curRoutine && it.day in shortDayRange }
                    .map { it.id })

                alarmsToUpdate.filter { it.routineId == curRoutine && it.day in shortDayRange }
                    .map { it.id }.forEach {
                    Log.d(TAG, "doWork new alarmsToUpdate: $it")
                }

                // notificationManager.setNotifications(alarmIdsToSchedule)

                return res

            } catch (e: Throwable) {
                Log.d(TAG, "insertModifiySchedule: error updating ")
                Log.d(TAG, "insertModifiySchedule: ${e.message}")
                return null
            }
        } else {
            Log.d(TAG, "insertModifySchedule: doWorkk new schedule")
            return try {
                notificationManager.removeNotifications(alarmIdsToCancel)
                val res = daoService.scheduleDao.insertTransaction(
                    scheduleEntity = fullSchedule.schedule,
                    schedulesToDelete = scheduleEntitiesToDelete,
                    schedulesToUpdate = scheduleEntitiesToUpdate,
                    alarmsToUpdate = alarmsToUpdate,
                    alarmsToInsert = fullSchedule.alarms,
                    todoEntities = fullSchedule.todos
                )
//                alarmIdsToSchedule.addAll(alarmsToUpdate.filter { it.routineId == curRoutine && it.day in shortDayRange }
//                    .map { it.id })
//                alarmIdsToSchedule.addAll(alarmsToInsert.filter { it.routineId == curRoutine && it.day in shortDayRange }
//                    .map { it.id })
              //  notificationManager.setNotifications(alarmIdsToSchedule)

                return res
            } catch (e: Throwable) {
                null
            }
        }

    }

    override suspend fun checkIfOverwrite(schedule: Schedule): List<Schedule> {
        val startDay = schedule.startDay
        val endDay = schedule.endDay
        Log.d(TAG, "checkIfOverwrite: target start $startDay")
        return getConflictedSchedules(
            daoService.scheduleDao.selectStartingTimes(startDay, endDay, schedule.routineId),
            schedule
        ).map { fullSchedule ->
            mappers.fullScheduleCacheMapper.mapFromEntity(fullSchedule)
        }.filter { it.id != schedule.id }

    }

    override fun getAllSchedules(routineId: Long): Flow<List<Schedule>> {
        return daoService.scheduleDao.selectSevenDaysSchedule(routineId).map { schedules ->
            schedules.map {
                mappers.fullScheduleCacheMapper.mapFromEntity(it)
            }
        }
    }


    override suspend fun deleteSchedule(schedule:Schedule): Int {
        val alarmsToDelete=schedule.alarms.map { it.id }

        notificationManager.removeNotifications(alarmsToDelete)

        return daoService.scheduleDao.deleteSchedule(
          mappers.fullScheduleCacheMapper.mapToEntity(
              schedule
          ).schedule
        )
    }

    override suspend fun getSchedule(scheduleId: Long): Schedule {
        return mappers.fullScheduleCacheMapper.mapFromEntity(
            daoService.scheduleDao.selectSchedule(
                scheduleId
            )
        )

    }

    override fun getDailySchedules(
        dayIndex: Int,
        routineId: Long,
        curTime: Int
    ): Flow<List<Schedule>> {
        return daoService.scheduleDao.selectDailyScheduleDistinct(dayIndex, curTime, routineId)
            .map { fullSchedules ->
                fullSchedules.map { mappers.fullScheduleCacheMapper.mapFromEntity(it) }
            }
    }

    override suspend fun insertTodo(todo: Todo): Long {
        return daoService.todoDao.insertTodo(
            mappers.todoCacheMapper.mapToEntity(todo)
        )
    }

    override suspend fun deleteTodo(todo: Todo): Int {
        return daoService.todoDao.deleteTodo(
            mappers.todoCacheMapper.mapToEntity(todo).also {
                Log.d(TAG, "deleteTodo: ${it.id}")
            }
        )
    }

    override suspend fun getScheduleTodos(scheduleId: Long): List<Todo> {
        return daoService.todoDao.getScheduleToDo(scheduleId).map {
            mappers.todoCacheMapper.mapFromEntity(it)
        }
    }

    override suspend fun deleteAndRetrieveTodos(todo: Todo): List<Todo> {
        return daoService.todoDao.deleteAndRetrieveTodos(
            mappers.todoCacheMapper.mapToEntity(todo)
        ).map {
            mappers.todoCacheMapper.mapFromEntity(it)
        }

    }

    override suspend fun insertAndRetrieveTodos(todo: Todo): List<Todo> {
        return daoService.todoDao.insertAndRetrieveTodos(
            mappers.todoCacheMapper.mapToEntity(todo)
        ).map {
            mappers.todoCacheMapper.mapFromEntity(it)
        }
    }

    override suspend fun updateAndRetrieveTodos(todo: Todo): List<Todo> {
        return daoService.todoDao.updateAndRetrieveTodos(
            mappers.todoCacheMapper.mapToEntity(todo)
        ).map {
            mappers.todoCacheMapper.mapFromEntity(it)
        }
    }

    override suspend fun updateListAndRetrieveTodos(
        todos: List<Todo>,
        scheduleId: Long
    ): List<Todo> {
        return daoService.todoDao.updateListAndRetrieveTodos(
            todos.map { mappers.todoCacheMapper.mapToEntity(it) },
            scheduleId
        ).map {
            mappers.todoCacheMapper.mapFromEntity(it)
        }
    }
}


