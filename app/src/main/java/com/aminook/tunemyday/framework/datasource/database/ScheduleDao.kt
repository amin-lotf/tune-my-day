package com.aminook.tunemyday.framework.datasource.database

import android.content.LocusId
import androidx.room.*
import com.aminook.tunemyday.framework.datasource.model.ProgramEntity
import com.aminook.tunemyday.framework.datasource.model.ScheduleAndProgram
import com.aminook.tunemyday.framework.datasource.model.ScheduleEntity
import com.aminook.tunemyday.framework.datasource.model.SchedulesPerDay
import kotlinx.coroutines.flow.Flow


@Dao
interface ScheduleDao {

    @Transaction
    @Query("SELECT * FROM schedules")
     fun testRelation():Flow<List<ScheduleAndProgram>>

    @Transaction
    @Query("select * from schedules where day= :day")
    fun selectDailySchedule(day:Int):Flow<List<ScheduleAndProgram>>

    @Query("select * from schedules where id= :id")
    fun selectSchedule(id:Int):Flow<ScheduleEntity?>

    @Query("delete from schedules")
    fun deleteAllSchedules():Int


    @Query("select * from schedules ")
    fun selectSevenDaysSchedule():Flow<List<ScheduleAndProgram>>

    @Delete
    fun deleteSchedule(scheduleEntity: ScheduleEntity):Int

    @Insert
    fun insertSchedule(scheduleEntity: ScheduleEntity):Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateSchedule(scheduleEntity: ScheduleEntity):Int

}