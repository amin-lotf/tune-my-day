package com.aminook.tunemyday.framework.datasource.cache.database

import androidx.room.*
import com.aminook.tunemyday.business.domain.model.Todo
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

    @Transaction
    @Update
    suspend fun updateTodos(todoEntities:List<TodoEntity>)

    @Delete
    suspend fun deleteTodo(todoEntity: TodoEntity):Int

    @Transaction
    @Query("select * from todos where schedule_id= :scheduleId order by is_done,priorityIndex")
    suspend fun getScheduleToDo(scheduleId:Long):List<FullTodo>


    @Transaction
    @Query("select * from todos where schedule_id= :programId")
    fun getProgramToDo(programId:Int):Flow<List<FullTodo>>


    @Insert
    suspend fun insertSubTodo(subTodo:SubTodoEntity):Long

    @Delete
    suspend fun deleteSubTodo(subTodo:SubTodoEntity):Int

    @Update
    suspend fun updateSubTodo(subTodo: SubTodoEntity):Int


    @Transaction
    suspend fun deleteAndRetrieveTodos(todoToDelete: TodoEntity):List<FullTodo>{
        deleteTodo(todoToDelete)
        return getScheduleToDo(todoToDelete.scheduleId)
    }

    @Transaction
    suspend fun insertAndRetrieveTodos(todoToInsert: TodoEntity):List<FullTodo>{
        insertTodo(todoToInsert)
        return getScheduleToDo(todoToInsert.scheduleId)
    }

    @Transaction
    suspend fun updateAndRetrieveTodos(todoToUpdate:TodoEntity):List<FullTodo>{
        updateTodo(todoToUpdate)
        return getScheduleToDo(todoToUpdate.scheduleId)
    }

    @Transaction
    suspend fun updateListAndRetrieveTodos(todosToUpdate:List<TodoEntity>,scheduleId:Long):List<FullTodo>{
        updateTodos(todosToUpdate)
        return getScheduleToDo(scheduleId)
    }

}