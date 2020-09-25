package com.aminook.tunemyday.business.interactors.schedule

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleInteractors @Inject constructor(
    val getDaysOfWeek: GetDaysOfWeek,
    val insertSchedule: InsertSchedule,
    val validateSchedule: ValidateSchedule,
    val getDailySchedules: GetDailySchedules,
    val getSchedule: GetSchedule,
    val deleteSchedule: DeleteSchedule
)