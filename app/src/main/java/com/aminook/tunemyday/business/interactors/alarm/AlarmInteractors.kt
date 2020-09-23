package com.aminook.tunemyday.business.interactors.alarm

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmInteractors @Inject constructor(
    val getUpcomingAlarms:GetUpcomingAlarms
)