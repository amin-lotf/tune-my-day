package com.aminook.tunemyday.framework.datasource.model

import androidx.room.Embedded
import androidx.room.Relation

data class ToDosPerSchedule(
    @Embedded
    val scheduleEntity: ScheduleEntity,

    @Relation(parentColumn = "")
)