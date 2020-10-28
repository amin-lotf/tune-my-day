package com.aminook.tunemyday.business.interactors.alarm

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmInteractors @Inject constructor(
    val getUpcomingAlarms:GetUpcomingAlarms,
    val scheduleUpcomingAlarms: ScheduleUpcomingAlarms,
    val getAlarmsById: GetAlarmsById,

    val cancelUpcomingAlarmsByRoutine: CancelUpcomingAlarmsByRoutine,
    val getUpcomingAlarmIdsByRoutine: GetUpcomingAlarmIdsByRoutine,
    val scheduleUpcomingAlarmsByRoutine: ScheduleUpcomingAlarmsByRoutine
)