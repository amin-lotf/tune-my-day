package com.aminook.tunemyday.framework.datasource.database

import androidx.room.*
import com.aminook.tunemyday.framework.datasource.model.ToDoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {

    @Insert
    fun insertTodo(toDoEntity: ToDoEntity): Flow<Long>

    @Update
    fun updateTodo(toDoEntity: ToDoEntity):Flow<Int>

    @Delete
    fun deleteTodo(toDoEntity: ToDoEntity):Flow<Int>

    @Query("select * from todos where schedule_id= :scheduleId")
    fun getScheduleToDo(scheduleId:Int):Flow<List<ToDoEntity>>

    @Query("select * from todos where schedule_id= :programId")
    fun getProgramToDo(programId:Int):Flow<List<ToDoEntity>>

}