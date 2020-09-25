package com.aminook.tunemyday.framework.presentation

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.aminook.tunemyday.worker.AlarmWorker
import com.aminook.tunemyday.worker.NotificationReceiver.Companion.CHANNEL_ID

import dagger.hilt.android.HiltAndroidApp
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication : Application(), Configuration.Provider {
    private val TAG="aminjoon"
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tune My Day",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notification channel for TuneMyDay"
            setShowBadge(true)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
        setPeriodicSchedule()
    }


    fun setPeriodicSchedule() {
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

        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueueUniquePeriodicWork(
            "periodic alarm scheduler",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicAlarmWorker
        )
    }
}