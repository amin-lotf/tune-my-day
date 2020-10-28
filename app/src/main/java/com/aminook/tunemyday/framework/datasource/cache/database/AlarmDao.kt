package com.aminook.tunemyday.framework.datasource.cache.database

import androidx.room.*
import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.framework.datasource.cache.model.AlarmEntity
import com.aminook.tunemyday.framework.datasource.cache.model.NotificationAlarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface AlarmDao {

    @Query("select id from alarms where day in(:days) and routine_id=:routineId")
    suspend fun selectUpcomingAlarmIds(days:List<Int>,routineId:Long):List<Long>

    //@Query("select * from alarms where routine_id=:routineId and day in(:days) and ((day=:curDay and startInSec>:start) or day>:curDay)")
    @Query("select * from alarms where day in(:days) and routine_id=:routineId")
    suspend fun selectUpcomingAlarms(days:List<Int>,routineId:Long):List<AlarmEntity>


    @Query("select id from alarms where day in(:days)  and schedule_id=:scheduleId and routine_id=:routineId")
    suspend fun selectScheduleUpcomingAlarmIds(days:List<Int>,scheduleId:Long,routineId:Long):List<Long>

    @Query("select * from alarms where day in(:days)  and schedule_id=:scheduleId and routine_id=:routineId")
    suspend fun selectScheduleUpcomingAlarms(days:List<Int>,scheduleId:Long,routineId:Long):List<AlarmEntity>



    @Query("select * from alarms where id in(:alarmIds) and ((day=:curDay and startInSec>:start) or day>:curDay)")
    suspend fun selectAlarms(alarmIds:List<Long>,curDay:Int,start:Int):List<AlarmEntity>

    @Query("select * from alarms where id=:alarmId")
    suspend fun getAlarmById(alarmId:Long):AlarmEntity

    @Query("select * from alarms where id=:alarmId")
    suspend fun getAlarmNotificationById(alarmId:Long):NotificationAlarm

    @Query("select id from alarms where schedule_id=:scheduleId")
    suspend fun getAlarmIdsByScheduleId(scheduleId:Long):List<Long>
}