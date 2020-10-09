package com.aminook.tunemyday.framework.presentation.addschedule

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.*
import com.aminook.tunemyday.business.domain.state.*
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.program.ProgramInteractors
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.business.interactors.todo.TodoInteractors
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_END
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_START
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_EDIT
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_NEW
import com.aminook.tunemyday.util.TodoCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map


class AddScheduleViewModel @ViewModelInject constructor(
    val programInteractors: ProgramInteractors,
    val scheduleInteractors: ScheduleInteractors,
    val todoInteractors: TodoInteractors,
    val dateUtil:DateUtil
    ) : BaseViewModel() {
    private val TAG = "aminjoon"
    val addScheduleManager = AddScheduleManager()
    var conflictedSchedules = listOf<Schedule>()
    var modifiedAlarmIndexes= listOf<Long>()
    private val activeScope = IO + viewModelScope.coroutineContext
    private var _allPrograms = MutableLiveData<List<Program>>()
    private var job: Job? = null
    private var _requestType:String= SCHEDULE_REQUEST_NEW


    val requestType:String
    get() = _requestType

    val selectedProgram: LiveData<Program>
        get() = addScheduleManager.chosenProgram

    val allPrograms: LiveData<List<Program>>
        get() = _allPrograms

    val daysOfWeek: List<Day>
        get() = addScheduleManager.daysOfWeek

    val chosenDay: LiveData<Day>
        get() = addScheduleManager.getChosenDay()

    val startTime: LiveData<Time>
        get() = addScheduleManager.startTime

    val endTime: LiveData<Time>
        get() = addScheduleManager.endTime




    val listChanged: LiveData<String>
        get() = addScheduleManager.listChanged

    val alarmModifiedPosition: Int
        get() = addScheduleManager.alarmModifiedPosition



    fun addTodo(todo: Todo):LiveData<List<Todo>> {

        return todoInteractors.insertAndRetrieveTodos(todo)
            .map {
                processResponse(it?.stateMessage)
                addScheduleManager.addTodos(it?.data)
                it?.data?: emptyList()
            }
            .flowOn(Dispatchers.Default)
            .asLiveData()
    }

    fun createTodo(scheduleId: Long, task: String, isOneTime: Boolean = false):LiveData<List<Todo>> {
        val todo=Todo(
            title = task,
            scheduleId = scheduleId,
            programId = addScheduleManager.buffSchedule.program.id,
            isOneTime = isOneTime,
            priorityIndex = dateUtil.curTimeInMillis,
            dateAdded = dateUtil.curTimeInMillis
        )
        return addTodo(todo)
    }

    fun updateTodo(todo: Todo):LiveData<List<Todo>>{
        return todoInteractors.updateTodo(todo)
            .map {
                processResponse(it?.stateMessage)
                addScheduleManager.addTodos(it?.data)
                it?.data?: emptyList()
            }
            .flowOn(Dispatchers.Default)
            .asLiveData()
    }

    fun updateTodos(todos: List<Todo>,scheduleId: Long):LiveData<List<Todo>>{
        return todoInteractors.updateTodos(todos,scheduleId)
            .map {
                processResponse(it?.stateMessage)
                addScheduleManager.addTodos(it?.data)
                it?.data?: emptyList()
            }
            .flowOn(Dispatchers.Default)
            .asLiveData()
    }

    fun getTodos(): LiveData<List<Todo>> {
        return todoInteractors.getScheduleTodos(addScheduleManager.buffSchedule.id)
            .flowOn(Dispatchers.Default)
            .map {
                processResponse(it?.stateMessage)
                addScheduleManager.addTodos(it?.data)
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
                addScheduleManager.addTodos(it?.data)
                it?.data?: emptyList()

            }

            .flowOn(Dispatchers.Default)
            .asLiveData()

    }


    fun validateSchedule(areYouSureCallback: AreYouSureCallback) {

        if (addScheduleManager.buffSchedule.program.name.isEmpty()) {
            handleLocalError("Please choose an activity")
        } else {
            job = CoroutineScope(activeScope).launch {
                scheduleInteractors.validateSchedule(
                    schedule = addScheduleManager.buffSchedule, areYouSureCallback
                )
                    .collect { dataState ->

                        processResponse(dataState?.stateMessage)

                        dataState?.data?.let {
                            if (it.isNullOrEmpty()) {

                                saveSchedule(emptyList())
                            } else {
                                val indexes= mutableListOf<Long>()
                                it.forEach {

                                    val alarms=it.alarms
                                    alarms.forEach {
                                        indexes.add(it.id)
                                    }
                                }
                                modifiedAlarmIndexes=indexes
                                conflictedSchedules = it
                            }

                        }
                    }
            }
            //  handleJob(job)
        }
    }

    fun saveSchedule(confSchedules: List<Schedule> = conflictedSchedules) {
        if (addScheduleManager.buffSchedule.program.name.isEmpty()) {
            handleLocalError("Please choose an activity")
        } else {

            job = CoroutineScope(activeScope).launch {
                scheduleInteractors.insertSchedule(
                    addScheduleManager.buffSchedule,
                    confSchedules,
                    _requestType
                )
                    .collect { dataState ->
                        processResponse(dataState?.stateMessage)
                    }
            }
        }
    }

    fun processRequest(request: String, args: AddEditScheduleFragmentArgs) {
        _requestType=request

        when(request){
            SCHEDULE_REQUEST_EDIT -> {
                val scheduleId = args.scheduleId
                addScheduleManager.setScheduleId(scheduleId)
                if (scheduleId != 0L) {
                    CoroutineScope(activeScope).launch {
                        scheduleInteractors.getSchedule(scheduleId).collect { dataState ->
                            processResponse(dataState?.stateMessage)
                            try {
                                dataState?.data?.let { schedule ->

                                    catchDaysOfWeek(schedule.startDay)
                                    bufferChosenProgram(schedule.program)
                                    updateBufferedDays(daysOfWeek[schedule.startDay])
                                    setTime(
                                        schedule.startTime.hour,
                                        schedule.startTime.minute,
                                        TIME_START
                                    )
                                    setTime(
                                        schedule.endTime.hour,
                                        schedule.endTime.minute,
                                        TIME_END
                                    )

                                    schedule.alarms.forEach {
                                        setAlarm(it)
                                    }

                                }
                            } catch (e: Throwable) {
                                addScheduleManager.setScheduleId(0)
                            }

                        }
                    }
                } else {

                    //TODO(handle error)
                }
            }
            SCHEDULE_REQUEST_NEW->{
                val index=args.chosenDay
                catchDaysOfWeek(index)
            }
        }
    }


    fun removeAlarm(alarm: Alarm) {
        addScheduleManager.removeAlarm(alarm)
    }

    fun getAlarms() = addScheduleManager.alarms


    fun setAlarm(alarm: Alarm) {

        addScheduleManager.addAlarm(alarm)
    }



    fun setTime(hour: Int, minute: Int, type: Int) {
        addScheduleManager.setTime(hour, minute, type)
    }

    fun updateBufferedDays(updatedDay: Day) {
        addScheduleManager.setChosenDay(updatedDay)
    }


    private fun catchDaysOfWeek(chosenDay: Int) {
        CoroutineScope(activeScope).launch {
            scheduleInteractors.getDaysOfWeek(chosenDay).collect { dataState ->

                processResponse(dataState?.stateMessage)
                dataState?.data?.let { daysList ->
                    addScheduleManager.addDaysToBuffer(daysList)
                    daysList.forEach { day ->
                        if (day.isChosen) {
                            withContext(Main) {
                                addScheduleManager.setChosenDay(day)
                            }
                        }
                    }
                }
            }
        }
    }


    fun addProgram(program: Program) {
        CoroutineScope(activeScope).launch {

            programInteractors.insertProgram(program).collect { dataState ->

                processResponse(dataState?.stateMessage)
                dataState?.data?.let {
                    bufferChosenProgram(it)
                }
            }
        }
    }

    fun bufferChosenProgram(program: Program) {
        addScheduleManager.addProgramToBuffer(program)

    }

    fun getAllPrograms() {

        job = CoroutineScope(activeScope).launch {
            programInteractors.getAllPrograms()
                .collect { dataState ->

                    processResponse(dataState?.stateMessage)
                    dataState?.data?.let {

                        _allPrograms.postValue(dataState.data)
                    }

                }
        }
    }







}