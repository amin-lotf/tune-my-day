package com.aminook.tunemyday.framework.datasource.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "routines"
)
data class RoutineEntity(
    var name:String
) {
    @PrimaryKey(autoGenerate = true)
    var id:Long=0
}