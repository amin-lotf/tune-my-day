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
      onUpdate = ForeignKey.CASCADE
    ), ForeignKey(
      entity = ScheduleEntity::class,
      parentColumns = ["id"],
      childColumns = ["schedule_id"],
      onDelete = ForeignKey.CASCADE
    ),
  ForeignKey(
    entity = RoutineEntity::class,
    parentColumns = ["id"],
    childColumns = ["routine_id"],
    onDelete = ForeignKey.CASCADE
  )
  ]
)
data class AlarmEntity(
  @ColumnInfo(name = "schedule_id", index = true)
  var scheduleId: Long,
  @ColumnInfo(name = "program_id", index = true)
  var programId: Long,
  @ColumnInfo(name = "routine_id",index = true)
  var routineId:Long,
  val hourBefore: Int,
  val minBefore: Int,
  var day:Int,
  var startInSec:Int
) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L


}


data class NotificationAlarm(
  @Embedded
  val alarm:AlarmEntity,

  @Relation(
    entity = ScheduleEntity::class,
    parentColumn = "schedule_id",
    entityColumn = "id"
  )
  val detailedSchedule: DetailedSchedule
)