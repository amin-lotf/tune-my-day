package com.aminook.tunemyday.worker

import android.content.Context
import androidx.work.*
import com.aminook.tunemyday.worker.AlarmWorker.Companion.ACTION_TYPE
import com.aminook.tunemyday.worker.AlarmWorker.Companion.ALARMS_IDS
import com.aminook.tunemyday.worker.AlarmWorker.Companion.ALARM_WORKER_NAME
import com.aminook.tunemyday.worker.AlarmWorker.Companion.TYPE_ADD_ALARMS
import com.aminook.tunemyday.worker.AlarmWorker.Companion.TYPE_DELETE_ALARMS



class NotificationManager(
    val context: Context
) {


    fun removeNotifications(alarmIds: List<Long>) {
        val data = Data.Builder()
            .putLongArray(ALARMS_IDS, alarmIds.toLongArray())
            .putString(ACTION_TYPE, TYPE_DELETE_ALARMS)
            .build()


        val alarmWorkRequest = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ALARM_WORKER_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            alarmWorkRequest
        )

    }

    fun setNotifications(alarmIds: List<Long>) {
        val data = Data.Builder()
            .putLongArray(ALARMS_IDS, alarmIds.toLongArray())
            .putString(ACTION_TYPE, TYPE_ADD_ALARMS)
            .build()

        val alarmWorker = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ALARM_WORKER_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            alarmWorker
        )
    }


}