package com.aminook.tunemyday.framework.presentation.dailylist

import android.util.Log
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
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.framework.presentation.dailylist.manager.DailyScheduleManager
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

    ) : BaseViewModel() {

    private val TAG = "aminjoon"
    val activeScope = Dispatchers.IO + viewModelScope.coroutineContext


    private val _schedules = MutableLiveData<List<Schedule>>()
    private val _curScheduleTodos = MutableLiveData<List<Todo>>()
    private var dayIndex=0

    val schedules: LiveData<List<Schedule>>
        get() = _schedules



    fun getDay():Day{
        return dateUtil.getDay(dayIndex)
    }

    fun getDailySchedules(fragmentIndex: Int) {
        dayIndex = dateUtil.curDayIndex + fragmentIndex
        dayIndex = if (dayIndex > 6) dayIndex - 7 else dayIndex
        CoroutineScope(activeScope).launch {
            scheduleInteractors.getDailySchedules(dayIndex).collect { dataState ->
                processResponse(dataState?.stateMessage)
                dataState?.data?.let { schedules ->
                    _schedules.value = schedules
                }
            }
        }
    }

    fun createTodo(scheduleId: Long, task: String, isOneTime: Boolean = false):LiveData<List<Todo>> {
            val todo=Todo(
                title = task,
                scheduleId = scheduleId,
                isOneTime = isOneTime,
                priorityIndex = dateUtil.curTimeInMillis,
                dateAdded = dateUtil.curTimeInMillis
            )
            return addTodo(todo)
    }

    fun addTodo(todo: Todo):LiveData<List<Todo>> {

        return todoInteractors.insertAndRetrieveTodos(todo)
            .map {
                processResponse(it?.stateMessage)
                it?.data?: emptyList()
            }
            .flowOn(Default)
            .asLiveData()
    }

    fun updateTodo(todo: Todo):LiveData<List<Todo>>{
        return todoInteractors.updateTodo(todo)
            .map {
                processResponse(it?.stateMessage)
                it?.data?: emptyList()
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
            .flowOn(Default)
            .asLiveData()
    }

    fun getTodos(scheduleId: Long): LiveData<List<Todo>> {

        return todoInteractors.getScheduleTodos(scheduleId)
            .flowOn(Default)
            .map {
                processResponse(it?.stateMessage)
                it?.data ?: emptyList()
            }
            .flowOn(IO)
            .asLiveData()

    }

    fun deleteTodo(todo: Todo,undoCallback: SnackbarUndoCallback,onDismissCallback: TodoCallback):LiveData<List<Todo>> {

           return todoInteractors.deleteAndRetrieveTodos(
                todo = todo,
                undoCallback = undoCallback,
                onDismissCallback = onDismissCallback
            )
                .map {
                    processResponse(it?.stateMessage)
                    it?.data?: emptyList()

                }

                .flowOn(Default)
                .asLiveData()

    }

    override fun onCleared() {
        super.onCleared()
    }
}