package com.aminook.tunemyday.framework.presentation.viewtodos

import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.business.interactors.todo.TodoInteractors
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.util.TodoCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ViewTodoViewModel @ViewModelInject constructor(
    val todoInteractors: TodoInteractors,
    val scheduleInteractors: ScheduleInteractors,
    val dateUtil: DateUtil,
    @DataStoreCache dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache, dataStoreSettings) {
    private val TAG="aminjoon"
    var dayIndex = 0
    var _schedule = Schedule()
    val activeScope = viewModelScope.coroutineContext + Default

    private val _newTodo= MutableLiveData<Todo?>()
    private val _updatedTodo=MutableLiveData<Todo?>()
    private val _deletedTodo=MutableLiveData<Todo?>()
    private val _checkChangedTodo=MutableLiveData<Todo?>()
    private val _draggedTodos=MutableLiveData<List<Todo>?>()


    val newTodo:LiveData<Todo?>
    get() = _newTodo

    val updatedTodo:LiveData<Todo?>
        get() = _updatedTodo

    val deletedTodo:LiveData<Todo?>
        get() = _deletedTodo

    val checkChangedTodo:LiveData<Todo?>
        get() = _checkChangedTodo

    val draggedTodos:LiveData<List<Todo>?>
        get() = _draggedTodos

    fun getSchedule(scheduleId: Long): LiveData<Schedule?> {
        dayIndex = dateUtil.curDayIndex
        return scheduleInteractors.getSchedule(scheduleId)
            .map { dataState ->
                processResponse(dataState?.stateMessage)
                dataState?.data?.let {
                    _schedule = it
                }
                dataState?.data
            }
            .asLiveData()

    }

    fun createTodo(
        task: String,
        isOneTime: Boolean = false
    ){
        val todo = Todo(
            title = task,
            scheduleId = _schedule.id,
            programId = _schedule.program.id,
            isOneTime = isOneTime,
            priorityIndex = dateUtil.curDateInInt,
            dateAdded = dateUtil.curDateInInt
        )
         addTodo(todo)
    }

    fun addTodo(todo: Todo) {
            CoroutineScope(activeScope).launch {
                todoInteractors.insertTodo(todo)
                    .collect {
                        processResponse(it?.stateMessage)
                        withContext(Main){
                            _newTodo.value=it?.data
                        }
                    }
            }

    }

    fun updateTodo(
        todo: Todo,
        showSnackbar: Boolean,
        undoCallback: SnackbarUndoCallback? = null,
        onDismissCallback: TodoCallback? = null
    ) {
        CoroutineScope(activeScope).launch {
            todoInteractors.updateTodo(todo, showSnackbar, undoCallback, onDismissCallback)
                .collect {
                    processResponse(it?.stateMessage)
                    withContext(Main) {
                        _updatedTodo.value = it?.data
                    }
                }
        }

    }

    fun moveTodos(todos: List<Todo>, scheduleId: Long){
        CoroutineScope(activeScope).launch {
            todoInteractors.updateTodos(todos, scheduleId)
                .collect {
                    processResponse(it?.stateMessage)
                    withContext(Main){
                        _draggedTodos.value=it?.data
                    }
                }
        }
    }

    fun changeTodoCheck(
        todo: Todo,
        showSnackbar: Boolean,
        undoCallback: SnackbarUndoCallback? = null,
        onDismissCallback: TodoCallback? = null
    ) {

        CoroutineScope(activeScope).launch {
            todoInteractors.updateTodo(todo, showSnackbar, undoCallback, onDismissCallback)
                .collect {
                    processResponse(it?.stateMessage)
                    withContext(Main){
                        _checkChangedTodo.value=it?.data
                    }

                }

        }



    }


    fun deleteTodo(
        todo: Todo,
        undoCallback: SnackbarUndoCallback,
        onDismissCallback: TodoCallback
    ) {
        CoroutineScope(activeScope).launch {
            todoInteractors.deleteTodo(
                todo = todo,
                undoCallback = undoCallback,
                onDismissCallback = onDismissCallback
            )
                .collect {
                    processResponse(it?.stateMessage)
                    withContext(Main){
                        _deletedTodo.value=it?.data
                    }
                }
        }



    }


    override fun onCleared() {
        Log.d(TAG, "onCleared: view todo ")
        super.onCleared()
    }

}