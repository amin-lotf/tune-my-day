package com.aminook.tunemyday.framework.datasource.cache.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "subtodo",
    foreignKeys = [
    ForeignKey(
        entity = TodoEntity::class,
        parentColumns = ["id"],
        childColumns = ["todo_id"],
        onDelete = ForeignKey.CASCADE
    )
    ]
)

data class SubTodoEntity (
    @ColumnInfo(name = "todo_id",index = true)
    val todoId:Long,
    val title: String,
    val isDone: Boolean,
    val dateAdded:Int
){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}