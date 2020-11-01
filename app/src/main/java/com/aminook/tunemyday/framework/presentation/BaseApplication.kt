package com.aminook.tunemyday.framework.presentation

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.util.DAY_INDEX
import com.aminook.tunemyday.worker.AlarmWorker
import com.aminook.tunemyday.worker.NotificationReceiver.Companion.CHANNEL_ID

import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication : Application(), Configuration.Provider {
    private val TAG = "aminjoon"

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    @DataStoreSettings
    lateinit var dataStore: DataStore<Preferences>

    @Inject
    lateinit var dateUtil: DateUtil

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

        updateDayIndex()
    }

    private fun updateDayIndex() {
        CoroutineScope(Dispatchers.IO).launch {

            dataStore.edit { settings ->
                settings[DAY_INDEX] = dateUtil.curDayIndex
            }

        }

    }


    private fun setPeriodicSchedule() {
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
        Log.d(TAG, "setPeriodicSchedule: an app run timediff: $timeDiff")
        val data = Data.Builder()
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
            ExistingPeriodicWorkPolicy.KEEP,
            periodicAlarmWorker
        )

    }
}
