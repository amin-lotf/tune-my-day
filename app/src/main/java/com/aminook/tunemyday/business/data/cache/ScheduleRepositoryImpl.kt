package com.aminook.tunemyday.business.data.cache

import android.util.Log
import com.aminook.tunemyday.business.data.util.getConflictedSchedules
import com.aminook.tunemyday.business.data.util.selectSchedulesToDelete
import com.aminook.tunemyday.business.data.util.updateSchedules
import com.aminook.tunemyday.business.domain.model.*
import com.aminook.tunemyday.business.domain.util.DayFactory
import com.aminook.tunemyday.framework.datasource.cache.database.*
import com.aminook.tunemyday.framework.datasource.cache.mappers.Mappers
import com.aminook.tunemyday.framework.datasource.cache.model.AlarmEntity
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_EDIT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
//TODO(use try catch for queries)
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

    override suspend fun insertModifiySchedule(
        schedule: Schedule,
        conflictedSchedule: List<Schedule>,
        requestType:String
    ): Long? {

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
        val alarmsToInsert=fullSchedule.alarms.onEach { it.id=0L }
        Log.d(TAG, "insertSchedule: alarm size: ${fullSchedule.alarms.size}")
        if (requestType== SCHEDULE_REQUEST_EDIT) {
            return try {
                daoService.scheduleDao.updateTransaction(
                    scheduleEntity = fullSchedule.schedule,
                    schedulesToDelete = scheduleEntitiesToDelete,
                    schedulesToUpdate = scheduleEntitiesToUpdate,
                    alarmsToUpdate = alarmsToUpdate,
                    alarmsToInsert = alarmsToInsert
                )
            } catch (e: Throwable) {
                Log.d(TAG, "insertModifiySchedule: error updating ")
                Log.d(TAG, "insertModifiySchedule: ${e.message}")
                null
            }
        }else{
            return try {
                daoService.scheduleDao.insertTransaction(
                    scheduleEntity = fullSchedule.schedule,
                    schedulesToDelete = scheduleEntitiesToDelete,
                    schedulesToUpdate = scheduleEntitiesToUpdate,
                    alarmsToUpdate = alarmsToUpdate,
                    alarmsToInsert = fullSchedule.alarms
                )
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
            daoService.scheduleDao.selectStartingTimes(startDay, endDay),
            schedule
        ).map {fullSchedule->
            mappers.scheduleCacheMapper.mapFromEntity(fullSchedule)
        }.filter { it.id!=schedule.id }

    }

    override fun getDailySchedules(): Flow<List<Schedule>> {
        return daoService.scheduleDao.selectSevenDaysSchedule().map {schedules->
          schedules.map {
              mappers.scheduleCacheMapper.mapFromEntity(it)
          }
        }
    }

    override suspend fun getUpcomingAlarms(startDay:Int,endDay:Int):List<Alarm> {
        Log.d(TAG, "getUpcomingAlarms: $startDay $endDay")
        val dayRange= mutableListOf<Int>()

        for (i in startDay..endDay){
            val day= if (i<7) i else i-7
            dayRange.add(day)

        }



        return daoService.alarmDao.selectUpcomingAlarms(dayRange)
            .map {
                mappers.alarmCacheMapper.mapFromEntity(it) }

    }

    override suspend fun deleteSchedule(scheduleId: Long): Int {
        return daoService.scheduleDao.deleteScheduleById(scheduleId)
    }

    override suspend fun getSchedule(scheduleId: Long): Schedule {
        return mappers.scheduleCacheMapper.mapFromEntity(daoService.scheduleDao.selectSchedule(scheduleId))

    }
}


