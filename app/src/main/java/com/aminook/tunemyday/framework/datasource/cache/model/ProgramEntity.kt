package com.aminook.tunemyday.framework.datasource.cache.model

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "programs")
data class ProgramEntity(
    val name: String,
    val color: Int,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}