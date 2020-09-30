package com.aminook.tunemyday.framework.datasource.cache.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey


//TODO(Delete if not used)
@Entity(
    tableName = "todo_schedule", foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["schedule_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TodoEntity::class,
            parentColumns = ["id"],
            childColumns = ["todo_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["schedule_id","todo_id"]
)
data class ToDoScheduleMapper(
    @ColumnInfo(name = "schedule_id",index = true)
    val scheduleId:Int,
    @ColumnInfo(name = "todo_id",index = true)
    val todoId:Int
)