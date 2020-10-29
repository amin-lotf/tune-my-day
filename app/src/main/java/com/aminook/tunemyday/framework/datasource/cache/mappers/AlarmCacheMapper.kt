package com.aminook.tunemyday.framework.datasource.cache.mappers

import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.business.domain.util.EntityMapper
import com.aminook.tunemyday.framework.datasource.cache.model.AlarmEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmCacheMapper @Inject  constructor():EntityMapper<AlarmEntity,Alarm>  {
    override fun mapFromEntity(entity: AlarmEntity?): Alarm {
        return if (entity!=null){
            Alarm(
                id = entity.id,
                scheduleId = entity.scheduleId,
                programId = entity.programId,
                routineId = entity.routineId,
                hourBefore = entity.hourBefore,
                minuteBefore = entity.minBefore,
                day = entity.day,
                startInSec = entity.startInSec)
        }else{
            Alarm()
        }
    }

    override fun mapToEntity(domainModel: Alarm): AlarmEntity {
        return AlarmEntity(
            programId = domainModel.programId,
            scheduleId = domainModel.scheduleId,
            routineId = domainModel.routineId,
            hourBefore = domainModel.hourBefore,
            minBefore = domainModel.minuteBefore,
            day = domainModel.day,
            startInSec = domainModel.startInSec
        ).apply {
            if(domainModel.id!=0L) {
                id = domainModel.id
            }

        }
    }
}