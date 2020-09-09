package com.aminook.tunemyday.framework.presentation

import com.aminook.tunemyday.business.domain.state.Event
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.StateEvent
import com.aminook.tunemyday.business.domain.state.StateMessage

interface UIController {

    fun onResponseReceived(response:Response?)
}