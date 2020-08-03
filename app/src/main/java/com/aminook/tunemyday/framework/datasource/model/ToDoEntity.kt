package com.aminook.tunemyday.framework.datasource.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "todos",
    foreignKeys = [
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["program_id"],
            onDelete = ForeignKey.CASCADE,
            ), ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["schedule_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ToDoEntity(
    @ColumnInfo(name = "program_id", index = true)
    val programId: Int,
    @ColumnInfo(name = "schedule_id",index = true)
    val scheduleId:Int,
    val title: String,
    val priority: Int,
    @ColumnInfo(name = "is_done")
    val isDone: Boolean,
    val date: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}