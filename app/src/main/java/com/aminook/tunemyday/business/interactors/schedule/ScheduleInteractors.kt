package com.aminook.tunemyday.business.interactors.schedule

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleInteractors @Inject constructor(
    val insertSchedule: InsertSchedule,
    val validateSchedule: ValidateSchedule,
    val getSchedule: GetSchedule,
    val deleteSchedule: DeleteSchedule,
    val getDailySchedules: GetDailySchedules,
    val getNotificationScheduleByAlarmId: GetNotificationScheduleByAlarmId
)