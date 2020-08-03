package com.aminook.tunemyday.framework.datasource.database

import androidx.room.*
import com.aminook.tunemyday.framework.datasource.model.ProgramEntity
import com.aminook.tunemyday.framework.datasource.model.ScheduleAndProgram
import com.aminook.tunemyday.framework.datasource.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules")
     fun testRelation():Flow<List<ScheduleAndProgram>>

    @Query("select * from schedules where day= :day")
    fun selectDailySchedule(day:Int):Flow<List<ScheduleAndProgram>>

    @Query("select * from schedules where id= :id")
    fun selectSchedule(id:Int):Flow<ScheduleEntity?>

    @Query("delete from schedules")
    suspend fun deleteAllSchedules()


    @Delete
    fun deleteSchedule(scheduleEntity: ScheduleEntity):Flow<Int>

    @Insert
    fun insertSchedule(scheduleEntity: ScheduleEntity):Flow<Long>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateSchedule(scheduleEntity: ScheduleEntity):Flow<Int>

}