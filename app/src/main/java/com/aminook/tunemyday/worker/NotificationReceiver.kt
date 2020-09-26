package com.aminook.tunemyday.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.framework.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver() : HiltBroadcastReceiver() {

    private val TAG = "aminjoon"

    @Inject
    lateinit var scheduleInteractors: ScheduleInteractors

    @Inject
    lateinit var dateUtil:DateUtil


    override fun onReceive(context: Context?, intent: Intent?) {
        if(context==null) return
        super.onReceive(context, intent)

        if (intent?.action.equals("android.intent.action.BOOT_COMPLETED")) {
            setPeriodicSchedule(context)
            return
        }


        val scheduleId = intent?.getLongExtra(SCHEDULE_ID, -1)
        val alarmId = intent?.getLongExtra(ALARM_ID, -1)
        Log.d(TAG, "onReceive: id: $scheduleId")
        if (scheduleId!=null && scheduleId != -1L) {
            try {
                CoroutineScope(Default).launch {
                    scheduleInteractors.getSchedule(scheduleId).collect { dataState->

                       dataState?.data?.let {
                           Log.d(TAG, "onReceive broadcast: schedule id:${it.id}  program: ${it.program?.name}")

                           showNotification(context,it,it.alarms.filter { it.id==alarmId }[0])
                       }

                    }
                }
            } catch (e: Throwable) {
                Log.d(TAG, "onReceive: ${e.printStackTrace()}")
            }
        }

    }

    fun showNotification(context: Context,schedule:Schedule,alarm:Alarm){
        val summaryNotification= NotificationCompat.Builder(context,CHANNEL_ID)
            .setContentTitle("Tune My Day")
            .setContentText("Upcoming Activity")
            .setSmallIcon(R.drawable.ic_notification)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        val notificationIntent=Intent(context,MainActivity::class.java).apply {
            flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notificationPendingIntent=PendingIntent.getActivity(
            context,
            schedule.id.toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification=NotificationCompat.Builder(context.applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(schedule.program?.name)
            .setContentText(getNotificationSmallFormat(alarm))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        getNotificationBigFormat(alarm,schedule)
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(notificationPendingIntent)
            .setGroup(GROUP_KEY)
            .setAutoCancel(true)
            .build()
        with(NotificationManagerCompat.from(context.applicationContext)){
            notify(schedule.id.toInt(),notification)
            notify(GROUP_KEY_ID,summaryNotification)
        }
    }


    private fun getNotificationSmallFormat(alarm: Alarm):String{
        val h = if (alarm.hourBefore == 1) " hour" else " hours "
        val m = if (alarm.minuteBefore == 1) " minute" else " minutes"
        return if (alarm.hourBefore == 0 && alarm.minuteBefore == 0) {
            "Starting now!"
        } else if (alarm.hourBefore == 0) {
            "Starts in ${alarm.minuteBefore}$m"

        }else if(alarm.minuteBefore==0){
            "Starts in ${alarm.hourBefore}$h"
        } else {

            "Starts in ${alarm.hourBefore}$h ${alarm.minuteBefore}$m"

        }
    }

    private fun getNotificationBigFormat(alarm: Alarm,schedule: Schedule):String{
        val day=if(dateUtil.curDayIndex==schedule.startDay) "" else "(Tomorrow)"
        val time="${schedule.startTime.hour}:${schedule.startTime.minute}"
        return if (alarm.hourBefore == 0 && alarm.minuteBefore == 0) {
            "Starting now!"
        } else  {
            "Starts at $time$day and is going to take ${schedule.duration}"
        }
    }

    private fun setPeriodicSchedule(context: Context) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // Set Execution around 05:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 5)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        Log.d(TAG, "setPeriodicSchedule: timediff: $timeDiff")
        val data= Data.Builder()
            .putString(AlarmWorker.ACTION_TYPE, AlarmWorker.TYPE_PERIODIC_SCHEDULE)
            .build()
        val periodicAlarmWorker = PeriodicWorkRequestBuilder<AlarmWorker>(
            timeDiff,
            TimeUnit.MILLISECONDS
        )
            .setInputData(data)
            .build()

        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.enqueueUniquePeriodicWork(
            "periodic alarm scheduler",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicAlarmWorker
        )
    }



    companion object {
        val CHANNEL_ID="com.aminook.tunemyday.worker-notification-channel"
        val SCHEDULE_ID = "com.aminook.tunemyday.worker-schedule-id"
        val ALARM_ID = "com.aminook.tunemyday.worker-alarm-id"
        val GROUP_KEY = "com.aminook.tunemyday.worker-group-key"
        val GROUP_KEY_ID = -2

    }
}