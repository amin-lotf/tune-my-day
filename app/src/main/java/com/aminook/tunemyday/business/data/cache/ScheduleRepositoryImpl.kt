package com.aminook.tunemyday.business.data.cache

import android.util.Log
import com.aminook.tunemyday.business.data.util.getConflictedSchedules
import com.aminook.tunemyday.business.data.util.selectSchedulesToDelete
import com.aminook.tunemyday.business.data.util.updateSchedules
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.DayFactory
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.datasource.cache.database.*
import com.aminook.tunemyday.framework.datasource.cache.mappers.Mappers
import com.aminook.tunemyday.framework.datasource.cache.model.AlarmEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    val daoService: DaoService,
    val mappers: Mappers,
    val dayFactory: DayFactory,
    val scheduleDatabase: ScheduleDatabase
) : ScheduleRepository {
    private val TAG = "aminjoon"
    override suspend fun updateProgram(program: Program): Int {
        return daoService.programDao.updateProgram(
            mappers.programCacheMapper.mapToEntity(program)
        )
    }

    override suspend fun insertProgram(program: Program): Long {

        return daoService.programDao.insertProgram(

            mappers.programCacheMapper.mapToEntity(program)

        )
    }

    override fun getAllPrograms(): Flow<List<Program>> {
        return daoService.programDao.getAllProgramsDistinctUntilChanged().map { entityList ->
            entityList.map { entity ->
                mappers.programCacheMapper.mapFromEntity(entity)
            }
        }
    }

    override fun selectProgram(id: Int): Flow<Program?> {
        return daoService.programDao.selectProgram(id).map { entity ->
            entity?.let {
                mappers.programCacheMapper.mapFromEntity(it)
            }
        }
    }

    override suspend fun deleteAllPrograms(): Int {
        return daoService.programDao.deleteAllPrograms()
    }

    override suspend fun deleteProgram(program: Program): Int {
        return daoService.programDao.deleteProgram(
            mappers.programCacheMapper.mapToEntity(program)
        )
    }

    override fun getDaysOfWeek(chosenDay: Int): List<Day> {
        return dayFactory.getDaysOfWeek(chosenDay)
    }

    override suspend fun insertSchedule(
        schedule: Schedule,
        conflictedSchedule: List<Schedule>
    ): Long {
        val conflictedEntitySchedule = conflictedSchedule.map {
            mappers.scheduleCacheMapper.mapToEntity(it).schedule
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
            mappers.scheduleCacheMapper.mapToEntity(it).schedule
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
            mappers.scheduleCacheMapper.mapToEntity(it).schedule
        }

        val alarmsToUpdate= mutableListOf<AlarmEntity>()
        schedulesToUpdate.forEach {
            alarmsToUpdate.addAll(mappers.scheduleCacheMapper.mapToEntity(it).alarms)
        }
        val fullSchedule=mappers.scheduleCacheMapper.mapToEntity(schedule)
        return daoService.scheduleDao.insertTransaction(
            scheduleEntity = fullSchedule.schedule,
            schedulesToDelete = scheduleEntitiesToDelete,
            schedulesToUpdate = scheduleEntitiesToUpdate,
            alarmsToUpdate =alarmsToUpdate,
            alarmsToInsert = fullSchedule.alarms
        )

    }

    override suspend fun checkIfOverwrite(schedule: Schedule): List<Schedule> {
        val startDay = schedule.startDay
        val endDay = schedule.endDay
        Log.d(TAG, "checkIfOverwrite: target start $startDay")
        return getConflictedSchedules(
            daoService.scheduleDao.selectStartingTimes(startDay, endDay),
            schedule
        ).map {
            mappers.scheduleCacheMapper.mapFromEntity(it)
        }

    }

    override fun getDailySchedules(): Flow<List<Schedule>> {
        return daoService.scheduleDao.selectSevenDaysSchedule().map {schedules->
          schedules.map {
              mappers.scheduleCacheMapper.mapFromEntity(it)
          }
        }
    }
}


