package com.aminook.tunemyday.framework.datasource.cache.model

import androidx.room.*

@Entity(
    tableName = "schedules", foreignKeys = [
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["program_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AlarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["alarm_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ScheduleEntity(
    @ColumnInfo(name = "program_id", index = true)
    var programId: Int,
    @ColumnInfo(name = "alarm_id",index = true)
    var alarmId: Int?=null,
    var start: Int,
    var end: Int,
    var startDay: Int,
    var endDay:Int
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}


data class ScheduleAndProgram(
    @Embedded
    val schedule: ScheduleEntity,

    @Relation(
        parentColumn = "program_id",
        entityColumn = "id"
    )
    val program: ProgramEntity
)

data class SchedulesPerDay(
    val day:Int,
    val schedules:List<ScheduleAndProgram>
)