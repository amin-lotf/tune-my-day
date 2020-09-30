package com.aminook.tunemyday.framework.presentation.dailylist.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.util.DateUtil
import dagger.hilt.EntryPoint
import javax.inject.Inject


class DailyScheduleManager constructor(
    private val dateUtil: DateUtil
) {


    private val _buffSchedules = hashMapOf<Long, Schedule>()
    private val _buffTodos=MutableLiveData<List<Todo>>()

    val buffSchedules: List<Schedule>
        get() = ArrayList(_buffSchedules.values)


    val buffTodo:LiveData<List<Todo>>
    get() = _buffTodos

    fun bufferTodos(todos:List<Todo>){
        _buffTodos.value=todos
    }

    fun createTodo(scheduleId:Long,task:String,isOnTime:Boolean):Todo{
        return Todo(
            title = task,
            scheduleId = scheduleId,
            dateAdded = dateUtil.curTimeInMillis,
            isOneTime = isOnTime
        ).apply {
            _buffSchedules[scheduleId]?.let {schedule->
                var lastIndex=0
                if (schedule.todos.isNotEmpty()) {
                    lastIndex = schedule.todos.last().priorityIndex
                }
                this.priorityIndex = lastIndex + 1

                if (schedule.startInSec>dateUtil.curTimeInSec){
                    this.lastChecked=dateUtil.curDateInInt
                }
            }
        }
    }

    fun bufferSchedules(schedules: List<Schedule>) {
        schedules.forEach { schedule ->
            if (!_buffSchedules.containsKey(schedule.id)) {
                _buffSchedules[schedule.id] = schedule

            }
        }
    }
}