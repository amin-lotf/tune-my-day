package com.aminook.tunemyday.framework.datasource.cache.model

import androidx.room.*

@Entity(
    tableName = "todos",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["schedule_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["program_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TodoEntity(
    @ColumnInfo(name = "schedule_id",index = true)
    val scheduleId:Long,
    @ColumnInfo(name = "program_id",index = true)
    var programId:Long,
    val title: String,
    val priorityIndex: Long,
    @ColumnInfo(name = "is_done")
    var isDone: Boolean,
    val dateAdded: Long,
    var isOneTime:Boolean,
    var lastChecked:Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}


