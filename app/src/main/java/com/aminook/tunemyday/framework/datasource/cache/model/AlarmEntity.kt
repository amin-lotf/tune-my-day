package com.aminook.tunemyday.framework.datasource.cache.model

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
  @ColumnInfo(name = "schedule_id",index = true)
  val scheduleId:Int,
  @ColumnInfo(name = "program_id", index = true)
  val programId: Int,
  val date: String,
  val time: String,
  @ColumnInfo(index = true)
  val day: Int
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}