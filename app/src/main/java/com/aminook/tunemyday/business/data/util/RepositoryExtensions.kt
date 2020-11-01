package com.aminook.tunemyday.business.data.util

import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.datasource.cache.model.FullSchedule


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
                if (target.startInSec <= it.startInSec) {
                    it.startTime = target.endTime
                } else {
                    it.endTime = target.startTime
                }
            } else {
                it.startTime = target.endTime
            }
        } else if (it.startDay == target.startDay) {
            if (target.startInSec <= it.startInSec) {
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
