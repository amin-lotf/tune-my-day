package com.aminook.tunemyday.framework.presentation

import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.model.NotificationSettings
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.alarm.AlarmInteractors
import com.aminook.tunemyday.business.interactors.program.ProgramInteractors
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.business.interactors.settings.NotificationSettingsInteractors
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.di.DataStoreNotification
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class MainViewModel @ViewModelInject constructor(
    val dateUtil: DateUtil,
    val alarmInteractors: AlarmInteractors,
    val scheduleInteractors: ScheduleInteractors,
    val programInteractors: ProgramInteractors,
    val notificationSettingsInteractors: NotificationSettingsInteractors,
    @DataStoreCache dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache, dataStoreSettings) {

    //private val TAG="aminjoon"
    private val activeScope = Dispatchers.IO + viewModelScope.coroutineContext
    var buffRoutineId: Long? = null

    fun setDayIndex(index: Int) {
        CoroutineScope(activeScope).launch {
            dataStoreSettings.edit { settings ->
                settings[DAY_INDEX] = index
            }
        }
    }


    fun getNotificationSettings():LiveData<NotificationSettings?>{
        return notificationSettingsInteractors.getNotificationSettings()
            .map {
                processResponse(it?.stateMessage)
                it?.data
            }.asLiveData()
    }


    fun updateNotificationSettings(notificationSettings: NotificationSettings){
        CoroutineScope(activeScope).launch {
            notificationSettingsInteractors.updateNotificationSettings(notificationSettings)
                .map {
                    processResponse(it?.stateMessage)
                }.single()
        }
    }


    fun rescheduleAlarmsForNewRoutine(prevRoutineId: Long, currentRoutineId: Long) {
        CoroutineScope(activeScope).launch {
            alarmInteractors.rescheduleAlarmsForNewRoutine(prevRoutineId, currentRoutineId)
                .map {
                    processResponse(it?.stateMessage)
                }.single()
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        CoroutineScope(activeScope).launch {
            scheduleInteractors.deleteSchedule(
                schedule,
                object : SnackbarUndoCallback {
                    override fun undo() {
                        saveSchedule(schedule)
                    }
                },
                object : TodoCallback {
                    override fun execute() {
                    }
                }
            ).collect { dataState ->
                processResponse(dataState?.stateMessage)
            }
        }
    }

    private fun saveSchedule(schedule: Schedule) {
        CoroutineScope(activeScope).launch {
            scheduleInteractors.insertSchedule(schedule, listOf(), SCHEDULE_REQUEST_NEW, routineId)
                .collect { dataState ->
                    processResponse(dataState?.stateMessage)
                }
        }
    }
}