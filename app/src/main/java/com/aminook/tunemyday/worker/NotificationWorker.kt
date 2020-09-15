package com.aminook.tunemyday.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class NotificationWorker(context: Context, appParameters:WorkerParameters):CoroutineWorker(context,appParameters) {
    override suspend fun doWork(): Result = withContext(IO){


        Result.success()
    }

    companion object{

    }
}