package com.aminook.tunemyday.framework.datasource.model

import androidx.room.Embedded
import androidx.room.Relation

data class DetailedSchedule(
    @Embedded
    val scheduleEntity: ScheduleEntity,

    @Relation(parentColumn = "id", entityColumn = "schedule_id")
    val  toDos:List<ToDoEntity>,

    @Relation(parentColumn = "program_id",entityColumn = "id")
    val program:ProgramEntity

)