package com.aminook.tunemyday.framework.presentation.dailylist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.schedule.ScheduleInteractors
import kotlinx.coroutines.Dispatchers

class DailyViewModel @ViewModelInject constructor(
    val dateUtil:DateUtil,
    val scheduleInteractors: ScheduleInteractors
):ViewModel() {

    val activeScope= Dispatchers.IO + viewModelScope.coroutineContext


}