package com.aminook.tunemyday.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.framework.datasource.cache.model.AlarmEntity
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.worker.AlarmWorker.Companion.ACTION_TYPE
import com.aminook.tunemyday.worker.AlarmWorker.Companion.ALARMS_IDS
import com.aminook.tunemyday.worker.AlarmWorker.Companion.ALARM_WORKER_NAME
import com.aminook.tunemyday.worker.AlarmWorker.Companion.TYPE_DELETE_ALARMS
import kotlin.math.roundToLong


class NotificationManager(
    val context: Context,
    val dateUtil:DateUtil
) {
    val TAG="aminjoon"


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
                                        )+500
                                        ).toDouble().roundToLong(),
                                pe2
                            )
                            Log.d(TAG, "setAlarms: alaram :${alarm.id} set")
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
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "cancelAlarms: alaram :$it canceled")
        }
    }


}