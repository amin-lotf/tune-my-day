package com.aminook.tunemyday.framework.datasource.cache.database

import androidx.room.Dao
import androidx.room.Insert
import com.aminook.tunemyday.framework.datasource.cache.model.ToDoScheduleMapper

@Dao
interface ToDoScheduleDao {

    @Insert
    fun insertToDoSchedule(toDoScheduleMapper: ToDoScheduleMapper):Long

}