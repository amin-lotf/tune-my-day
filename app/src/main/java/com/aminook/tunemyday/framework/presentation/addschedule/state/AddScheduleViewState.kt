package com.aminook.tunemyday.framework.presentation.addschedule.state

import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.ViewState

data class AddScheduleViewState(
    var newProgram:Program?=null,
    var newSchedule:Schedule?=null
):ViewState