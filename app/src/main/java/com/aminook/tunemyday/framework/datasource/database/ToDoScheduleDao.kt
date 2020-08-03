package com.aminook.tunemyday.framework.datasource.database

import androidx.room.Dao
import androidx.room.Insert
import com.aminook.tunemyday.framework.datasource.model.ToDoScheduleMapper
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoScheduleDao {

    @Insert
    fun insertToDoSchedule(toDoScheduleMapper: ToDoScheduleMapper):Flow<Long>

}