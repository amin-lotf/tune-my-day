package com.aminook.tunemyday.framework.datasource.cache.model

import android.graphics.Color
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "programs")
data class ProgramEntity(
    var name: String,
    var color: Int,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
}


data class ProgramDetail(
    @Embedded
    val program:ProgramEntity,

    @Relation(
        entity = ScheduleEntity::class,
        parentColumn = "id",
        entityColumn = "program_id"
    )
    val schedules:List<FullSchedule>,

    @Relation(
        parentColumn = "id",
        entityColumn = "program_id"
    )
    val todos:List<TodoEntity>

)