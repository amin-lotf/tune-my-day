package com.aminook.tunemyday.framework.datasource.database

import androidx.room.*
import com.aminook.tunemyday.framework.datasource.model.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAlarm(alarmEntity: AlarmEntity):Flow<Long>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateAlarm(alarmEntity: AlarmEntity):Flow<Int>

    @Delete
    fun deleteAlarm(alarmEntity: AlarmEntity):Flow<Int>

    @Query("select * from alarms where day= :day")
    fun selectDailyAlarms(day:Int):Flow<List<AlarmEntity>>

    @Query("select * from alarms where schedule_id= :scheduleId")
    fun selectScheduleAlarm(scheduleId:Int):Flow<AlarmEntity?>
}