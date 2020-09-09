package com.aminook.tunemyday.business.data.util

import android.util.Log
import com.aminook.tunemyday.business.data.cache.CacheResult
import com.aminook.tunemyday.business.data.util.CacheConstants.CACHE_TIMEOUT
import com.aminook.tunemyday.business.data.util.CacheConstants.DELETE_SCHEDULE
import com.aminook.tunemyday.business.data.util.CacheConstants.UPDATE_SCHEDULE
import com.aminook.tunemyday.business.data.util.ErrorConstants.CACHE_ERROR_TIMEOUT
import com.aminook.tunemyday.business.data.util.ErrorConstants.CACHE_ERROR_UNKNOWN
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleAndProgram
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.single

suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher,
    cacheCall: () -> Flow<T>
): CacheResult<T?> {
    return withContext(dispatcher) {
        try {
            withTimeout(CACHE_TIMEOUT) {

                //cacheCall.
                CacheResult.Success(null)
            }
        } catch (throwable: Throwable) {
            when (throwable) {

                is TimeoutCancellationException -> {
                    CacheResult.GenericError(CACHE_ERROR_TIMEOUT)
                }
                else -> {
                    CacheResult.GenericError(CACHE_ERROR_UNKNOWN)
                }
            }
        }
    }
}

fun getConflictedSchedules(
    schedules: List<ScheduleAndProgram>,
    target: Schedule
): List<ScheduleAndProgram> {
    return schedules.filter {scheduleAndProgram->

        scheduleAndProgram.schedule.let {
            if (it.startDay == it.endDay) {
                (target.startDay == it.startDay && target.startInSec < it.end && (target.endInSec > it.start || target.endDay != target.startDay)) ||
                        (target.startDay != it.startDay && target.endDay == it.startDay && target.endInSec > it.start)
            } else if (it.startDay == target.startDay) {
                target.endInSec > it.start || target.endDay != target.startDay
            } else if (it.endDay == target.startDay) {
                target.startInSec < it.end
            } else {
                false
            }
        }
    }
}

fun selectSchedulesToDelete(
    schedules: List<ScheduleEntity>,
    target: Schedule
): List<ScheduleEntity> {
    return schedules.filter {
//            Log.d("aminjoon", "getInvolvedSchedules: entity start:${it.startDay} - schedule start:${target.startDay}")
        if (it.startDay == it.endDay) {
            (target.startDay == it.startDay && target.startInSec <= it.start && (target.endInSec >= it.end || target.endDay != target.startDay)) ||
                    (target.startDay != it.startDay && target.endDay == it.startDay && target.endInSec >= it.end)
        } else if (it.startDay == target.startDay) {
            target.startInSec < it.start && target.endInSec >= it.end && target.endDay == it.endDay
        } else {
            false
        }
    }

}

fun updateSchedules(
    schedules: List<ScheduleEntity>,
    target: Schedule
): List<ScheduleEntity> {
    schedules.onEach {
        if (it.startDay == it.endDay) {
            if (target.startDay == it.startDay) {
                if (target.startInSec < it.start) {
                    it.start = target.endInSec
                } else {
                    it.end = target.startInSec
                }
            } else {
                it.start = target.endInSec
            }
        } else if (it.startDay == target.startDay) {
            if (target.startInSec < it.start) {
                it.start = target.endInSec
                it.startDay = target.endDay
            } else {
                it.end = target.startInSec
                it.endDay = target.startDay
            }
        } else if (it.endDay == target.startDay) {
            it.end = target.startInSec
        }
    }

    return schedules
}
