package com.aminook.tunemyday.framework.datasource.cache.database

import androidx.room.*
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.framework.datasource.cache.model.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Insert
    suspend fun insertTodo(todoEntity: TodoEntity): Long



    @Update
    suspend fun updateTodo(todoEntity: TodoEntity):Int

    @Transaction
    @Update
    suspend fun updateTodos(todoEntities:List<TodoEntity>)

    @Delete
    suspend fun deleteTodo(todoEntity: TodoEntity):Int

    @Transaction
    @Query("select * from todos where schedule_id= :scheduleId order by is_done,priorityIndex")
    suspend fun getScheduleToDo(scheduleId:Long):List<TodoEntity>

    @Query("select * from todos where schedule_id= :scheduleId order by is_done,priorityIndex")
    fun getScheduleToDoFlow(scheduleId:Long):Flow<List<TodoEntity>>

    @Transaction
    @Query("select * from todos where schedule_id= :programId")
    fun getProgramToDo(programId:Int):Flow<List<TodoEntity>>




    @Transaction
    suspend fun deleteAndRetrieveTodos(todoToDelete: TodoEntity):List<TodoEntity>{
        deleteTodo(todoToDelete)
        return getScheduleToDo(todoToDelete.scheduleId)
    }

    @Transaction
    suspend fun insertAndRetrieveTodos(todoToInsert: TodoEntity):List<TodoEntity>{
        insertTodo(todoToInsert)
        return getScheduleToDo(todoToInsert.scheduleId)
    }

    @Transaction
    suspend fun updateAndRetrieveTodos(todoToUpdate:TodoEntity):List<TodoEntity>{
        updateTodo(todoToUpdate)
        return getScheduleToDo(todoToUpdate.scheduleId)
    }

    @Transaction
    suspend fun updateListAndRetrieveTodos(todosToUpdate:List<TodoEntity>,scheduleId:Long):List<TodoEntity>{
        updateTodos(todosToUpdate)
        return getScheduleToDo(scheduleId)
    }

}