package com.aminook.tunemyday.framework.datasource.cache.mappers

import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Time
import com.aminook.tunemyday.business.domain.util.EntityMapper
import com.aminook.tunemyday.framework.datasource.cache.model.FullSchedule
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class FullScheduleCacheMapper @Inject constructor(
    val programCacheMapper: ProgramCacheMapper,
    val alarmCacheMapper: AlarmCacheMapper
) : EntityMapper<FullSchedule, Schedule> {
    override fun mapFromEntity(entity: FullSchedule): Schedule {
        val program = programCacheMapper.mapFromEntity(entity.program)
        return Schedule(
            id = entity.schedule.id,
            startDay = entity.schedule.startDay,
            program = program
        ).apply {
            val startHour = (entity.schedule.start / 3600) % 24
            val startMinute = (entity.schedule.start - (entity.schedule.start / 3600) * 3600) / 60
            this.startTime = Time(startHour, startMinute)

            val endHour = (entity.schedule.end / 3600) % 24
            val endMinute = (entity.schedule.end - (entity.schedule.end / 3600) * 3600) / 60
            this.endTime = Time(endHour, endMinute)

            this.alarms.addAll(entity.alarms.map { alarmCacheMapper.mapFromEntity(it) })

        }
    }

    override fun mapToEntity(domainModel: Schedule): FullSchedule {
        val scheduleEntity = ScheduleEntity(
            start = domainModel.startInSec,
            end = domainModel.endInSec,
            startDay = domainModel.startDay,
            endDay = domainModel.endDay,
            programId = domainModel.program?.id ?: 1
        ).apply {
            if (domainModel.id!=0L){
                this.id = domainModel.id
            }

        }
        val alarms = domainModel.alarms.map {alarm->
            alarmCacheMapper.mapToEntity(alarm).apply {
                this.scheduleId = domainModel.id

                domainModel.program?.let { program ->
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



        return FullSchedule(
            scheduleEntity,
            programCacheMapper.mapToEntity(domainModel = domainModel.program!!),
            alarms
        )
    }

}