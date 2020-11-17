package com.aminook.tunemyday.business.data.cache

import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import com.aminook.tunemyday.business.data.util.CacheConstants.TODO_CONVERSION_FINISHED
import com.aminook.tunemyday.business.data.util.CacheConstants.TODO_CONVERSION_UNFINISHED
import com.aminook.tunemyday.business.data.util.getConflictedSchedules
import com.aminook.tunemyday.business.data.util.selectSchedulesToDelete
import com.aminook.tunemyday.business.data.util.updateSchedules
import com.aminook.tunemyday.business.domain.model.*
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.di.DataStoreNotification
import com.aminook.tunemyday.framework.datasource.cache.database.*
import com.aminook.tunemyday.framework.datasource.cache.mappers.Mappers
import com.aminook.tunemyday.framework.datasource.cache.model.*
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_EDIT
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_NEW
import com.aminook.tunemyday.util.SOUND_SETTINGS
import com.aminook.tunemyday.util.VIBRATE_SETTINGS
import com.aminook.tunemyday.worker.AppNotificationManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

//TODO(use try catch for queries)
@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    val daoService: DaoService,
    val mappers: Mappers,
    val dateUtil: DateUtil,
    val scheduleDatabase: ScheduleDatabase,
    val appNotificationManager: AppNotificationManager,
    @DataStoreNotification
    val dataStoreNotification: DataStore<Preferences>
) : ScheduleRepository {
    //private val TAG = "aminjoon"

    override suspend fun updateNotificationSettings(notificationSettings: NotificationSettings) {

        try {
            dataStoreNotification.edit { settings ->
                settings[VIBRATE_SETTINGS] = notificationSettings.shouldVibrate
                settings[SOUND_SETTINGS] = notificationSettings.shouldRing
            }
        }catch (exception:Throwable){
            FirebaseCrashlytics.getInstance().recordException(exception)
        }

    }

    override fun getNotificationSettings(): Flow<NotificationSettings> {
        return dataStoreNotification.data
            .catch { exception ->
                if (exception is IOException) {
                    FirebaseCrashlytics.getInstance().recordException(exception)
                    emit(
                        emptyPreferences()
                    )
                } else {
                    FirebaseCrashlytics.getInstance().recordException(exception)
                    throw  exception
                }
            }
            .map { settings ->
                val shouldVibrate = settings[VIBRATE_SETTINGS] ?: true
                val shouldRing = settings[SOUND_SETTINGS] ?: true
                NotificationSettings(shouldRing, shouldVibrate)
            }
    }

    override suspend fun getNotificationScheduleByAlarmId(alarmId: Long): Schedule {
        val entity = daoService.alarmDao.getAlarmNotificationById(alarmId)
        val detailedScheduleEntity = entity.detailedSchedule.apply {
            alarms = listOf(entity.alarm)
        }

        return mappers.detailedScheduleCacheMapper.mapFromEntity(detailedScheduleEntity)
    }

    override suspend fun scheduleUpcomingAlarmsByRoutine(routineId: Long): Boolean {
        val dayRange = dateUtil.shortDayRange
        return try {
            val alarms = daoService.alarmDao.selectUpcomingAlarms(dayRange, routineId)
            appNotificationManager.addAlarms(alarms)
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }

    }

    override suspend fun rescheduleAlarmsForNewRoutine(
        prevRoutineId: Long,
        currentRoutineId: Long
    ): Boolean {
        return try {
            val res = cancelCurrentRoutineAlarms(prevRoutineId)
            if (!res) {
                return false
            }
            return scheduleCurrentRoutineAlarms(currentRoutineId)
        } catch (e: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }


    override suspend fun cancelCurrentRoutineAlarms(routineId: Long): Boolean {
        val dayRange = dateUtil.shortDayRange

        val alarmIds = daoService.alarmDao.selectUpcomingAlarmIds(
            dayRange,
            routineId
        )
        return try {
            appNotificationManager.cancelAlarms(alarmIds)
            true
        } catch (e: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }


    override suspend fun scheduleCurrentRoutineAlarms(routineId: Long): Boolean {
        val dayRange = dateUtil.shortDayRange

        val alarms = daoService.alarmDao.selectUpcomingAlarms(dayRange, routineId)
        return try {
            appNotificationManager.addAlarms(alarms)
            true
        } catch (e: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }


    override suspend fun getAlarmsById(alarmIds: List<Long>): List<Alarm> {
        return daoService.alarmDao.selectAlarms(
            alarmIds,
            dateUtil.curDayIndex,
            dateUtil.curTimeInSec
        ).map { mappers.alarmCacheMapper.mapFromEntity(it) }
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

    override suspend fun insertRoutine(routineEntity: RoutineEntity, curRoutine: Long): Long {
        return daoService.routineDao.insertRoutine(routineEntity)
    }

    override fun getRoutine(routineId: Long): Flow<RoutineEntity> {
        return daoService.routineDao.getRoutine(routineId)
    }

    override fun getAllRoutines(): Flow<List<RoutineEntity>> {
        return daoService.routineDao.getAllRoutines()
    }

    override suspend fun updateRoutine(routineEntity: RoutineEntity): Int {
        return daoService.routineDao.updateRoutine(routineEntity)
    }

    override suspend fun deleteRoutine(routineId: Long, curRoutine: Long): Int {
        if (routineId == curRoutine) {
            cancelCurrentRoutineAlarms(curRoutine)
        }
        return daoService.routineDao.deleteRoutine(routineId)
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
        appNotificationManager.cancelAlarms(alarmsToDelete)
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
            FirebaseCrashlytics.getInstance().recordException(e)
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
        val alarmsToSchedule = mutableListOf<AlarmEntity>()
        //val alarmIdsToSchedule = mutableListOf<Long>()
        if (schedule.id != 0L && schedule.routineId == curRoutine) {
            val prevAlarmIds = daoService.alarmDao.getAlarmIdsByScheduleId(schedule.id)
            alarmIdsToCancel.addAll(prevAlarmIds)
        }

        conflictedSchedule.forEach { s ->
            if (s.routineId == curRoutine) {
                alarmIdsToCancel.addAll(s.alarms.map { it.id })
            }
        }

        val schedulesToDelete = selectSchedulesToDelete(conflictedSchedule, schedule)

        val scheduleEntitiesToDelete = schedulesToDelete.map {
            mappers.fullScheduleCacheMapper.mapToEntity(it).schedule
        }

        val schedulesToUpdate = updateSchedules(
            conflictedSchedule.minus(schedulesToDelete),
            schedule
        )

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
                appNotificationManager.cancelAlarms(alarmIdsToCancel)
                val res = daoService.scheduleDao.updateTransaction(
                    scheduleEntity = fullSchedule.schedule,
                    schedulesToDelete = scheduleEntitiesToDelete,
                    schedulesToUpdate = scheduleEntitiesToUpdate,
                    alarmsToUpdate = alarmsToUpdate,
                    alarmsToInsert = alarmsToInsert
                )

                alarmsToSchedule.addAll(alarmsToUpdate.filter { it.routineId == curRoutine && it.day in shortDayRange })

                val newScheduleAlarms = daoService.alarmDao.selectScheduleUpcomingAlarms(
                    shortDayRange,
                    fullSchedule.schedule.id,
                    curRoutine
                )
                alarmsToSchedule.addAll(newScheduleAlarms)
                appNotificationManager.addAlarms(alarmsToSchedule)
                return res

            } catch (e: Throwable) {
                FirebaseCrashlytics.getInstance().recordException(e)
                return null
            }
        } else {
            return try {
                appNotificationManager.cancelAlarms(alarmIdsToCancel)
                val res = daoService.scheduleDao.insertTransaction(
                    scheduleEntity = fullSchedule.schedule,
                    schedulesToDelete = scheduleEntitiesToDelete,
                    schedulesToUpdate = scheduleEntitiesToUpdate,
                    alarmsToUpdate = alarmsToUpdate,
                    alarmsToInsert = fullSchedule.alarms,
                    todoEntities = fullSchedule.todos
                )

                val newScheduleAlarms = daoService.alarmDao.selectScheduleUpcomingAlarms(
                    shortDayRange,
                    res,
                    curRoutine
                )
                alarmsToSchedule.addAll(newScheduleAlarms)
                appNotificationManager.addAlarms(newScheduleAlarms)

                return res
            } catch (e: Throwable) {
                FirebaseCrashlytics.getInstance().recordException(e)
                null
            }
        }

    }

    override suspend fun checkIfOverwrite(schedule: Schedule): List<Schedule> {
        val startDay = schedule.startDay
        val endDay = schedule.endDay
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

    override suspend fun deleteSchedule(schedule: Schedule): Int {
        val alarmsToDelete = schedule.alarms.map { it.id }

        appNotificationManager.cancelAlarms(alarmsToDelete)

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

    override suspend fun getDetailedSchedule(scheduleId: Long): Schedule {
        return mappers.detailedScheduleCacheMapper.mapFromEntity(
            daoService.scheduleDao.selectDetailedSchedule(
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
                val tmp =
                    fullSchedules.map { mappers.fullScheduleCacheMapper.mapFromEntity(it) } as MutableList

                if (tmp.size > 1 && tmp.last().startDay != dayIndex && tmp.last().startDay == 6) {
                    val lst = tmp.removeLast()
                    if (lst.endInSec > curTime) {
                        tmp.add(0, lst)
                    }
                }
                tmp
            }
    }

    private fun processTodos(todos: List<TodoEntity>, conversionType: String): List<Todo> {
        return when (conversionType) {
            TODO_CONVERSION_FINISHED -> {
                val mapped = todos.map { mappers.todoCacheMapper.mapFromEntity(it) }
                mapped.filter {
                    (it.isDone &&
                            (it.lastChecked != dateUtil.currentDayInInt ||
                                    it.lastChecked != dateUtil.currentDayInInt - 1 ||
                                    it.lastChecked != dateUtil.currentDayInInt + 1))
                }
            }
            TODO_CONVERSION_UNFINISHED -> {
                val mapped = todos.map { mappers.todoCacheMapper.mapFromEntity(it) }
                mapped.filter {
                    !it.isDone ||
                            (it.lastChecked != dateUtil.currentDayInInt &&
                                    it.lastChecked != dateUtil.currentDayInInt - 1 &&
                                    it.lastChecked != dateUtil.currentDayInInt + 1)
                }.onEach { it.isDone = false }
            }
            else -> {
                todos.map { mappers.todoCacheMapper.mapFromEntity(it) }
            }
        }
    }

    override suspend fun insertTodo(todo: Todo): Long {
        return daoService.todoDao.insertTodo(
            mappers.todoCacheMapper.mapToEntity(todo)
        )
    }

    override suspend fun updateTodos(todos: List<Todo>): Int {
        return daoService.todoDao.updateTodos(todos.map { mappers.todoCacheMapper.mapToEntity(it) })
    }

    override suspend fun updateTodo(todo: Todo): Int {
        return daoService.todoDao.updateTodo(mappers.todoCacheMapper.mapToEntity(todo))
    }

    override suspend fun deleteTodo(todo: Todo): Int {
        return daoService.todoDao.deleteTodo(
            mappers.todoCacheMapper.mapToEntity(todo)
        )
    }

    override fun getScheduleTodos(scheduleId: Long): Flow<List<Todo>> {
        return daoService.todoDao.getScheduleToDoFlow(scheduleId).map { todoEntities ->
            todoEntities.map { mappers.todoCacheMapper.mapFromEntity(it) }
        }
    }

    override suspend fun deleteAndRetrieveTodos(todo: Todo): List<Todo> {
        return processTodos(
            daoService.todoDao.deleteAndRetrieveTodos(
                mappers.todoCacheMapper.mapToEntity(todo)
            ),
            TODO_CONVERSION_UNFINISHED
        )
    }

    override suspend fun insertAndRetrieveTodos(todo: Todo): List<Todo> {
        return processTodos(
            daoService.todoDao.insertAndRetrieveTodos(
                mappers.todoCacheMapper.mapToEntity(todo)
            ),
            TODO_CONVERSION_UNFINISHED
        )
    }

    override suspend fun updateAndRetrieveTodos(todo: Todo): List<Todo> {
        return processTodos(
            daoService.todoDao.updateAndRetrieveTodos(
                mappers.todoCacheMapper.mapToEntity(todo)
            ),
            TODO_CONVERSION_UNFINISHED
        )
    }

    override suspend fun updateListAndRetrieveTodos(
        todos: List<Todo>,
        scheduleId: Long
    ): List<Todo> {
        return processTodos(
            daoService.todoDao.updateListAndRetrieveTodos(
                todos.map { mappers.todoCacheMapper.mapToEntity(it) },
                scheduleId
            ),
            TODO_CONVERSION_UNFINISHED
        )

    }
}


