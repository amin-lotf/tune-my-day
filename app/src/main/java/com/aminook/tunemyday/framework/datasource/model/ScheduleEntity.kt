package com.aminook.tunemyday.framework.datasource.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules", foreignKeys = [
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["program_id"],
            onDelete = ForeignKey.CASCADE
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
    val programId: Int,
    @ColumnInfo(name = "alarm_id")
    val alarmId: Int?=null,
    val start: Long,
    val end: Long,
    val day: Int
) {

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
}