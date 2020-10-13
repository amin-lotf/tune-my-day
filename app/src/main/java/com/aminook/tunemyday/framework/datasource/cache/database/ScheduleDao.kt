package com.aminook.tunemyday.framework.datasource.cache.database

import androidx.room.*
import com.aminook.tunemyday.framework.datasource.cache.model.AlarmEntity
import com.aminook.tunemyday.framework.datasource.cache.model.FullSchedule
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleEntity
import com.aminook.tunemyday.framework.datasource.cache.model.TodoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged


@Dao
interface ScheduleDao {

    @Transaction
    @Query("SELECT * FROM schedules")
    fun testRelation(): Flow<List<FullSchedule>>

    @Transaction
    @Query("select * from schedules where startDay= :day and `end`>=:curTime  and routine_id=:routineId order by start")
    fun selectDailySchedule(day: Int, curTime:Int, routineId:Long): Flow<List<FullSchedule>>

    fun selectDailyScheduleDistinct(day: Int,startTime:Int, routineId:Long): Flow<List<FullSchedule>>{
        return selectDailySchedule(day,startTime,routineId).distinctUntilChanged()
    }

    @Transaction
    @Query("select * from schedules where id= :id")
    suspend fun selectSchedule(id: Long): FullSchedule

    @Query("delete from schedules where routine_id=:routineId")
    fun deleteAllSchedules(routineId:Long): Int

    @Transaction
    @Query("select * from schedules where routine_id=:routineId and  (startDay=:startDay or endDay=:startDay or endDay=:endDay or startDay=:endDay) order by start")
    suspend fun selectStartingTimes(startDay: Int,endDay:Int, routineId:Long): List<FullSchedule>

    @Transaction
    @Query("select * from schedules where routine_id=:routineId   order by start")
    fun selectSevenDaysSchedule( routineId:Long): Flow<List<FullSchedule>>

    @Delete
    suspend fun deleteSchedule(scheduleEntity: ScheduleEntity): Int

    @Query("delete from schedules where  id=:scheduleId")
    suspend fun deleteScheduleById(scheduleId:Long): Int

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

    @Query("delete from alarms where schedule_id=:scheduleId")
    suspend fun deleteScheduleAlarms(scheduleId:Long)

    @Transaction
    @Insert
    suspend fun insertTodos(todoEntities: List<TodoEntity>)

    @Transaction
    suspend fun insertTransaction(
        scheduleEntity: ScheduleEntity,
        schedulesToDelete:List<ScheduleEntity>,
        schedulesToUpdate:List<ScheduleEntity>,
        alarmsToUpdate:List<AlarmEntity>,
        alarmsToInsert:List<AlarmEntity>,
        todoEntities: List<TodoEntity>
    ):Long{
        val id=insertSchedule(scheduleEntity)
        deleteSchedules(schedulesToDelete)
        updateSchedules(schedulesToUpdate)
        updateAlarms(alarmsToUpdate)
        if(todoEntities.size>0){
            insertTodos(todoEntities)
        }
        alarmsToInsert.onEach {
            it.scheduleId=id
        }
        insertAlarms(alarmsToInsert)
        return id
    }

    @Transaction
    suspend fun updateTransaction(
        scheduleEntity: ScheduleEntity,
        schedulesToDelete:List<ScheduleEntity>,
        schedulesToUpdate:List<ScheduleEntity>,
        alarmsToUpdate:List<AlarmEntity>,
        alarmsToInsert:List<AlarmEntity>
    ):Long{
        val id=updateSchedule(scheduleEntity)
        deleteScheduleAlarms(scheduleEntity.id)
        deleteSchedules(schedulesToDelete)
        updateSchedules(schedulesToUpdate)
        updateAlarms(alarmsToUpdate)
        alarmsToInsert.onEach {
            it.scheduleId=scheduleEntity.id
        }
        insertAlarms(alarmsToInsert)
        return id.toLong()
    }
}