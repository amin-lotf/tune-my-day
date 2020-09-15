package com.aminook.tunemyday.business.data.util

import com.aminook.tunemyday.business.data.cache.CacheResult
import com.aminook.tunemyday.business.data.util.CacheConstants.CACHE_TIMEOUT
import com.aminook.tunemyday.business.data.util.ErrorConstants.CACHE_ERROR_TIMEOUT
import com.aminook.tunemyday.business.data.util.ErrorConstants.CACHE_ERROR_UNKNOWN
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.datasource.cache.model.FullSchedule
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow

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
    schedules: List<FullSchedule>,
    target: Schedule
): List<FullSchedule> {
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
    schedules: List<Schedule>,
    target: Schedule
): List<Schedule> {
    return schedules.filter {
//            Log.d("aminjoon", "getInvolvedSchedules: entity start:${it.startDay} - schedule start:${target.startDay}")
        if (it.startDay == it.endDay) {
            (target.startDay == it.startDay && target.startInSec <= it.startInSec && (target.endInSec >= it.endInSec || target.endDay != target.startDay)) ||
                    (target.startDay != it.startDay && target.endDay == it.startDay && target.endInSec >= it.endInSec)
        } else if (it.startDay == target.startDay) {
            target.startInSec < it.startInSec && target.endInSec >= it.endInSec && target.endDay == it.endDay
        } else {
            false
        }
    }

}

fun updateSchedules(
    schedules: List<Schedule>,
    target: Schedule
): List<Schedule> {
    schedules.onEach {
        if (it.startDay == it.endDay) {
            if (target.startDay == it.startDay) {
                if (target.startInSec < it.startInSec) {
                    it.startTime = target.endTime
                } else {
                    it.endTime = target.startTime
                }
            } else {
                it.startTime = target.endTime
            }
        } else if (it.startDay == target.startDay) {
            if (target.startInSec < it.startInSec) {
                it.startTime = target.endTime
                it.startDay = target.endDay
            } else {
                it.endTime = target.startTime

            }
        } else if (it.endDay == target.startDay) {
            it.endTime = target.startTime
        }
    }

    return schedules
}
