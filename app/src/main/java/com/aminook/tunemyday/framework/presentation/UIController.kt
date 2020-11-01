package com.aminook.tunemyday.framework.presentation

import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.Response

interface UIController {
    fun <T> onResponseReceived(response: Response?, data: T? = null)
}

interface OnDeleteListener {
    fun onScheduleDeleted(schedule: Schedule)
}

