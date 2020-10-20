package com.aminook.tunemyday.framework.presentation.dailylist

import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.business.interactors.todo.InsertTodo.Companion.INSERT_TODO_SUCCESS
import com.aminook.tunemyday.business.interactors.todo.TodoInteractors
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.framework.presentation.dailylist.manager.DailyScheduleManager
import com.aminook.tunemyday.util.ROUTINE_INDEX
import com.aminook.tunemyday.util.TodoCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.*

class DailyViewModel @ViewModelInject constructor(
    val dateUtil: DateUtil,
    val scheduleInteractors: ScheduleInteractors,
    val todoInteractors: TodoInteractors,
    @DataStoreCache  dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache,dataStoreSettings){

    private val TAG = "aminjoon"
    val activeScope = Dispatchers.IO + viewModelScope.coroutineContext


    private val dailyScheduleManager=DailyScheduleManager()
    private val _schedules = MutableLiveData<List<Schedule>>()
    private val _curScheduleTodos = MutableLiveData<List<Todo>>()
    private var dayIndex=0

    val schedules: LiveData<List<Schedule>>
        get() = _schedules


    fun getDailySchedules(routineId:Long) {
        dayIndex = dateUtil.curDayIndex
       // dayIndex = if (dayIndex > 6) dayIndex - 7 else dayIndex
        CoroutineScope(activeScope).launch {
            scheduleInteractors.getDailySchedules(dayIndex,routineId,dateUtil.curTimeInSec)
                .collect { dataState ->
                processResponse(dataState?.stateMessage)
                    _schedules.value = dataState?.data?: emptyList()
            }
        }
    }

    fun createTodo(
        scheduleId: Long,
        programId:Long,
        task: String,
        isOneTime: Boolean = false
    ):LiveData<Todo?> {
            val todo=Todo(
                title = task,
                scheduleId = scheduleId,
                programId = programId,
                isOneTime = isOneTime,
                priorityIndex = dateUtil.curDateInInt,
                dateAdded = dateUtil.curDateInInt
            )
            return addTodo(todo)
    }

    fun addTodo(todo: Todo):LiveData<Todo?> {

        return todoInteractors.insertTodo(todo)
            .map {
                processResponse(it?.stateMessage)
                it?.data
            }
            .flowOn(Default)
            .asLiveData()
    }

    fun updateTodo(todo: Todo,showSnackbar: Boolean,undoCallback: SnackbarUndoCallback?=null,onDismissCallback: TodoCallback?=null):LiveData<Todo?>{
        return todoInteractors.updateTodo(todo,showSnackbar,undoCallback,onDismissCallback)
            .map {
                processResponse(it?.stateMessage)
                it?.data
            }
            .flowOn(Default)
            .asLiveData()
    }

    fun updateTodos(todos: List<Todo>,scheduleId: Long):LiveData<List<Todo>>{
        return todoInteractors.updateTodos(todos,scheduleId)
            .map {
                processResponse(it?.stateMessage)
               it?.data?: emptyList()
            }
            .asLiveData()
    }

    fun moveTodos(todos: List<Todo>,scheduleId: Long):LiveData<List<Todo>>{

             return todoInteractors.updateTodos(todos,scheduleId)
                 .map {
                     processResponse(it?.stateMessage)
                     it?.data?: emptyList()
                 }
                 .asLiveData()


    }

    fun changeTodoCheck(todo:Todo,showSnackbar:Boolean,undoCallback: SnackbarUndoCallback?=null,onDismissCallback: TodoCallback?=null):LiveData<Todo?>{

            return todoInteractors.updateTodo(todo,showSnackbar,undoCallback,onDismissCallback)
                .map {
                    processResponse(it?.stateMessage)
                    it?.data
                }
                .asLiveData()


    }



    fun deleteTodo(todo: Todo,undoCallback: SnackbarUndoCallback,onDismissCallback: TodoCallback):LiveData<Todo?> {
               return todoInteractors.deleteTodo(
                    todo = todo,
                    undoCallback = undoCallback,
                    onDismissCallback = onDismissCallback
                )
                    .map {
                        processResponse(it?.stateMessage)
                        it?.data

                    }
                    .asLiveData()

    }

}