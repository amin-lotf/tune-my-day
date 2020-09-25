package com.aminook.tunemyday.framework.presentation

import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.Event
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.StateEvent
import com.aminook.tunemyday.business.domain.state.StateMessage

interface UIController {

    fun <T> onResponseReceived(response:Response?,data:T?=null)
}

interface OnScheduleDeleteListener {

    fun  onScheduleDeleted(schedule:Schedule)
}