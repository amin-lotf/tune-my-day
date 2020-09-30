package com.aminook.tunemyday.framework.presentation.dailylist

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.business.interactors.todo.InsertTodo.Companion.INSERT_TODO_SUCCESS
import com.aminook.tunemyday.business.interactors.todo.TodoInteractors
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.framework.presentation.dailylist.manager.DailyScheduleManager
import com.aminook.tunemyday.util.TodoCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DailyViewModel @ViewModelInject constructor(
    val dateUtil: DateUtil,
    val scheduleInteractors: ScheduleInteractors,
    val todoInteractors: TodoInteractors,

) : BaseViewModel() {

    private val TAG="aminjoon"
    val activeScope = Dispatchers.IO + viewModelScope.coroutineContext

    var dailyScheduleManager:DailyScheduleManager?= DailyScheduleManager(dateUtil)
    private val _schedules = MutableLiveData<List<Schedule>>()



    val schedules: LiveData<List<Schedule>>
        get() = _schedules


    fun getDailySchedules(fragmentIndex: Int) {
        var dayIndex = dateUtil.curDayIndex + fragmentIndex
        dayIndex = if (dayIndex > 6) dayIndex - 7 else dayIndex
        CoroutineScope(activeScope).launch {
            scheduleInteractors.getDailySchedules(dayIndex).collect { dataState ->
                processResponse(dataState?.stateMessage)

                dataState?.data?.let { schedules ->

                    _schedules.value = schedules
                    dailyScheduleManager?.bufferSchedules(schedules)
                }
            }
        }
    }

    fun createTodo(scheduleId: Long, task: String, isOneTime: Boolean = false) {
        val todo = dailyScheduleManager?.createTodo(scheduleId, task, isOneTime)
        todo?.let {
            addTodo(it)
        }

    }

    fun addTodo(todo: Todo) {
        CoroutineScope(activeScope).launch {
            todoInteractors.insertTodo(todo).collect { dataState ->
                processResponse(dataState?.stateMessage)

                dataState?.data?.let { result->
                    if(result==INSERT_TODO_SUCCESS){
                        delay(200)
                   //     _todoChanged.value=todo.scheduleId
                    }
                }
            }
        }
    }

    fun getTodos(scheduleId:Long):LiveData<List<Todo>>{
        CoroutineScope(activeScope).launch {
            todoInteractors.getScheduleTodos(scheduleId).collect {dataState->
                processResponse(dataState?.stateMessage)

                dataState?.data?.let {
                  dailyScheduleManager?.bufferTodos(it)
                }
            }
        }

      return dailyScheduleManager?.buffTodo?: MutableLiveData()
    }

    fun deleteTodo(todo: Todo) {
        CoroutineScope(activeScope).launch {
            todoInteractors.deleteTodo(
                todo = todo,
                undoCallback = object : SnackbarUndoCallback {
                    override fun undo() {
                        addTodo(todo)
                    }
                },
                onDismissCallback = object : TodoCallback {
                    override fun execute() {
                        Log.d(TAG, "todo snackbar dismissed")
                    }
                }
            ).collect {dataState->

                processResponse(dataState?.stateMessage)
            }
        }
    }

    override fun onCleared() {
        dailyScheduleManager=null
        super.onCleared()
    }
}