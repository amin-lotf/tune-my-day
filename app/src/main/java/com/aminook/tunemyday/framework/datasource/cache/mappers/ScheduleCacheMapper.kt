package com.aminook.tunemyday.framework.datasource.cache.mappers

import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Time
import com.aminook.tunemyday.business.domain.util.EntityMapper
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleAndProgram
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

@Singleton
class ScheduleCacheMapper @Inject constructor(
    val programCacheMapper: ProgramCacheMapper
) : EntityMapper<ScheduleAndProgram, Schedule> {
    override fun mapFromEntity(entity: ScheduleAndProgram): Schedule {
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

            //TODO(map alarms and todos)

        }
    }

    override fun mapToEntity(domainModel: Schedule): ScheduleAndProgram {
        val scheduleEntity = ScheduleEntity(
            start = domainModel.startInSec,
            end = domainModel.endInSec,
            startDay = domainModel.startDay,
            endDay = domainModel.endDay, programId = domainModel.program?.id?:1
        ).apply {
            domainModel.id?.let {
                this.id=it
            }
        }
        return ScheduleAndProgram(scheduleEntity,programCacheMapper.mapToEntity(domainModel = domainModel.program!!))
    }

}