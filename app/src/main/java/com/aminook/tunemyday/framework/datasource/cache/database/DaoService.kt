package com.aminook.tunemyday.framework.datasource.cache.database

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DaoService @Inject constructor(
    val scheduleDao: ScheduleDao,
    val programDao: ProgramDao,
    val alarmDao: AlarmDao,
    val todoDao: TodoDao,
    val routineDao: RoutineDao
)