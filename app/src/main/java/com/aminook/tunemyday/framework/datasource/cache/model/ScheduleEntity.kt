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
        )
    ]
)
data class ScheduleEntity(
    @ColumnInfo(name = "program_id", index = true)
    var programId: Long,
    var start: Int,
    var end: Int,
    var startDay: Int,
    var endDay: Int
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}


data class FullSchedule(
    @Embedded
    val schedule: ScheduleEntity,

    @Relation(
        parentColumn = "program_id",
        entityColumn = "id"
    )
    val program: ProgramEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "schedule_id")
    val alarms:List<AlarmEntity>

)

data class SchedulesPerDay(
    val day: Int,
    val schedules: List<FullSchedule>
)