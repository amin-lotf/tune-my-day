package com.aminook.tunemyday.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.alarm.GetAlarmsById
import com.aminook.tunemyday.framework.datasource.cache.model.AlarmEntity
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.worker.AlarmWorker.Companion.ACTION_TYPE
import com.aminook.tunemyday.worker.AlarmWorker.Companion.ALARMS_IDS
import com.aminook.tunemyday.worker.AlarmWorker.Companion.ALARM_WORKER_NAME
import com.aminook.tunemyday.worker.AlarmWorker.Companion.TYPE_ADD_ALARMS
import com.aminook.tunemyday.worker.AlarmWorker.Companion.TYPE_DELETE_ALARMS
import kotlinx.coroutines.flow.collect
import kotlin.math.roundToLong


class NotificationManager(
    val context: Context,
    val dateUtil:DateUtil
) {
    val TAG="aminjoon"

    fun removeNotifications_bck(alarmIds: List<Long>) {
        if (alarmIds.isEmpty()){
            Log.d(TAG, "removeNotifications: doWorkk new empty")
        }
        val data = Data.Builder()
            .putLongArray(ALARMS_IDS, alarmIds.toLongArray())
            .putString(ACTION_TYPE, TYPE_DELETE_ALARMS)
            .build()


        val alarmWorkRequest = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
            .setInputData(data)
            .build()
        Log.d(TAG, "removeNotifications: doWorkk ")
        WorkManager.getInstance(context).enqueueUniqueWork(
            ALARM_WORKER_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            alarmWorkRequest
        )

    }

//    fun setNotifications_bck(alarmIds: List<Long>) {
//        if (alarmIds.isEmpty()){
//            Log.d(TAG, "setNotifications: doWorkk new empty")
//        }
//        val data = Data.Builder()
//            .putLongArray(ALARMS_IDS, alarmIds.toLongArray())
//            .putString(ACTION_TYPE, TYPE_ADD_ALARMS)
//            .build()
//
//        val alarmWorker = OneTimeWorkRequest.Builder(AlarmWorker::class.java)
//            .setInputData(data)
//            .build()
//        Log.d(TAG, "setNotifications: doWorkk ")
//        WorkManager.getInstance(context).enqueueUniqueWork(
//            ALARM_WORKER_NAME,
//            ExistingWorkPolicy.APPEND_OR_REPLACE,
//            alarmWorker
//        )
//    }

    fun addAlarms(alarms: List<AlarmEntity>) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
                    alarms.forEach { alarm ->
                        val timeDiff =
                            dateUtil.getTimeDifferenceInMills(alarm.day, alarm.startInSec)

                        if (timeDiff > 0) {
                            val intent = Intent(
                                context,
                                NotificationReceiver::class.java
                            ).apply {
                                putExtra(NotificationReceiver.ALARM_ID, alarm.id)
                                action = AlarmWorker.ACTION_CALL_FROM_WORKER
                            }

                            val pendingIntent = PendingIntent.getBroadcast(
                                context,
                                alarm.id.toInt(),
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            val intentMain = Intent(context, MainActivity::class.java)
                            val pe2 =
                                PendingIntent.getActivity(context, 0, intentMain, 0)
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


     fun cancelAlarms(alarms: List<Long>) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarms.forEach {
            val intent = Intent(
                context,
                NotificationReceiver::class.java
            ).apply {
                putExtra(NotificationReceiver.ALARM_ID, it.toInt())
                action = AlarmWorker.ACTION_CALL_FROM_WORKER
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                it.toInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "cancelAlarms: alaram :$it canceled")
        }
    }


}