package com.aminook.tunemyday.framework.datasource.cache.database

import androidx.room.*
import com.aminook.tunemyday.framework.datasource.cache.model.ToDoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {

    @Insert
    fun insertTodo(toDoEntity: ToDoEntity): Long

    @Update
    fun updateTodo(toDoEntity: ToDoEntity):Int

    @Delete
    fun deleteTodo(toDoEntity: ToDoEntity):Int

    @Query("select * from todos where schedule_id= :scheduleId")
    fun getScheduleToDo(scheduleId:Int):Flow<List<ToDoEntity>>

    @Query("select * from todos where schedule_id= :programId")
    fun getProgramToDo(programId:Int):Flow<List<ToDoEntity>>

}