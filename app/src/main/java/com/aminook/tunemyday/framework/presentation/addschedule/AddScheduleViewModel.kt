package com.aminook.tunemyday.framework.presentation.addschedule

import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
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
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_END
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.TIME_START
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map


class AddScheduleViewModel @ViewModelInject constructor(
    val programInteractors: ProgramInteractors,
    val scheduleInteractors: ScheduleInteractors,
    val todoInteractors: TodoInteractors,
    val dateUtil: DateUtil,
    @DataStoreCache  dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache,dataStoreSettings) {
    private val TAG = "aminjoon"
    val addScheduleManager = AddScheduleManager()
    var conflictedSchedules = listOf<Schedule>()
    var modifiedAlarmIndexes = listOf<Long>()
    private val activeScope = Default + viewModelScope.coroutineContext
    private var _allPrograms = MutableLiveData<List<Program>>()
    private var job: Job? = null
    private var _requestType: String = SCHEDULE_REQUEST_NEW
    private val _todos = MutableLiveData<List<Todo>>()
    private val _scheduleValidated = MutableLiveData<Boolean>()


    val scheduleTodos: LiveData<List<Todo>>
        get() = _todos

    val scheduleValidated: LiveData<Boolean>
        get() = _scheduleValidated

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

    val scheduleId: Long
        get() = addScheduleManager.buffSchedule.id


    val listChanged: LiveData<String>
        get() = addScheduleManager.listChanged

    val alarmModifiedPosition: Int
        get() = addScheduleManager.alarmModifiedPosition


    fun addTodo(todo: Todo): LiveData<List<Todo>> {

        return todoInteractors.insertAndRetrieveTodos(todo)
            .map {
                processResponse(it?.stateMessage)
                addScheduleManager.addTodos(it?.data)
                it?.data ?: emptyList()
            }
            .flowOn(Dispatchers.Default)
            .asLiveData()
    }

    fun createTodo(
        scheduleId: Long,
        task: String,
        isOneTime: Boolean = false
    ): LiveData<List<Todo>> {
        val todo = Todo(
            title = task,
            scheduleId = scheduleId,
            programId = addScheduleManager.buffSchedule.program.id,
            isOneTime = isOneTime,
            priorityIndex = dateUtil.curDateInInt,
            dateAdded = dateUtil.curDateInInt
        )
        return addTodo(todo)
    }

    fun updateTodo(todo: Todo): LiveData<List<Todo>> {
        return todoInteractors.updateTodo(todo)
            .map {
                processResponse(it?.stateMessage)
                addScheduleManager.addTodos(it?.data)
                it?.data ?: emptyList()
            }
            .flowOn(Dispatchers.Default)
            .asLiveData()
    }

    fun updateTodos(todos: List<Todo>, scheduleId: Long): LiveData<List<Todo>> {
        return todoInteractors.updateTodos(todos, scheduleId)
            .map {
                processResponse(it?.stateMessage)
                addScheduleManager.addTodos(it?.data)
                it?.data ?: emptyList()
            }
            .flowOn(Default)
            .asLiveData()
    }


    fun deleteTodo(
        todo: Todo,
        undoCallback: SnackbarUndoCallback,
        onDismissCallback: TodoCallback
    ): LiveData<List<Todo>> {

        return todoInteractors.deleteAndRetrieveTodos(
            todo = todo,
            undoCallback = undoCallback,
            onDismissCallback = onDismissCallback
        )
            .map {
                processResponse(it?.stateMessage)
                addScheduleManager.addTodos(it?.data)
                it?.data ?: emptyList()

            }

            .flowOn(Default)
            .asLiveData()

    }


    fun validateSchedule() {

        if (addScheduleManager.buffSchedule.program.name.isEmpty()) {
            handleLocalError("Please choose an activity")
        } else {
            job = CoroutineScope(activeScope).launch {
                scheduleInteractors.validateSchedule(
                    schedule = addScheduleManager.buffSchedule, object : AreYouSureCallback {
                        override fun proceed() {
                            _scheduleValidated.value=true
                        }

                        override fun cancel() {
                            _scheduleValidated.value=false
                        }
                    }
                ).map { dataState ->
//
                    processResponse(dataState?.stateMessage)

                    dataState?.data?.let {
                        if (it.isNullOrEmpty()) {

                            saveSchedule(emptyList())
                        } else {
                            val indexes = mutableListOf<Long>()
                            it.forEach {

                                val alarms = it.alarms
                                alarms.forEach {
                                    indexes.add(it.id)
                                }
                            }
                            modifiedAlarmIndexes = indexes
                            conflictedSchedules = it
                        }

                    }
                }
                    .collect()

            }
            //  handleJob(job)
        }
    }

     fun saveSchedule(confSchedules: List<Schedule> = conflictedSchedules) {
        if (addScheduleManager.buffSchedule.program.name.isEmpty()) {
            handleLocalError("Please choose an activity")
        } else {

            CoroutineScope(activeScope).launch {
                scheduleInteractors.insertSchedule(
                    addScheduleManager.buffSchedule,
                    confSchedules,
                    _requestType,
                    routineId
                ).map {
                    processResponse(it?.stateMessage)
                }.collect()

            }
        }
    }


    fun processRequest(request: String, args: AddEditScheduleFragmentArgs, routineId: Long) {
        _requestType = request
        addScheduleManager.setRoutineId(routineId)
        when (request) {
            SCHEDULE_REQUEST_EDIT -> {
                val scheduleId = args.scheduleId
                addScheduleManager.setScheduleId(scheduleId)
                if (scheduleId > 0L) {
                    try {


                        CoroutineScope(Default).launch {
                            scheduleInteractors.getSchedule(scheduleId)
                                .map {
                                    processResponse(it?.stateMessage)
                                    try {
                                        withContext(Main) {
                                            it?.data?.let { schedule ->
                                                bufferChosenProgram(schedule.program)
                                                catchDaysOfWeek(schedule.startDay)
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
                                                _todos.value = schedule.todos
                                                addScheduleManager.addTodos(schedule.todos)
                                            }
                                        }
                                    } catch (e: Throwable) {
                                        Log.d(TAG, "processRequest: error ${e.message}")
                                        print(e.stackTraceToString())
                                        addScheduleManager.setScheduleId(0)
                                    }
                                }.collect ()

                        }
                    } catch (e: Throwable) {
                        Log.d(TAG, "processRequest: error getting schedule add-edit ${e.message}")
                        println(e.stackTrace)
                    }
                }
            }
            SCHEDULE_REQUEST_NEW -> {
                val startTime=args.startTime
                val endTime=args.endTime
                CoroutineScope(activeScope).launch {
                    dataStoreSettings.data.map {
                        val index = it[DAY_INDEX] ?: dateUtil.curDayIndex
                        withContext(Main) {
                            catchDaysOfWeek(index)
                            startTime?.let {
                                setTime(
                                    startTime.hour,
                                    startTime.minute,
                                    TIME_START
                                )
                            }
                            endTime?.let {
                                setTime(
                                    endTime.hour,
                                    endTime.minute,
                                    TIME_END
                                )
                            }
                        }
                    }.collect()

                }
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
        val daysList = dateUtil.getDaysOfWeek(chosenDay)
        addScheduleManager.addDaysToBuffer(daysList)
        daysList.forEach { day ->
            if (day.isChosen) {
                addScheduleManager.setChosenDay(day)
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

        CoroutineScope(activeScope).launch {
            programInteractors.getAllPrograms()
                .collect { dataState ->

                    processResponse(dataState?.stateMessage)
                    dataState?.data?.let {

                        _allPrograms.postValue(it)
                    }

                }
        }
    }

    fun getProgram(programId: Long) {
        CoroutineScope(activeScope).launch {
            programInteractors.getProgram(programId)
                .map {
                    processResponse(it?.stateMessage)

                    it?.data?.let {
                        withContext(Main) {
                            bufferChosenProgram(it)
                        }
                    }
                }
                .collect()
        }

    }


}