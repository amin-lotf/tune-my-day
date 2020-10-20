package com.aminook.tunemyday.framework.datasource.cache.mappers

import android.util.Log
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Time
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.domain.util.EntityMapper
import com.aminook.tunemyday.framework.datasource.cache.model.FullSchedule
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class FullScheduleCacheMapper @Inject constructor(
    val programCacheMapper: ProgramCacheMapper,
    val alarmCacheMapper: AlarmCacheMapper,
    val todoCacheMapper: TodoCacheMapper,
    val dateUtil: DateUtil
) : EntityMapper<FullSchedule, Schedule> {
    override fun mapFromEntity(entity: FullSchedule?): Schedule {
       try {
            if (entity != null) {
                val program = programCacheMapper.mapFromEntity(entity.program)
                return Schedule(
                    id = entity.schedule.id,
                    startDay = entity.schedule.startDay,
                    program = program,
                    routineId = entity.schedule.routineId
                ).apply {
                    val startHour = (entity.schedule.start / 3600) % 24
                    val startMinute =
                        (entity.schedule.start - (entity.schedule.start / 3600) * 3600) / 60
                    this.startTime = Time(startHour, startMinute)

                    val endHour = (entity.schedule.end / 3600) % 24
                    val endMinute = (entity.schedule.end - (entity.schedule.end / 3600) * 3600) / 60
                    this.endTime = Time(endHour, endMinute)

                    this.alarms.addAll(entity.alarms.map { alarmCacheMapper.mapFromEntity(it) })
 //                   this.unfinishedTodos.addAll(entity.todos.sortedBy { it.priorityIndex }.map {  todoCacheMapper.mapFromEntity(it)})

                    entity.todos.sortedBy { it.priorityIndex }.onEach {
                        if (
                            !it.isDone ||
                            (it.lastChecked!=dateUtil.currentDayInInt &&
                                    it.lastChecked!=dateUtil.currentDayInInt-1 &&
                                    it.lastChecked!=dateUtil.currentDayInInt+1)
                        ){
                            it.isDone=false
                            unfinishedTodos.add(todoCacheMapper.mapFromEntity(it))
                        }else{
                            finishedTodos.add(todoCacheMapper.mapFromEntity(it))
                        }
                    }

//                    this.unfinishedTodos.addAll(
//                        entity.todos.map { todoCacheMapper.mapFromEntity(it) }.sortedWith(
//                            compareBy({ it.isDone }, { it.priorityIndex })
//                        )
//                    )

                    if (this.alarms.size > 0) {
                        this.hasAlarm = true
                    }
                    if (this.unfinishedTodos.size > 0) {
                        this.hasToDo = true
                    }

                }
            } else {
                return Schedule()
            }
        }
        catch (e:Throwable){
            Log.d("aminjoon", "mapFromEntity: error in mapper ")
            e.printStackTrace()
            return Schedule()
        }
    }

    override fun mapToEntity(domainModel: Schedule): FullSchedule {
        val scheduleEntity = ScheduleEntity(
            start = domainModel.startInSec,
            end = domainModel.endInSec,
            startDay = domainModel.startDay,
            endDay = domainModel.endDay,
            programId = domainModel.program.id,
            routineId = domainModel.routineId
        ).apply {
            if (domainModel.id!=0L){
                this.id = domainModel.id
            }

        }
        val alarms = domainModel.alarms.map {alarm->
            alarmCacheMapper.mapToEntity(alarm).apply {
                this.scheduleId = domainModel.id

                domainModel.program.let { program ->
                    this.programId = program.id
                    this.programName=program.name
                }

                val alarmStart=domainModel.startInSec-alarm.hourBefore*3600-alarm.minuteBefore*60
                if (alarmStart<0){
                    this.day=6
                    this.startInSec=604800-abs(alarmStart)

                }else{
                    this.day=  alarmStart/86400
                    this.startInSec=alarmStart
                }
            }
        }

        val todos=domainModel.unfinishedTodos.map { todoCacheMapper.mapToEntity(it) }

        return FullSchedule(
            scheduleEntity,
            programCacheMapper.mapToEntity(domainModel = domainModel.program),
            alarms,
            todos
        )
    }

}