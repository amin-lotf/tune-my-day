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
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.util.ROUTINE_INDEX
import com.aminook.tunemyday.worker.NotificationReceiver.Companion.ALARM_ID
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.collect
import kotlin.math.log
import kotlin.math.roundToLong

class AlarmWorker @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    val dateUtil: DateUtil,
    val alarmInteractors: AlarmInteractors,
    val notificationManager: NotificationManager,
    @DataStoreCache val dataStoreCache: DataStore<Preferences>
) : CoroutineWorker(appContext, params) {
    private val TAG = "aminjoon"
    val alarmRange = 2


    override suspend fun doWork(): Result {
        try {
            val type = inputData.getString(ACTION_TYPE)
                if (type == TYPE_PERIODIC_SCHEDULE) {
                Log.d(TAG, "doWork: periodic work")
                dataStoreCache.data.collect {
                    val routine = it[ROUTINE_INDEX] ?: 0
                    if (routine != 0L) {
                        alarmInteractors.scheduleAlarmsForCurrentRoutine(routine)
                            .collect {
                                if (it?.data==null || it.data==false){
                                   throw Throwable("failed to schedule periodically")
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