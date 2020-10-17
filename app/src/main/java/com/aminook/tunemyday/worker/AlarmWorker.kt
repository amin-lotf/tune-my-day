package com.aminook.tunemyday.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.lifecycle.asLiveData
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.alarm.AlarmInteractors
import com.aminook.tunemyday.business.interactors.alarm.GetAlarmsById.Companion.ALARMS_GET_SUCCESS
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.util.ROUTINE_INDEX
import com.aminook.tunemyday.worker.NotificationReceiver.Companion.ALARM_ID
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.collect
import kotlin.math.roundToLong

class AlarmWorker @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    val dateUtil: DateUtil,
    val alarmInteractors: AlarmInteractors,
    @DataStoreCache val dataStoreCache: DataStore<Preferences>
) : CoroutineWorker(appContext, params) {
    private val TAG = "aminjoon"
    val alarmRange = 2


    override suspend fun doWork(): Result {
        try {
            Log.d(TAG, "doWorkk: ")

            val type = inputData.getString(ACTION_TYPE)

            if (type == TYPE_DELETE_ALARMS) {
                Log.d(TAG, "doWorkk new: remove alarms")
                inputData.getLongArray(ALARMS_IDS)?.let { alarmIds ->
                    alarmIds.forEach {
                        Log.d(TAG, "doWorkk new remove id: $it")
                    }
                    cancelAlarms(alarmIds)
                }
            } else if (type == TYPE_ADD_ALARMS) {
                Log.d(TAG, "doWorkk new: add alarms")
                inputData.getLongArray(ALARMS_IDS)?.let { alarmIds ->
                    alarmIds.forEach {
                        Log.d(TAG, "doWorkk new id: $it")
                    }
                    addAlarms(alarmIds.toList())
                }
            } else if (type == TYPE_PERIODIC_SCHEDULE) {
                Log.d(TAG, "doWork: periodic work")
                dataStoreCache.data.collect {
                    val routine = it[ROUTINE_INDEX] ?: 0
                    if (routine != 0L) {
                        alarmInteractors.getUpcomingAlarmIdsByRoutine(routine)
                            .collect { dataState ->
                                dataState?.data?.let { ids ->
                                    if (ids.isNotEmpty()) {
                                        addAlarms(ids)
                                    }
                                }
                            }
                    }
                }
            }
            return Result.success()
        } catch (e: Throwable) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.d(TAG, "doWork: Error in alarm worker")
            return Result.failure()
        }
    }

    private suspend fun addAlarms(alarms: List<Long>) {
        val alarmManager = applicationContext.getSystemService(AlarmManager::class.java)

        alarmInteractors.getAlarmsById(alarms).collect { dataState ->
            if (dataState?.stateMessage?.getContentIfNotHandled()?.response?.message == ALARMS_GET_SUCCESS) {
                dataState.data?.let { alarms ->
                    alarms.forEach { alarm ->
                        val timeDiff =
                            dateUtil.getTimeDifferenceInMills(alarm.day, alarm.startInSec)

                        if (timeDiff > 0) {
                            val intent = Intent(
                                applicationContext,
                                NotificationReceiver::class.java
                            ).apply {
                                putExtra(ALARM_ID, alarm.id)
                                action = ACTION_CALL_FROM_WORKER
                            }

                            val pendingIntent = PendingIntent.getBroadcast(
                                applicationContext,
                                alarm.id.toInt(),
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            val intentMain = Intent(applicationContext, MainActivity::class.java)
                            val pe2 =
                                PendingIntent.getActivity(applicationContext, 0, intentMain, 0)
                            val ac = AlarmManager.AlarmClockInfo(
                                (
                                        System.currentTimeMillis() + dateUtil.getTimeDifferenceInMills
                                            (
                                            alarm.day,
                                            alarm.startInSec
                                        )
                                        ).toDouble().roundToLong(),
                                pe2
                            )
                            alarmManager.setAlarmClock(ac, pendingIntent)

                            //alarmManager.
//                            alarmManager.setExactAndAllowWhileIdle(
//                                AlarmManager.RTC_WAKEUP,
//                                System.currentTimeMillis() + dateUtil.getTimeDifferenceInMills(
//                                    alarm.day,
//                                    alarm.startInSec
//                                ),
//                                pendingIntent
//                            )
                        }
                    }
                }
            }
        }
    }


    private fun cancelAlarms(alarms: LongArray) {
        val alarmManager = applicationContext.getSystemService(AlarmManager::class.java)
        alarms.forEach {
            val intent = Intent(
                applicationContext,
                NotificationReceiver::class.java
            ).apply {
                putExtra(ALARM_ID, it.toInt())
                action = ACTION_CALL_FROM_WORKER
            }

            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                it.toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "cancelAlarms: alaram :$it canceled")
        }
    }


    companion object {
        const val ACTION_TYPE = "action type"
        const val ALARMS_IDS = "alarm ids"
        const val TYPE_PERIODIC_SCHEDULE = "periodic schedule"
        const val TYPE_DELETE_ALARMS = "delete alarms"
        const val TYPE_ADD_ALARMS = "add alarms"
        const val ALARM_WORKER_NAME = "setupAlarm"
        const val PERIODIC_WORKER_NAME = "setupAlarm"
        const val ACTION_CALL_FROM_WORKER = "com.aminook.tunemyday.worker.show.notification"
    }
}