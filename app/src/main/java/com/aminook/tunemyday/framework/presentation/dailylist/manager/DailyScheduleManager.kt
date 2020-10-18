package com.aminook.tunemyday.framework.presentation.dailylist.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.util.DateUtil
import dagger.hilt.EntryPoint
import javax.inject.Inject


class DailyScheduleManager{

    fun processSchedules(schedules:List<Schedule>):List<Schedule>{
        schedules.onEach {schedule->
            schedule.todos= processTodoList(schedule.todos) as MutableList<Todo>
        }

        val tmpSchedules= mutableListOf<Schedule>()
        tmpSchedules.addAll(schedules)
        tmpSchedules.add(Schedule(id= -1))
        return tmpSchedules
    }

    fun processTodoList(todos: List<Todo>?): List<Todo> {
        val tmpTodos = mutableListOf<Todo>()
        todos?.let {
            tmpTodos.addAll(it)
            if(it.isNotEmpty()) {
                tmpTodos.add(Todo(id = -1))
            }
        }
        return tmpTodos
    }

}