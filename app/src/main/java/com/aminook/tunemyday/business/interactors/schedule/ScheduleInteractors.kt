package com.aminook.tunemyday.business.interactors.schedule

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleInteractors @Inject constructor(
    val getDaysOfWeek: GetDaysOfWeek
)