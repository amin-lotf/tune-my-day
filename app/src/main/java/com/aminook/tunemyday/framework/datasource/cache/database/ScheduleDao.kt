package com.aminook.tunemyday.framework.datasource.cache.database

import androidx.room.*
import com.aminook.tunemyday.framework.datasource.cache.model.AlarmEntity
import com.aminook.tunemyday.framework.datasource.cache.model.FullSchedule
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ScheduleDao {

    @Transaction
    @Query("SELECT * FROM schedules")
    fun testRelation(): Flow<List<FullSchedule>>

    @Transaction
    @Query("select * from schedules where startDay= :day order by start")
    fun selectDailySchedule(day: Int): Flow<List<FullSchedule>>

    @Query("select * from schedules where id= :id")
    fun selectSchedule(id: Int): Flow<ScheduleEntity?>

    @Query("delete from schedules")
    fun deleteAllSchedules(): Int

    @Query("select * from schedules where startDay=:startDay or endDay=:startDay or endDay=:endDay or startDay=:endDay order by start")
    suspend fun selectStartingTimes(startDay: Int,endDay:Int): List<FullSchedule>


    @Query("select * from schedules   order by start")
    fun selectSevenDaysSchedule(): Flow<List<FullSchedule>>

    @Delete
    suspend fun deleteSchedule(scheduleEntity: ScheduleEntity): Int

    @Insert
    suspend fun insertSchedule(scheduleEntity: ScheduleEntity): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateSchedule(scheduleEntity: ScheduleEntity): Int

    @Update
    suspend fun updateSchedules(schedules:List<ScheduleEntity>)

    @Delete
    suspend fun deleteSchedules(schedules: List<ScheduleEntity>)

    @Update
    suspend fun updateAlarms(alarms:List<AlarmEntity>)

    @Insert
    suspend fun insertAlarms(alarms: List<AlarmEntity>)

    @Transaction
    suspend fun insertTransaction(
        scheduleEntity: ScheduleEntity,
        schedulesToDelete:List<ScheduleEntity>,
        schedulesToUpdate:List<ScheduleEntity>,
        alarmsToUpdate:List<AlarmEntity>,
        alarmsToInsert:List<AlarmEntity>
    ):Long{
        deleteSchedules(schedulesToDelete)
        updateSchedules(schedulesToUpdate)
        return insertSchedule(scheduleEntity)
    }
}