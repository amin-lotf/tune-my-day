package com.aminook.tunemyday.framework.presentation

import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.Event
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.StateEvent
import com.aminook.tunemyday.business.domain.state.StateMessage
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramEntity

interface UIController {

    fun <T> onResponseReceived(response: Response?, data: T? = null)
}

interface OnDeleteListener {

    fun onProgramDeleteListener(program: ProgramDetail)
    fun onScheduleDeleted(schedule: Schedule)
}

