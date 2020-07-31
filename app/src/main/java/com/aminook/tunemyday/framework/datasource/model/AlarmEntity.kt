package com.aminook.tunemyday.framework.datasource.model

import androidx.room.*

@Entity(
  tableName = "alarms",
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
data class AlarmEntity(
  @PrimaryKey
  @ColumnInfo(name = "schedule_id")
  val scheduleId:Int,
  @ColumnInfo(name = "program_id", index = true)
  val programId: Int,
  val date: String,
  val time: String,
  @ColumnInfo(index = true)
  val day: Int
) {
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
}