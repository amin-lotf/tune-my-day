package com.aminook.tunemyday.framework.datasource.cache.mappers

import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.business.domain.util.EntityMapper
import com.aminook.tunemyday.framework.datasource.cache.model.AlarmEntity
import javax.inject.Singleton

@Singleton
class AlarmCacheMapper:EntityMapper<AlarmEntity,Alarm> {
    override fun mapFromEntity(entity: AlarmEntity): Alarm {
        return Alarm(id = entity.id,programId = entity.programId, hourBefore = entity.hourBefore,minuteBefore = entity.minBefore)
    }

    override fun mapToEntity(domainModel: Alarm): AlarmEntity {
        return AlarmEntity(
            programId = domainModel.programId,
            scheduleId = domainModel.scheduleId,
            hourBefore = domainModel.hourBefore,
            minBefore = domainModel.minuteBefore,
            day = domainModel.day,
            programName = "",
            startInSec = 0
        ).apply {
            id=domainModel.id

        }
    }
}