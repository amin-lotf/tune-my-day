package com.aminook.tunemyday.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.alarm.AlarmInteractors
import com.aminook.tunemyday.worker.NotificationReceiver.Companion.ALARM_ID
import com.aminook.tunemyday.worker.NotificationReceiver.Companion.SCHEDULE_ID
import kotlinx.coroutines.flow.collect

class AlarmWorker @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    val dateUtil: DateUtil,
    val alarmInteractors: AlarmInteractors
) : CoroutineWorker(appContext, params) {
    private val TAG = "aminjoon"
    val alarmRange = 2


    override suspend fun doWork(): Result {
        try {
            Log.d(TAG, "doWork: ")
            val modifiedAlarmIndexes = mutableListOf<Long>()

            val type = inputData.getString(ACTION_TYPE)
            if (type == TYPE_NEW_SCHEDULE) {
                inputData.getLongArray(MODIFIED_ALARMS_INDEX)?.let {
                    modifiedAlarmIndexes.addAll(
                        it.toList()
                    )
                }
            }

            if (type == TYPE_PERIODIC_SCHEDULE) {
                Log.d(TAG, "doWork: periodic work")
            }

            val todayIndex = dateUtil.curDayIndex
            alarmInteractors.getUpcomingAlarms(
                startDay = todayIndex,
                endDay = todayIndex + alarmRange
            ).collect { dataState ->

                dataState?.data?.let { alarms ->
                    Log.d(TAG, "doWork: size: ${alarms.size}")
                    val alarmManager = applicationContext.getSystemService(AlarmManager::class.java)

                    alarms.forEach {
                        Log.d(TAG, "doWork: at foreach ${it.id}")
                        modifiedAlarmIndexes.remove(it.id)

                        val intent =
                            Intent(applicationContext, NotificationReceiver::class.java).apply {
                                putExtra(SCHEDULE_ID, it.scheduleId)
                                putExtra(ALARM_ID, it.id)
                            }


                        val pendingIntent = PendingIntent.getBroadcast(
                            applicationContext,
                            it.id.toInt(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        val timeDiff = dateUtil.getTimeDifferenceInMills(it.day, it.startInSec)
                        if (timeDiff > 0) {
                            Log.d(
                                TAG,
                                "doWork: now: future:$timeDiff"
                            )

                            val ac = AlarmManager.AlarmClockInfo(
                                System.currentTimeMillis() + dateUtil.getTimeDifferenceInMills(
                                    it.day,
                                    it.startInSec
                                ),
                                pendingIntent
                            )
                            alarmManager.setAlarmClock(ac,pendingIntent)
//                            alarmManager.setExactAndAllowWhileIdle(
//                                AlarmManager.RTC_WAKEUP,
//                                System.currentTimeMillis() + dateUtil.getTimeDifferenceInMills(
//                                    it.day,
//                                    it.startInSec
//                                ),
//                                pendingIntent
//                            )
                            Log.d(
                                TAG,
                                "doWork: alarm set id:${it.id} schedule id:${it.scheduleId} day: ${it.day} start:${it.startInSec}"
                            )
                        } else {
                            Log.d(TAG, "doWork: negative")
                        }

                    }

                    if (modifiedAlarmIndexes.isNotEmpty()) {
                        modifiedAlarmIndexes.forEach {
                            val intent =
                                Intent(applicationContext, NotificationReceiver::class.java)


                            val pendingIntent = PendingIntent.getBroadcast(
                                applicationContext,
                                it.toInt(),
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            alarmManager.cancel(pendingIntent)
                        }
                    }

                }
            }


            return Result.success()
        } catch (e: Throwable) {
            Log.e(TAG, "doWork: ${e.printStackTrace()}")
            return Result.failure()
        }
    }


    companion object {
        val ACTION_TYPE = "action type"
        val MODIFIED_ALARMS_INDEX = "modified alarms index"
        val TYPE_NEW_SCHEDULE = "new schedule"
        val TYPE_PERIODIC_SCHEDULE = "periodic schedule"
    }
}