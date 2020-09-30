package com.aminook.tunemyday.framework.datasource.cache.database

import androidx.room.*
import com.aminook.tunemyday.framework.datasource.cache.model.FullTodo
import com.aminook.tunemyday.framework.datasource.cache.model.SubTodoEntity
import com.aminook.tunemyday.framework.datasource.cache.model.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Insert
    suspend fun insertTodo(todoEntity: TodoEntity): Long

    @Update
    suspend fun updateTodo(todoEntity: TodoEntity):Int

    @Delete
    suspend fun deleteTodo(todoEntity: TodoEntity):Int

    @Transaction
    @Query("select * from todos where schedule_id= :scheduleId")
    fun getScheduleToDo(scheduleId:Long):Flow<List<FullTodo>>


    @Transaction
    @Query("select * from todos where schedule_id= :programId")
    fun getProgramToDo(programId:Int):Flow<List<FullTodo>>


    @Insert
    suspend fun insertSubTodo(subTodo:SubTodoEntity):Long

    @Delete
    suspend fun deleteSubTodo(subTodo:SubTodoEntity):Int

    @Update
    suspend fun updateSubTodo(subTodo: SubTodoEntity):Int



}