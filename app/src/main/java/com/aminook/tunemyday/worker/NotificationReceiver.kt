package com.aminook.tunemyday.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.alarm.AlarmInteractors
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.worker.AlarmWorker.Companion.ACTION_CALL_FROM_WORKER
import com.aminook.tunemyday.worker.AlarmWorker.Companion.PERIODIC_WORKER_NAME
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver() : HiltBroadcastReceiver() {

    //private val TAG = "aminjoon"

    @Inject
    lateinit var scheduleInteractors: ScheduleInteractors

    @Inject
    lateinit var dateUtil: DateUtil


    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        super.onReceive(context, intent)

        if (intent?.action.equals("android.intent.action.BOOT_COMPLETED")) {
            setPeriodicSchedule(context)
            return
        }

        if (intent?.action.equals(ACTION_CALL_FROM_WORKER)) {
            val alarmId = intent?.getLongExtra(ALARM_ID, 0)
            alarmId?.let { id ->
                try {
                    CoroutineScope(Default).launch {
                        scheduleInteractors.getNotificationScheduleByAlarmId(id)
                            .collect { dataState ->
                                dataState?.data?.let { schedule ->
                                    withContext(Main) {
                                        showNotification(context, schedule)
                                    }
                                }
                            }
                    }
                } catch (e: Throwable) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    e.printStackTrace()
                }
            }
        }
    }

    fun showNotification(context: Context, schedule: Schedule) {
        val alarm = schedule.alarms.first()
        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(R.string.app_name.toString())
            .setContentText("Upcoming Activity")
            .setSmallIcon(R.drawable.ic_notification_new)
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notificationPendingIntent = PendingIntent.getActivity(
            context,
            schedule.id.toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context.applicationContext, CHANNEL_ID)

            .setContentTitle(
                schedule.program.name
            )
            .setSmallIcon(R.drawable.ic_notification_new)
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
            .setContentText(getNotificationSmallFormat(alarm, schedule))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        getNotificationBigFormat(schedule)
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(notificationPendingIntent)
            .setGroup(GROUP_KEY)
            .setAutoCancel(true)
            .build()
        with(NotificationManagerCompat.from(context.applicationContext)) {
            notify(alarm.scheduleId.toInt(), notification)
            notify(GROUP_KEY_ID, summaryNotification)
        }
    }


    private fun getNotificationSmallFormat(alarm: Alarm, schedule: Schedule): String {
        val h = if (alarm.hourBefore == 1) " hour" else " hours "
        val m = if (alarm.minuteBefore == 1) " minute" else " minutes"
        val firstPart = if (alarm.hourBefore == 0 && alarm.minuteBefore == 0) {
            "Starting now!"
        } else if (alarm.hourBefore == 0) {
            "Starts in ${alarm.minuteBefore}$m"

        } else if (alarm.minuteBefore == 0) {
            "Starts in ${alarm.hourBefore}$h"
        } else {

            "Starts in ${alarm.hourBefore}$h ${alarm.minuteBefore}$m"

        }


        return firstPart + " (${schedule.startTime})"
    }

    private fun getNotificationBigFormat(schedule: Schedule): String {
        val alarm = schedule.alarms.first()
        val day = if (dateUtil.curDayIndex == schedule.startDay) "" else "(Tomorrow)"

        return if (alarm.hourBefore == 0 && alarm.minuteBefore == 0) {
            "Starting now! (${schedule.startTime})"
        } else {
            "Starts at ${schedule.startTime} $day"
        }
    }

    private fun setPeriodicSchedule(context: Context) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, 5)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val data = Data.Builder()
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
            PERIODIC_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicAlarmWorker
        )
    }

    companion object {
        const val CHANNEL_ID = "com.aminook.tunemyday.worker-notification-channel"
        const val ALARM_ID = "com.aminook.tunemyday.worker-alarm-id"
        const val GROUP_KEY = "com.aminook.tunemyday.worker-group-key"
        const val GROUP_KEY_ID = -2

    }
}